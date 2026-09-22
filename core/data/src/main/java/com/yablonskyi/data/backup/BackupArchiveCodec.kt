package com.yablonskyi.data.backup

import com.yablonskyi.domain.repository.BackupError
import com.yablonskyi.model.backup.BackupAsset
import com.yablonskyi.model.backup.BackupDataV1
import com.yablonskyi.model.backup.BackupManifest
import com.yablonskyi.model.backup.BackupRecordCounts
import com.yablonskyi.model.backup.BackupValidationLimits
import com.yablonskyi.model.backup.BackupValidator
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction
import java.nio.file.Files
import java.security.MessageDigest
import java.time.Instant
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

internal data class BackupArchiveLimits(
    val entries: Int = 10_002,
    val manifestBytes: Long = 4L * 1024 * 1024,
    val dataBytes: Long = 32L * 1024 * 1024,
    val imageBytes: Long = 32L * 1024 * 1024,
    val totalBytes: Long = 512L * 1024 * 1024,
    val archiveBytes: Long = 520L * 1024 * 1024,
)

internal class BackupArchiveException(val error: BackupError, message: String) : IOException(message)

/** Caller owns the returned directory and must remove it after discard/completion. */
internal data class StagedBackup(val directory: File, val manifest: BackupManifest, val data: BackupDataV1) {
    val images: Map<String, File> get() = manifest.assets.associate { it.id to File(directory, it.path) }
}

