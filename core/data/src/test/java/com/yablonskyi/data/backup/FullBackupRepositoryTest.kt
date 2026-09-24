package com.yablonskyi.data.backup

import androidx.room.Room
import com.yablonskyi.data.AppDatabase
import com.yablonskyi.data.di.AppModule
import com.yablonskyi.domain.backup.BackupAccessGate
import com.yablonskyi.domain.backup.BackupAccessState
import com.yablonskyi.domain.repository.*
import com.yablonskyi.model.backup.*
import com.yablonskyi.model.character.Character
import com.yablonskyi.model.character.RichText
import kotlinx.coroutines.CompletableDeferred
import kotlinx.serialization.json.Json
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.time.Instant
import java.security.MessageDigest
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FullBackupRepositoryTest {
    @get:Rule val temporary = TemporaryFolder()

    @Test fun givenCompleteBackup_whenPreparedThenConfirmed_thenRoundTripsAndConsumesToken() = runTest {
        withRepository { db, repository, staging, _ ->
            val image = temporary.newFile().apply { writeBytes(byteArrayOf(1, 2, 3)) }
            val original = backupFixture().let { fixture ->
                fixture.copy(characters = fixture.characters.map { it.copy(imagePath = image.absolutePath) })
            }
            db.backupDao().replace(original)
            val output = ByteArrayOutputStream()
            assertEquals(BackupResult.Success(Unit), repository.createBackup({ output }, { fail("Successful output removed") }))
            assertTrue(staging.list()!!.isEmpty())
            val prepared = (repository.prepareRestore { ByteArrayInputStream(output.toByteArray()) } as BackupResult.Success).value
            assertEquals(original, db.backupDao().snapshot()) // Preparation must not mutate live data.
            assertEquals(1001, prepared.summary.counts.diceRolls)
            assertEquals(1, prepared.summary.portraitCount) // Shared portrait is stored once.
            assertEquals(Instant.parse("2026-09-21T12:00:00Z"), prepared.summary.createdAt)
            assertEquals("test", prepared.summary.sourceAppVersion)
            assertEquals(BackupResult.Success(Unit), repository.restore(prepared.token))
            val restored = db.backupDao().snapshot()
            assertEquals(original, restored.copy(characters = restored.characters.map { it.copy(imagePath = image.absolutePath) }))
            restored.characters.forEach { assertArrayEquals(byteArrayOf(1, 2, 3), File(it.imagePath!!).readBytes()) }
            assertEquals(BackupResult.Failure(BackupError.EXPIRED_RESTORE), repository.restore(prepared.token))
            assertEquals(BackupProgress.IDLE, repository.progress.value)
            assertTrue(staging.list()!!.isEmpty())
        }
    }

    @Test fun givenMissingPortrait_whenExporting_thenDoesNotOpenDestination() = runTest {
        withRepository { db, repository, staging, _ ->
            db.backupDao().replace(backupFixture())
            var opened = false
            val result = repository.createBackup({ opened = true; ByteArrayOutputStream() }, { fail("Unopened output removed") })
            assertEquals(BackupResult.Failure(BackupError.MISSING_IMAGE), result)
            assertFalse(opened)
            assertTrue(staging.list()!!.isEmpty())
        }
    }

    @Test fun givenOutputCloseFailure_whenExporting_thenReportsFailureAndRemovesPartial() = runTest {
        withRepository { _, repository, staging, _ ->
            var removed = false
            val result = repository.createBackup({ object : ByteArrayOutputStream() {
                override fun close() { throw IOException("close") }
            } }, { removed = true })
            assertEquals(BackupResult.Failure(BackupError.IO_FAILURE), result)
            assertTrue(removed)
            assertTrue(staging.list()!!.isEmpty())
            assertEquals(BackupProgress.IDLE, repository.progress.value)
        }
    }

    @Test fun givenDestinationWriteInProgress_whenDatabaseChanges_thenExportKeepsStagedSnapshot() = runTest {
        withRepository(io = Dispatchers.IO) { db, repository, staging, gate ->
            val original = backupFixture().copy(
                characters = backupFixture().characters.map { it.copy(imagePath = null) },
            )
            db.backupDao().replace(original)
            val writing = CompletableDeferred<Unit>()
            val release = CountDownLatch(1)
            val output = object : ByteArrayOutputStream() {
                override fun write(bytes: ByteArray, offset: Int, length: Int) {
                    writing.complete(Unit)
                    check(release.await(5, TimeUnit.SECONDS)) { "Timed out waiting to finish destination write" }
                    super.write(bytes, offset, length)
                }
            }
            val export = async { repository.createBackup({ output }, {}) }
            writing.await()
            try {
                AppModule.provideCharacterRepository(
                    db, db.spellDao(), db.attackDao(), db.characterDao(), gate,
                ).insertCharacter(Character(id = 100, name = "After snapshot"))
            } finally {
                release.countDown()
            }
            assertEquals(BackupResult.Success(Unit), export.await())
            assertTrue(db.backupDao().snapshot().characters.any { it.id == 100L })

            val prepared = (repository.prepareRestore {
                ByteArrayInputStream(output.toByteArray())
            } as BackupResult.Success).value
            assertEquals(BackupResult.Success(Unit), repository.restore(prepared.token))
            assertEquals(original, db.backupDao().snapshot())
            assertTrue(staging.list()!!.isEmpty())
        }
    }

    @Test fun givenInvalidArchive_whenPreparing_thenLeavesLiveDataUntouched() = runTest {
        withRepository { db, repository, staging, _ ->
            val original = backupFixture()
            db.backupDao().replace(original)
            assertEquals(BackupResult.Failure(BackupError.INVALID_ARCHIVE),
                repository.prepareRestore { ByteArrayInputStream(byteArrayOf(1, 2, 3)) })
            assertEquals(original, db.backupDao().snapshot())
            assertTrue(staging.list()!!.isEmpty())
        }
    }

    @Test fun givenInvalidUnsupportedOrOversizedBackup_whenPreparing_thenReturnsTypedFailureWithoutLiveChanges() = runTest {
        withRepository { db, repository, staging, _ ->
            val original = backupFixture()
            db.backupDao().replace(original)
            val empty = BackupDataV1(
                emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(),
            )
            val dangling = empty.copy(characterSpells = listOf(BackupCharacterSpell(1, 2)))
            val cases = listOf(
                archiveDocument(empty) { it.copy(formatVersion = BackupValidator.VERSION + 1) } to
                    BackupError.UNSUPPORTED_VERSION,
                archiveDocument(empty) {
                    it.copy(counts = it.counts.copy(characters = BackupValidationLimits().characters + 1))
                } to BackupError.SIZE_LIMIT_EXCEEDED,
                archiveDocument(dangling) to BackupError.INVALID_ARCHIVE,
            )

            cases.forEach { (document, expected) ->
                assertEquals(
                    BackupResult.Failure(expected),
                    repository.prepareRestore { ByteArrayInputStream(document) },
                )
                assertEquals(original, db.backupDao().snapshot())
                assertTrue(staging.list()!!.isEmpty())
            }
        }
    }

    @Test fun givenInvalidV2Notes_whenPreparing_thenLeavesLiveDataUntouched() = runTest {
        withRepository { db, repository, staging, _ ->
            val original = backupFixture()
            db.backupDao().replace(original)
            val character = original.toBackup(mapOf("/old/portrait.jpg" to "portrait"))
                .characters.first().copy(imageAssetId = null, notes = listOf(
                    original.characters.first().notes.single().copy(text = RichText(version = 99)),
                ))
            val document = archiveDocument(BackupDataV2(
                listOf(character), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(),
            ))

            assertEquals(BackupResult.Failure(BackupError.INVALID_ARCHIVE),
                repository.prepareRestore { ByteArrayInputStream(document) })
            assertEquals(original, db.backupDao().snapshot())
            assertTrue(staging.list()!!.isEmpty())
        }
    }

    @Test fun givenPreparedRestore_whenReplacedOrDiscarded_thenExpiresTokensAndRemovesStaging() = runTest {
        withRepository { _, repository, staging, _ ->
            val output = ByteArrayOutputStream()
            repository.createBackup({ output }, {})
            suspend fun prepare() = (repository.prepareRestore { ByteArrayInputStream(output.toByteArray()) } as BackupResult.Success).value
            val first = prepare()
            val second = prepare()
            assertEquals(1, staging.list()!!.size)
            assertEquals(BackupResult.Failure(BackupError.EXPIRED_RESTORE), repository.restore(first.token))
            repository.discardPreparedRestore(second.token)
            repository.discardPreparedRestore(second.token)
            assertEquals(BackupResult.Failure(BackupError.EXPIRED_RESTORE), repository.restore(second.token))
            assertTrue(staging.list()!!.isEmpty())
        }
    }

    @Test fun givenPreCommitFailure_whenRestoring_thenPreservesLiveDataAndConsumesToken() = runTest {
        withRepository(
            checkpoint = { boundary ->
                if (boundary == RestoreBoundary.IMAGES_WRITTEN) throw IOException("pre-commit failure")
            },
        ) { db, repository, staging, gate ->
            val source = backupFixture().copy(
                characters = backupFixture().characters.map { it.copy(name = "Source", imagePath = null) },
            )
            db.backupDao().replace(source)
            val output = ByteArrayOutputStream()
            assertEquals(BackupResult.Success(Unit), repository.createBackup({ output }, {}))

            val destination = backupFixture().copy(
                characters = backupFixture().characters.map { it.copy(name = "Destination", imagePath = null) },
                spells = backupFixture().spells + backupFixture().spells.first().copy(spellId = 99, name = "Destination only"),
            )
            db.backupDao().replace(destination)
            val prepared = (repository.prepareRestore {
                ByteArrayInputStream(output.toByteArray())
            } as BackupResult.Success).value

            assertEquals(BackupResult.Failure(BackupError.IO_FAILURE), repository.restore(prepared.token))
            assertEquals(destination, db.backupDao().snapshot())
            assertEquals(BackupAccessState.READY, gate.state.value)
            assertEquals(BackupResult.Failure(BackupError.EXPIRED_RESTORE), repository.restore(prepared.token))
            assertTrue(staging.list()!!.isEmpty())
        }
    }

    @Test fun givenClosedGate_whenExporting_thenRequiresRecoveryBeforeOpeningDestination() = runTest {
        withRepository(recover = false) { _, repository, _, _ ->
            assertEquals(BackupResult.Failure(BackupError.RECOVERY_REQUIRED),
                repository.createBackup({ error("Must not open") }, {}))
            assertEquals(BackupResult.Success(Unit), repository.recover())
            assertEquals(BackupResult.Success(Unit), repository.createBackup({ ByteArrayOutputStream() }, {}))
        }
    }

    @Test fun givenCancellationDuringPreparation_whenStopped_thenCleansStagingAndResetsProgress() = runTest {
        withRepository { _, repository, staging, _ ->
            lateinit var operation: Job
            var closed = false
            val source = object : ByteArrayInputStream(ByteArray(20_000)) {
                override fun read(bytes: ByteArray, offset: Int, length: Int): Int {
                    operation.cancel()
                    return super.read(bytes, offset, length)
                }
                override fun close() { closed = true }
            }
            operation = launch(start = CoroutineStart.LAZY) { repository.prepareRestore { source } }
            operation.start()
            operation.join()
            assertTrue(operation.isCancelled)
            assertTrue(closed)
            assertTrue(staging.list()!!.isEmpty())
            assertEquals(BackupProgress.IDLE, repository.progress.value)
        }
    }

    @Test fun givenPreparedToken_whenRecoveryRuns_thenExpiresItAndPreservesUnrelatedFiles() = runTest {
        withRepository { _, repository, staging, _ ->
            val output = ByteArrayOutputStream()
            repository.createBackup({ output }, {})
            val token = (repository.prepareRestore { ByteArrayInputStream(output.toByteArray()) } as BackupResult.Success).value.token
            val unrelated = File(staging, "keep.txt").apply { writeText("keep") }
            assertEquals(BackupResult.Success(Unit), repository.recover())
            assertEquals(BackupResult.Failure(BackupError.EXPIRED_RESTORE), repository.restore(token))
            assertEquals(listOf(unrelated.name), staging.list()!!.toList())
        }
    }

    @Test fun givenProviderPermissionFailure_whenPreparing_thenReturnsTypedFailureAndCleansStaging() = runTest {
        withRepository { _, repository, staging, _ ->
            assertEquals(BackupResult.Failure(BackupError.PERMISSION_DENIED),
                repository.prepareRestore { throw SecurityException("denied") })
            assertTrue(staging.list()!!.isEmpty())
        }
    }

    @Test fun givenDiskFullCause_whenExporting_thenReturnsStorageFailure() = runTest {
        withRepository { _, repository, staging, _ ->
            val result = repository.createBackup({
                throw IOException("write", android.system.ErrnoException("write", android.system.OsConstants.ENOSPC))
            }, {})
            assertEquals(BackupResult.Failure(BackupError.INSUFFICIENT_STORAGE), result)
            assertTrue(staging.list()!!.isEmpty())
        }
    }

    private suspend fun TestScope.withRepository(
        recover: Boolean = true,
        io: kotlinx.coroutines.CoroutineDispatcher = StandardTestDispatcher(testScheduler),
        checkpoint: (RestoreBoundary) -> Unit = {},
        block: suspend (AppDatabase, FullBackupRepositoryImpl, File, BackupAccessGate) -> Unit,
    ) {
        val db = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), AppDatabase::class.java).build()
        try {
            val gate = BackupAccessGate()
            val files = temporary.newFolder()
            val staging = temporary.newFolder()
            val coordinator = BackupRestoreCoordinator(
                db.backupDao(), gate, RestoreJournalStore(files), io, checkpoint,
            )
            if (recover) coordinator.recover()
            val repository = FullBackupRepositoryImpl(db.backupDao(), gate, coordinator, staging, io,
                "test", { Instant.parse("2026-09-21T12:00:00Z") })
            block(db, repository, staging, gate)
        } finally { db.close() }
    }

    private fun archiveDocument(
        data: BackupDataV1,
        transformManifest: (BackupManifest) -> BackupManifest = { it },
    ): ByteArray {
        val counts = BackupRecordCounts(
            data.characters.size,
            data.attacks.size,
            data.diceRolls.size,
            data.spells.size,
            data.characterSpells.size,
            data.races.size,
            data.classes.size,
        )
        return archiveDocument(Json.encodeToString(data).toByteArray(Charsets.UTF_8), counts, 1, transformManifest)
    }

    private fun archiveDocument(data: BackupDataV2): ByteArray = archiveDocument(
        Json.encodeToString(data).toByteArray(Charsets.UTF_8),
        BackupRecordCounts(data.characters.size, data.attacks.size, data.diceRolls.size, data.spells.size,
            data.characterSpells.size, data.races.size, data.classes.size),
        2,
    )

    private fun archiveDocument(
        dataBytes: ByteArray,
        counts: BackupRecordCounts,
        version: Int,
        transformManifest: (BackupManifest) -> BackupManifest = { it },
    ): ByteArray {
        val manifest = transformManifest(BackupManifest(
            format = BackupValidator.FORMAT,
            formatVersion = version,
            createdAt = "2026-09-21T12:00:00Z",
            sourceAppVersion = "test",
            counts = counts,
            dataSha256 = MessageDigest.getInstance("SHA-256").digest(dataBytes)
                .joinToString("") { "%02x".format(it.toInt() and 255) },
            assets = emptyList(),
        ))
        return ByteArrayOutputStream().also { document ->
            ZipOutputStream(document).use { zip ->
                zip.putNextEntry(ZipEntry("manifest.json"))
                zip.write(Json.encodeToString(manifest).toByteArray(Charsets.UTF_8))
                zip.closeEntry()
                zip.putNextEntry(ZipEntry("data.json"))
                zip.write(dataBytes)
                zip.closeEntry()
            }
        }.toByteArray()
    }
}