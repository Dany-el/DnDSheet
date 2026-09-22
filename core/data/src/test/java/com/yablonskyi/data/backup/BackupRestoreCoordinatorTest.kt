package com.yablonskyi.data.backup

import androidx.room.Room
import com.yablonskyi.data.AppDatabase
import com.yablonskyi.data.entity.CharacterEntity
import com.yablonskyi.data.entity.RestoreCommitEntity
import com.yablonskyi.domain.backup.BackupAccessGate
import com.yablonskyi.domain.backup.BackupAccessState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File
import java.io.IOException
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BackupRestoreCoordinatorTest {
    @get:Rule val temporary = TemporaryFolder()

    @Test fun givenInterruptionAtEachBoundary_whenRestarted_thenRollsBackOrCompletes() = runTest {
        RestoreBoundary.entries.forEach { boundary ->
            val directory = temporary.newFolder()
            fun openDatabase() = Room.databaseBuilder(
                RuntimeEnvironment.getApplication(),
                AppDatabase::class.java,
                File(directory, "restore-test.db").absolutePath,
            ).build()
            var db = openDatabase()
            try {
                val oldImage = File(directory, "char_img_old.jpg").apply { writeBytes(byteArrayOf(9, 8)) }
                val fixture = backupFixture()
                val original = fixture.copy(
                    characters = fixture.characters.map { it.copy(imagePath = oldImage.absolutePath) },
                )
                db.backupDao().replace(original)
                val gate = BackupAccessGate()
                val journal = RestoreJournalStore(directory)
                val coordinator = BackupRestoreCoordinator(
                    db.backupDao(), gate, journal, StandardTestDispatcher(testScheduler),
                ) { reached ->
                    if (reached == boundary) throw SimulatedProcessDeath()
                }
                coordinator.recover()
                assertTrue(
                    runCatching { coordinator.restore(staged(directory)) }.exceptionOrNull() is SimulatedProcessDeath,
                )

                db.close()
                db = openDatabase()
                val restartedGate = BackupAccessGate()
                val restarted = BackupRestoreCoordinator(
                    db.backupDao(), restartedGate, RestoreJournalStore(directory),
                    StandardTestDispatcher(testScheduler),
                )
                restarted.recover()
                restarted.recover()

                assertEquals(BackupAccessState.READY, restartedGate.state.value)
                assertNull(db.backupDao().committedRestore())
                assertNull(journal.read())
                if (boundary < RestoreBoundary.DATABASE_COMMITTED) {
                    assertEquals(original, db.backupDao().snapshot())
                    assertTrue(oldImage.exists())
                    assertTrue(directory.listFiles()!!.none { it.name.startsWith("char_img_restore_") })
                } else {
                    assertFalse(oldImage.exists())
                    val snapshot = db.backupDao().snapshot()
                    assertEquals("Restored", snapshot.characters.first().name)
                    assertEquals(1001, snapshot.diceRolls.size)
                    snapshot.characters.forEach {
                        assertArrayEquals(byteArrayOf(1, 2, 3), File(it.imagePath!!).readBytes())
                    }
                }
            } finally {
                db.close()
            }
        }
    }

    @Test fun givenCancellationBeforeOrAfterCommit_whenRestoreStops_thenRecoversAndPropagatesCancellation() = runTest {
        listOf(RestoreBoundary.IMAGES_WRITTEN, RestoreBoundary.DATABASE_COMMITTED).forEach { boundary ->
            val directory = temporary.newFolder()
            val db = Room.inMemoryDatabaseBuilder(
                RuntimeEnvironment.getApplication(), AppDatabase::class.java,
            ).build()
            try {
                val original = backupFixture()
                db.backupDao().replace(original)
                val gate = BackupAccessGate()
                lateinit var operation: Deferred<Unit>
                val coordinator = BackupRestoreCoordinator(
                    db.backupDao(), gate, RestoreJournalStore(directory),
                    StandardTestDispatcher(testScheduler),
                ) { reached ->
                    if (reached == boundary) operation.cancel(CancellationException("Canceled"))
                }
                coordinator.recover()
                operation = async(start = CoroutineStart.LAZY) { coordinator.restore(staged(directory)) }
                operation.start()

                assertTrue(runCatching { operation.await() }.exceptionOrNull() is CancellationException)
                operation.join()
                assertEquals(BackupAccessState.READY, gate.state.value)
                assertNull(db.backupDao().committedRestore())
                if (boundary == RestoreBoundary.IMAGES_WRITTEN) {
                    assertEquals(original, db.backupDao().snapshot())
                } else {
                    assertEquals("Restored", db.backupDao().snapshot().characters.first().name)
                }
            } finally {
                db.close()
            }
        }
    }

    @Test fun givenCommittedRestoreWithDamagedImage_whenRecovering_thenRetainsMarkerAndBlocksAccess() = runTest {
        val directory = temporary.newFolder()
        val db = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(), AppDatabase::class.java,
        ).build()
        try {
            val gate = BackupAccessGate()
            val store = RestoreJournalStore(directory)
            val coordinator = BackupRestoreCoordinator(
                db.backupDao(), gate, store, StandardTestDispatcher(testScheduler),
            ) { if (it == RestoreBoundary.DATABASE_COMMITTED) throw SimulatedProcessDeath() }
            coordinator.recover()
            assertTrue(
                runCatching { coordinator.restore(staged(directory)) }.exceptionOrNull() is SimulatedProcessDeath,
            )
            File(store.imagePaths(store.read()!!).values.first()).writeBytes(byteArrayOf(9))

            val recovering = BackupRestoreCoordinator(
                db.backupDao(), gate, store, StandardTestDispatcher(testScheduler),
            )
            assertTrue(runCatching { recovering.recover() }.exceptionOrNull() is IOException)
            assertEquals(BackupAccessState.RECOVERY_REQUIRED, gate.state.value)
            assertNotNull(db.backupDao().committedRestore())
            assertNotNull(store.read())
        } finally {
            db.close()
        }
    }

    @Test fun givenStagedImageChangedAfterValidation_whenRestoring_thenRollsBackAndRemovesNewImages() = runTest {
        val directory = temporary.newFolder()
        val db = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(), AppDatabase::class.java,
        ).build()
        try {
            val original = backupFixture()
            db.backupDao().replace(original)
            val gate = BackupAccessGate()
            val store = RestoreJournalStore(directory)
            val coordinator = BackupRestoreCoordinator(
                db.backupDao(), gate, store, StandardTestDispatcher(testScheduler),
            )
            coordinator.recover()
            val staged = staged(directory)
            staged.images.getValue("portrait").writeBytes(byteArrayOf(9))

            assertTrue(runCatching { coordinator.restore(staged) }.exceptionOrNull() is IOException)

            assertEquals(original, db.backupDao().snapshot())
            assertEquals(BackupAccessState.READY, gate.state.value)
            assertNull(db.backupDao().committedRestore())
            assertNull(store.read())
            assertTrue(directory.listFiles()!!.none { it.name.startsWith("char_img_restore_") })
        } finally {
            db.close()
        }
    }

    @Test fun givenManagedUnmanagedAndExternalOldImages_whenRestoreCompletes_thenDeletesOnlyManagedOldImage() = runTest {
        val directory = temporary.newFolder()
        val outsideDirectory = temporary.newFolder()
        val db = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(), AppDatabase::class.java,
        ).build()
        try {
            val managed = File(directory, "char_img_old.jpg").apply { writeBytes(byteArrayOf(1)) }
            val unmanaged = File(directory, "portrait.jpg").apply { writeBytes(byteArrayOf(2)) }
            val external = File(outsideDirectory, "char_img_external.jpg").apply { writeBytes(byteArrayOf(3)) }
            val fixture = backupFixture()
            db.backupDao().replace(fixture.copy(
                characters = listOf(
                    fixture.characters[0].copy(imagePath = managed.absolutePath),
                    fixture.characters[1].copy(imagePath = unmanaged.absolutePath),
                    CharacterEntity(id = 99, name = "External", imagePath = external.absolutePath),
                ),
            ))
            val gate = BackupAccessGate()
            val store = RestoreJournalStore(directory)
            val coordinator = BackupRestoreCoordinator(
                db.backupDao(), gate, store, StandardTestDispatcher(testScheduler),
            )
            coordinator.recover()

            coordinator.restore(staged(directory))

            assertFalse(managed.exists())
            assertTrue(unmanaged.exists())
            assertTrue(external.exists())
            assertNull(db.backupDao().committedRestore())
            assertNull(store.read())
            db.backupDao().snapshot().characters.forEach {
                assertTrue(File(checkNotNull(it.imagePath)).canonicalFile.parentFile == directory.canonicalFile)
            }
        } finally {
            db.close()
        }
    }

    @Test fun givenCommitMarkerWithoutJournal_whenRecovering_thenKeepsMarkerAndGateClosed() = runTest {
        val directory = temporary.newFolder()
        val db = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(), AppDatabase::class.java,
        ).build()
        try {
            val original = backupFixture()
            db.backupDao().replace(original)
            db.backupDao().insertCommitMarker(RestoreCommitEntity(operationId = "orphan"))
            val gate = BackupAccessGate()
            val coordinator = BackupRestoreCoordinator(
                db.backupDao(), gate, RestoreJournalStore(directory),
                StandardTestDispatcher(testScheduler),
            )

            assertTrue(runCatching { coordinator.recover() }.exceptionOrNull() is IOException)

            assertEquals(original, db.backupDao().snapshot())
            assertEquals("orphan", db.backupDao().committedRestore())
            assertEquals(BackupAccessState.RECOVERY_REQUIRED, gate.state.value)
        } finally {
            db.close()
        }
    }

    @Test fun givenJournalAndDifferentCommitMarker_whenRecovering_thenKeepsDurableStateAndGateClosed() = runTest {
        val directory = temporary.newFolder()
        val db = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(), AppDatabase::class.java,
        ).build()
        try {
            val original = backupFixture()
            db.backupDao().replace(original)
            val store = RestoreJournalStore(directory)
            val journal = store.newJournal(staged(directory), emptyList())
            store.write(journal)
            db.backupDao().insertCommitMarker(RestoreCommitEntity(operationId = "different"))
            val gate = BackupAccessGate()
            val coordinator = BackupRestoreCoordinator(
                db.backupDao(), gate, store, StandardTestDispatcher(testScheduler),
            )

            assertTrue(runCatching { coordinator.recover() }.exceptionOrNull() is IOException)

            assertEquals(original, db.backupDao().snapshot())
            assertEquals("different", db.backupDao().committedRestore())
            assertEquals(journal, store.read())
            assertEquals(BackupAccessState.RECOVERY_REQUIRED, gate.state.value)
        } finally {
            db.close()
        }
    }

    @Test fun givenCorruptJournal_whenRecovering_thenLeavesDataUntouchedAndGateClosed() = runTest {
        val directory = temporary.newFolder()
        val db = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(), AppDatabase::class.java,
        ).build()
        try {
            val original = backupFixture()
            db.backupDao().replace(original)
            File(directory, "restore-journal.json").writeText("{broken")
            val gate = BackupAccessGate()
            val coordinator = BackupRestoreCoordinator(
                db.backupDao(), gate, RestoreJournalStore(directory),
                StandardTestDispatcher(testScheduler),
            )

            assertTrue(runCatching { coordinator.recover() }.isFailure)
            assertEquals(BackupAccessState.RECOVERY_REQUIRED, gate.state.value)
            assertEquals(original, db.backupDao().snapshot())
        } finally {
            db.close()
        }
    }

    @Test fun givenPreparedJournal_whenPersisted_thenContainsNoUserPreferences() = runTest {
        val directory = temporary.newFolder()
        val db = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(), AppDatabase::class.java,
        ).build()
        try {
            val coordinator = BackupRestoreCoordinator(
                db.backupDao(), BackupAccessGate(), RestoreJournalStore(directory),
                StandardTestDispatcher(testScheduler),
            ) { if (it == RestoreBoundary.JOURNAL_WRITTEN) throw SimulatedProcessDeath() }
            coordinator.recover()
            assertTrue(
                runCatching { coordinator.restore(staged(directory)) }.exceptionOrNull() is SimulatedProcessDeath,
            )

            val journalText = File(directory, "restore-journal.json").readText()
            assertFalse(journalText.contains("preferences", ignoreCase = true))
            assertFalse(journalText.contains("language", ignoreCase = true))
            assertFalse(journalText.contains("theme", ignoreCase = true))
            assertFalse(journalText.contains("listView", ignoreCase = true))
        } finally {
            db.close()
        }
    }

    private suspend fun staged(parent: File): StagedBackup {
        val image = File(parent, "source.jpg").apply { writeBytes(byteArrayOf(1, 2, 3)) }
        val fixture = backupFixture()
        val data = fixture.copy(
            characters = fixture.characters.map { it.copy(name = "Restored") },
        ).toBackup(mapOf("/old/portrait.jpg" to "portrait"))
        val codec = BackupArchiveCodec()
        return codec.read(
            codec.write(data, mapOf("portrait" to image), parent, "1", Instant.now()),
            parent,
        )
    }

    private class SimulatedProcessDeath : Error()
}
