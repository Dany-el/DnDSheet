package com.yablonskyi.data.backup

import com.yablonskyi.domain.repository.BackupError
import com.yablonskyi.model.backup.BackupManifest
import com.yablonskyi.model.backup.BackupRecordCounts
import com.yablonskyi.model.backup.BackupValidationException
import com.yablonskyi.model.backup.BackupValidationFailure
import com.yablonskyi.model.backup.BackupValidator
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.security.MessageDigest
import java.time.Instant
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class BackupArchiveCodecTest {
    @get:Rule val temporary = TemporaryFolder()
    private val codec = BackupArchiveCodec()

    @Test fun givenEmptySnapshot_whenArchived_thenRoundTrips() = runTest {
        val data = BackupRoomSnapshot(emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
            .toBackup(emptyMap())
        val archive = codec.write(data, emptyMap(), temporary.root, "1.0", Instant.parse("2026-09-21T12:00:00Z"))
        val restored = codec.read(archive, temporary.root)
        assertEquals(data, restored.data)
        assertEquals("2026-09-21T12:00:00Z", restored.manifest.createdAt)
    }

    @Test fun givenUnsafeOrUnexpectedEntry_whenRead_thenRejectsAndCleansStaging() = runTest {
        listOf("../escape", "images/../../escape", "C:/escape", "images\\escape", "unexpected").forEach { name ->
            val archive = temporary.newFile()
            ZipOutputStream(archive.outputStream()).use { zip ->
                zip.putNextEntry(ZipEntry(name)); zip.write(byteArrayOf(1)); zip.closeEntry()
            }
            val before = temporary.root.list()!!.toSet()
            expectFailure<Exception> { codec.read(archive, temporary.root) }
            assertEquals(before, temporary.root.list()!!.toSet())
        }
    }

    @Test fun givenTruncatedArchive_whenRead_thenRejects() = runTest {
        val archive = temporary.newFile().apply { writeBytes(byteArrayOf(0x50, 0x4b, 3, 4)) }
        expectFailure<Exception> { codec.read(archive, temporary.root) }
    }

    @Test fun givenOversizedEntry_whenRead_thenRejects() = runTest {
        val archive = temporary.newFile()
        ZipOutputStream(archive.outputStream()).use { zip ->
            zip.putNextEntry(ZipEntry("manifest.json")); zip.write(ByteArray(1025)); zip.closeEntry()
        }
        expectFailure<BackupArchiveException> {
            BackupArchiveCodec(BackupArchiveLimits(manifestBytes = 1024)).read(archive, temporary.root)
        }
    }

    @Test fun givenCompleteSnapshotWithPortrait_whenArchived_thenPreservesAllRecordsAndOriginalBytes() = runTest {
        val (archive, data) = fullArchive()
        val restored = codec.read(archive, temporary.root)
        assertEquals(data, restored.data)
        assertArrayEquals(byteArrayOf(1, 2, 3, 4), restored.images.getValue("portrait").readBytes())
        assertEquals(1001, restored.data.diceRolls.size)
        assertEquals(2, restored.data.spells.size)
    }

    @Test fun givenVersionOneCharacterNotes_whenRead_thenConvertsExactLegacyText() = runTest {
        val (currentArchive, _) = fullArchive()
        val legacy = "  🐉\n{\"looks\":\"like JSON\"}  "
        val previous = rewrite(currentArchive) { entries ->
            val data = Json.parseToJsonElement(entries.getValue("data.json").toString(Charsets.UTF_8)).jsonObject
            val characters = data.getValue("characters").jsonArray.mapIndexed { index, element ->
                JsonObject(element.jsonObject + ("notes" to JsonPrimitive(if (index == 0) legacy else "")))
            }
            val dataBytes = JsonObject(data + ("characters" to JsonArray(characters))).toString().toByteArray(Charsets.UTF_8)
            val manifest = Json.decodeFromString<BackupManifest>(entries.getValue("manifest.json").toString(Charsets.UTF_8))
            val checksum = MessageDigest.getInstance("SHA-256").digest(dataBytes)
                .joinToString("") { "%02x".format(it.toInt() and 255) }
            entries["data.json"] = dataBytes
            entries["manifest.json"] = Json.encodeToString(manifest.copy(formatVersion = 1, dataSha256 = checksum))
                .toByteArray(Charsets.UTF_8)
        }

        val staged = codec.read(previous, temporary.root)

        assertEquals(1, staged.manifest.formatVersion)
        assertEquals("Notes", staged.data.characters.first().notes.single().topic)
        assertEquals(legacy, staged.data.characters.first().notes.single().text.plainText)
        assertEquals("", staged.data.characters.last().notes.single().text.plainText)
        assertEquals("Attack note", staged.data.attacks.single().notes)
    }

    @Test fun givenChangedDataOrImage_whenRead_thenRejectsChecksumMismatch() = runTest {
        val (archive, _) = fullArchive()
        listOf("data.json", "images/portrait.bin").forEach { name ->
            val corrupt = rewrite(archive) { entries -> entries[name] = entries.getValue(name) + byteArrayOf(32) }
            assertEquals(BackupError.INVALID_ARCHIVE,
                expectFailure<BackupArchiveException> { codec.read(corrupt, temporary.root) }.error)
        }
    }

    @Test fun givenMissingImageOrSection_whenRead_thenRejects() = runTest {
        val (archive, _) = fullArchive()
        listOf("manifest.json", "data.json", "images/portrait.bin").forEach { name ->
            val corrupt = rewrite(archive) { it.remove(name) }
            expectFailure<BackupArchiveException> { codec.read(corrupt, temporary.root) }
        }
    }

    @Test fun givenNewerVersion_whenRead_thenReportsUnsupportedVersion() = runTest {
        val (archive, _) = fullArchive()
        val newer = rewrite(archive) { entries ->
            entries["manifest.json"] = entries.getValue("manifest.json").toString(Charsets.UTF_8)
                .replace("\"formatVersion\":2", "\"formatVersion\":3").toByteArray()
        }
        assertEquals(BackupValidationFailure.UNSUPPORTED_VERSION,
            expectFailure<BackupValidationException> { codec.read(newer, temporary.root) }.failure)
    }

    @Test fun givenDuplicateZipNames_whenRead_thenRejects() = runTest {
        val (archive, _) = fullArchive()
        val duplicate = rewrite(archive) { it["evil.json"] = it.getValue("data.json") }
        // ZIP output rejects duplicates itself; patch equal-length names in both ZIP headers.
        duplicate.writeBytes(duplicate.readBytes().toString(Charsets.ISO_8859_1)
            .replace("evil.json", "data.json").toByteArray(Charsets.ISO_8859_1))
        expectFailure<BackupArchiveException> { codec.read(duplicate, temporary.root) }
    }

    @Test fun givenEntryOrTotalLimit_whenRead_thenRejects() = runTest {
        val (archive, _) = fullArchive()
        listOf(BackupArchiveLimits(entries = 2), BackupArchiveLimits(totalBytes = 100)).forEach { limits ->
            assertEquals(BackupError.SIZE_LIMIT_EXCEEDED,
                expectFailure<BackupArchiveException> { BackupArchiveCodec(limits).read(archive, temporary.root) }.error)
        }
    }

    @Test fun givenExcessiveNestingMalformedStructureOrUtf8_whenRead_thenRejectsAndCleansStaging() = runTest {
        val invalidDocuments = listOf(
            ("[".repeat(65) + "]".repeat(65)).toByteArray(),
            "}".toByteArray(),
            byteArrayOf(0xC3.toByte()),
        )
        invalidDocuments.forEach { dataBytes ->
            val archive = rawArchive(dataBytes)
            val before = temporary.root.list()!!.toSet()
            expectFailure<Exception> { codec.read(archive, temporary.root) }
            assertEquals(before, temporary.root.list()!!.toSet())
        }
    }

    @Test fun givenMissingSourcePortrait_whenWritten_thenFailsAndRemovesPartialArchive() = runTest {
        val data = backupFixture().toBackup(mapOf("/old/portrait.jpg" to "portrait"))
        val before = temporary.root.list()!!.toSet()
        val failure = try {
            codec.write(data, mapOf("portrait" to File(temporary.root, "missing")), temporary.root, "1", Instant.now())
            fail("Expected missing image failure")
            null
        } catch (failure: BackupArchiveException) {
            failure
        }
        assertEquals(BackupError.MISSING_IMAGE, failure?.error)
        assertEquals(before, temporary.root.list()!!.toSet())
    }

    private suspend fun fullArchive(): Pair<File, com.yablonskyi.model.backup.BackupDataV2> {
        val image = temporary.newFile().apply { writeBytes(byteArrayOf(1, 2, 3, 4)) }
        val data = backupFixture().toBackup(mapOf("/old/portrait.jpg" to "portrait"))
        return codec.write(data, mapOf("portrait" to image), temporary.root, "1", Instant.now()) to data
    }

    private fun rewrite(source: File, change: (MutableMap<String, ByteArray>) -> Unit): File {
        val entries = ZipFile(source).use { zip -> zip.entries().asSequence().associate { entry ->
            entry.name to zip.getInputStream(entry).use { it.readBytes() }
        }.toMutableMap() }
        change(entries)
        return temporary.newFile().also { target ->
            ZipOutputStream(target.outputStream()).use { zip -> entries.forEach { (name, bytes) ->
                zip.putNextEntry(ZipEntry(name)); zip.write(bytes); zip.closeEntry()
            } }
        }
    }

    private fun rawArchive(dataBytes: ByteArray): File {
        val manifest = BackupManifest(
            format = BackupValidator.FORMAT,
            formatVersion = BackupValidator.VERSION,
            createdAt = "2026-09-21T12:00:00Z",
            sourceAppVersion = "test",
            counts = BackupRecordCounts(0, 0, 0, 0, 0, 0, 0),
            dataSha256 = MessageDigest.getInstance("SHA-256").digest(dataBytes)
                .joinToString("") { "%02x".format(it.toInt() and 255) },
            assets = emptyList(),
        )
        return temporary.newFile().also { archive ->
            ZipOutputStream(archive.outputStream()).use { zip ->
                zip.putNextEntry(ZipEntry("manifest.json"))
                zip.write(Json.encodeToString(manifest).toByteArray())
                zip.closeEntry()
                zip.putNextEntry(ZipEntry("data.json"))
                zip.write(dataBytes)
                zip.closeEntry()
            }
        }
    }

    private suspend inline fun <reified T : Throwable> expectFailure(
        crossinline block: suspend () -> Unit,
    ): T {
        try {
            block()
        } catch (failure: Throwable) {
            if (failure is T) return failure
            throw failure
        }
        fail("Expected ${T::class.java.simpleName}")
        error("Unreachable")
    }
}