package com.yablonskyi.data.backup

import com.yablonskyi.data.dao.BackupDao
import com.yablonskyi.domain.backup.BackupAccessGate
import com.yablonskyi.model.backup.BackupValidator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.IOException

/** Recovery engine. Not exposed to UI until all writers and startup use the same gate. */
internal class BackupRestoreCoordinator(
    private val dao: BackupDao,
    private val gate: BackupAccessGate,
    private val journalStore: RestoreJournalStore,
    private val io: CoroutineDispatcher,
    private val checkpoint: (RestoreBoundary) -> Unit = {},
) {
    suspend fun recover() = withContext(io) {
        gate.recover { recoverLocked() }
    }

    suspend fun restore(staged: StagedBackup) = withContext(io) {
        gate.access {
            BackupValidator.validateNormalized(staged.manifest, staged.data)
            check(journalStore.read() == null && dao.committedRestore() == null) { "Pending restore must be recovered" }
            val journal = journalStore.newJournal(staged, dao.characters().mapNotNull { it.imagePath })
            gate.requireRecovery()
            var finishRecovery = false
            var operationFailure: Exception? = null
            try {
                journalStore.write(journal)
                checkpoint(RestoreBoundary.JOURNAL_WRITTEN)
                val context = currentCoroutineContext()
                journalStore.installImages(journal, staged) { context.ensureActive() }
                checkpoint(RestoreBoundary.IMAGES_WRITTEN)
                context.ensureActive()
                dao.commitRestore(staged.data.toRoomSnapshot(journalStore.imagePaths(journal)), journal.operationId)
                checkpoint(RestoreBoundary.DATABASE_COMMITTED)
                finishRecovery = true
            } catch (failure: Exception) {
                operationFailure = failure
                finishRecovery = true
                throw failure
            } finally {
                // Finish committed work or undo pre-commit staging; never turn cancellation into success.
                // Fatal VM errors leave the durable journal for the next process instead.
                if (finishRecovery) withContext(NonCancellable) {
                    try {
                        gate.recover { recoverLocked() }
                    } catch (recoveryFailure: Exception) {
                        val original = operationFailure
                        if (original == null) throw recoveryFailure
                        original.addSuppressed(recoveryFailure)
                    }
                }
            }
        }
    }

    private suspend fun recoverLocked() {
        val journal = journalStore.read()
        val committed = dao.committedRestore()
        if (journal == null) {
            if (committed != null) throw IOException("Committed restore has no recovery journal")
            return
        }
        if (committed != null && committed != journal.operationId) throw IOException("Restore journal and database disagree")
        if (committed == null && journal.phase == RestorePhase.PREPARED) {
            // No content replacement was committed; the old image references remain live.
            journalStore.discardNewImages(journal)
            journalStore.clear()
            return
        }
        journalStore.verifyImages(journal)
        if (journal.phase == RestorePhase.PREPARED) {
            journalStore.write(journal.copy(phase = RestorePhase.APPLIED))
            checkpoint(RestoreBoundary.COMPLETION_RECORDED)
        }
        // APPLIED is durable before removing the Room marker, so this window is unambiguous on restart.
        dao.clearCommitMarker(journal.operationId)
        checkpoint(RestoreBoundary.MARKER_CLEARED)
        journalStore.discardOldImages(journal)
        journalStore.clear()
        checkpoint(RestoreBoundary.JOURNAL_REMOVED)
    }
}

internal enum class RestoreBoundary {
    JOURNAL_WRITTEN, IMAGES_WRITTEN, DATABASE_COMMITTED,
    COMPLETION_RECORDED, MARKER_CLEARED, JOURNAL_REMOVED,
}