/** Blocking, private-file codec. Invoke on an IO dispatcher while the application write gate is held. */
internal class BackupArchiveCodec(
    private val limits: BackupArchiveLimits = BackupArchiveLimits(),
    private val validationLimits: BackupValidationLimits = BackupValidationLimits(),
) {
    private val json = Json { encodeDefaults = true }
    private val imagePath = Regex("images/[A-Za-z0-9_-]{1,100}\\.[A-Za-z0-9]{1,10}")

    /** Creates a new private archive. Source images are streamed once; metadata describes those exact bytes. */
    suspend fun write(
        data: BackupDataV1,
        images: Map<String, File>,
        parent: File,
        appVersion: String,
        createdAt: Instant,
    ): File {
        currentCoroutineContext().ensureActive()
        val file = Files.createTempFile(parent.toPath(), "backup-", ".zip").toFile()
        try {
            val dataBytes = json.encodeToString(data).toByteArray(Charsets.UTF_8)
            limit(dataBytes.size.toLong(), limits.dataBytes)
            limit(images.size.toLong() + 2, limits.entries.toLong())
            var total = dataBytes.size.toLong()
            ZipOutputStream(file.outputStream().buffered()).use { zip ->
                zip.putNextEntry(ZipEntry("data.json"))
                zip.write(dataBytes)
                zip.closeEntry()
                val assets = images.map { (id, source) ->
                    currentCoroutineContext().ensureActive()
                    val path = "images/$id.bin"
                    valid(imagePath.matches(path), "Invalid image ID")
                    if (!source.isFile) throw BackupArchiveException(BackupError.MISSING_IMAGE, "Missing portrait")
                    zip.putNextEntry(ZipEntry(path))
                    val digest = source.inputStream().use {
                        copyCancellable(it, zip, minOf(limits.imageBytes, limits.totalBytes - total))
                    }
                    total += digest.size
                    zip.closeEntry()
                    BackupAsset(id, path, digest.size, digest.sha256)
                }
                val manifest = BackupManifest(BackupValidator.FORMAT, BackupValidator.VERSION, createdAt.toString(), appVersion,
                    BackupRecordCounts(data.characters.size, data.attacks.size, data.diceRolls.size, data.spells.size,
                        data.characterSpells.size, data.races.size, data.classes.size), hash(dataBytes), assets)
                BackupValidator.validate(manifest, data, validationLimits)
                val manifestBytes = json.encodeToString(manifest).toByteArray(Charsets.UTF_8)
                limit(manifestBytes.size.toLong(), limits.manifestBytes)
                limit(total + manifestBytes.size, limits.totalBytes)
                zip.putNextEntry(ZipEntry("manifest.json"))
                zip.write(manifestBytes)
                zip.closeEntry()
            }
            limit(file.length(), limits.archiveBytes)
            return file
        } catch (failure: Throwable) {
            cleanup(file, failure)
            throw failure
        }
    }

    /** Reads a complete privately staged ZIP (central directory required); never touches live stores. */
    suspend fun read(archive: File, parent: File): StagedBackup {
        currentCoroutineContext().ensureActive()
        limit(archive.length(), limits.archiveBytes)
        val directory = Files.createTempDirectory(parent.toPath(), "restore-").toFile()
        try {
            ZipFile(archive).use { zip ->
                val entries = linkedMapOf<String, ZipEntry>()
                val iterator = zip.entries()
                var declaredTotal = 0L
                while (iterator.hasMoreElements()) {
                    currentCoroutineContext().ensureActive()
                    val entry = iterator.nextElement()
                    limit(entries.size.toLong() + 1, limits.entries.toLong())
                    valid(!entry.isDirectory && (entry.name == "manifest.json" || entry.name == "data.json" || imagePath.matches(entry.name)),
                        "Unsafe or unexpected ZIP entry")
                    valid(entries.put(entry.name, entry) == null, "Duplicate ZIP entry")
                    valid(entry.size >= 0, "Missing entry size")
                    val maximum = when (entry.name) {
                        "manifest.json" -> limits.manifestBytes
                        "data.json" -> limits.dataBytes
                        else -> limits.imageBytes
                    }
                    limit(entry.size, maximum)
                    limit(entry.size, limits.totalBytes - declaredTotal)
                    declaredTotal += entry.size
                }
                valid("manifest.json" in entries && "data.json" in entries, "Missing required section")
                var actualTotal = 0L
                suspend fun extract(name: String): Pair<File, Digest> {
                    val entry = entries.getValue(name)
                    val target = File(directory, name).canonicalFile
                    valid(target.toPath().startsWith(directory.canonicalFile.toPath()), "Unsafe extraction path")
                    Files.createDirectories(requireNotNull(target.parentFile).toPath())
                    val digest = target.outputStream().use { output ->
                        zip.getInputStream(entry).use { input ->
                            copyCancellable(input, output, minOf(entry.size, limits.totalBytes - actualTotal))
                        }
                    }
                    actualTotal += digest.size
                    valid(digest.size == entry.size && digest.crc == entry.crc, "Corrupt ZIP entry")
                    return target to digest
                }
                val manifest = json.decodeFromString<BackupManifest>(readJson(extract("manifest.json").first))
                BackupValidator.validateManifest(manifest, validationLimits)
                valid(entries.keys == (setOf("manifest.json", "data.json") + manifest.assets.map { it.path }), "Unexpected or missing assets")
                val (dataFile, dataDigest) = extract("data.json")
                valid(dataDigest.sha256 == manifest.dataSha256, "Data checksum mismatch")
                val data = json.decodeFromString<BackupDataV1>(readJson(dataFile))
                BackupValidator.validate(manifest, data, validationLimits)
                for (asset in manifest.assets) {
                    currentCoroutineContext().ensureActive()
                    val (_, digest) = extract(asset.path)
                    valid(digest.size == asset.sizeBytes && digest.sha256 == asset.sha256, "Image checksum mismatch")
                }
                return StagedBackup(directory, manifest, data)
            }
        } catch (failure: Throwable) {
            cleanup(directory, failure)
            throw failure
        }
    }

    private suspend fun readJson(file: File): String {
        val context = currentCoroutineContext()
        val bytes = ByteArrayOutputStream()
        file.inputStream().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                context.ensureActive()
                val count = input.read(buffer)
                context.ensureActive()
                if (count < 0) break
                bytes.write(buffer, 0, count)
            }
        }
        val text = Charsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes.toByteArray())).toString()
        // Bound nesting before the recursive JSON decoder sees untrusted content.
        var depth = 0
        var quoted = false
        var escaped = false
        text.forEachIndexed { index, char ->
            if (index % DEFAULT_BUFFER_SIZE == 0) context.ensureActive()
            if (quoted) {
                if (escaped) escaped = false
                else if (char == '\\') escaped = true
                else if (char == '"') quoted = false
            } else when (char) {
                '"' -> quoted = true
                '{', '[' -> { depth++; valid(depth <= 64, "JSON nesting limit exceeded") }
                '}', ']' -> { depth--; valid(depth >= 0, "Invalid JSON structure") }
            }
        }
        valid(depth == 0 && !quoted, "Invalid JSON structure")
        return text
    }

    private data class Digest(val size: Long, val sha256: String, val crc: Long)

    private suspend fun copyCancellable(input: InputStream, output: OutputStream, maximum: Long): Digest {
        val context = currentCoroutineContext()
        val sha = MessageDigest.getInstance("SHA-256")
        val crc = CRC32()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var size = 0L
        while (true) {
            context.ensureActive()
            val count = input.read(buffer)
            context.ensureActive()
            if (count < 0) break
            limit(count.toLong(), maximum - size)
            output.write(buffer, 0, count)
            sha.update(buffer, 0, count)
            crc.update(buffer, 0, count)
            size += count
        }
        return Digest(size, hex(sha.digest()), crc.value)
    }

    private fun hash(bytes: ByteArray) = hex(MessageDigest.getInstance("SHA-256").digest(bytes))
    private fun hex(bytes: ByteArray) = bytes.joinToString("") { "%02x".format(it.toInt() and 255) }

    private fun valid(condition: Boolean, message: String) {
        if (!condition) throw BackupArchiveException(BackupError.INVALID_ARCHIVE, message)
    }

    private fun limit(size: Long, maximum: Long) {
        if (size > maximum) throw BackupArchiveException(BackupError.SIZE_LIMIT_EXCEEDED, "Backup size limit exceeded")
    }

    private fun cleanup(target: File, failure: Throwable) {
        // Only files/directories created by this invocation are passed here.
        if (!target.deleteRecursively()) failure.addSuppressed(IOException("Could not remove backup staging"))
    }
}
