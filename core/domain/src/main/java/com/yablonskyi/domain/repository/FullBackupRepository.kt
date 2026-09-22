package com.yablonskyi.domain.repository

import com.yablonskyi.model.backup.BackupRecordCounts
import kotlinx.coroutines.flow.StateFlow
import java.io.InputStream
import java.io.OutputStream
import java.time.Instant

/** Implementations own and close supplied streams; callers open documents outside ViewModels. */
interface FullBackupRepository {
    val progress: StateFlow<BackupProgress>

    /**
     * Stage before opening the destination. Success requires a successful close. Callbacks run on IO;
     * cleanup must target only the newly created backup document, never an existing user file.
     */
    suspend fun createBackup(
        openDestination: () -> OutputStream,
        removePartialDestination: () -> Unit,
    ): BackupResult<Unit>

    /** Stage without live changes. Replaces the previous preparation; tokens expire across process death. */
    suspend fun prepareRestore(openSource: () -> InputStream): BackupResult<PreparedRestore>

    /** Consumes the token after confirmation, even on failure; committed work completes/recovers despite cancellation. */
    suspend fun restore(token: String): BackupResult<Unit>

    suspend fun discardPreparedRestore(token: String)

    /** Completes or rolls back an interrupted restore. Must run before normal content access. */
    suspend fun recover(): BackupResult<Unit>
}

/** Single-use opaque token plus validated data safe to show in a confirmation UI. */
data class PreparedRestore(val token: String, val summary: BackupSummary)

data class BackupSummary(
    val createdAt: Instant,
    val sourceAppVersion: String,
    val counts: BackupRecordCounts,
    val portraitCount: Int,
)

enum class BackupProgress { IDLE, SNAPSHOTTING, WRITING, VALIDATING, RESTORING, RECOVERING }

sealed interface BackupResult<out T> {
    data class Success<T>(val value: T) : BackupResult<T>
    data class Failure(val error: BackupError) : BackupResult<Nothing>
}

enum class BackupError {
    BUSY, UNSUPPORTED_VERSION, INVALID_ARCHIVE, MISSING_IMAGE, SIZE_LIMIT_EXCEEDED,
    PERMISSION_DENIED, INSUFFICIENT_STORAGE, IO_FAILURE, EXPIRED_RESTORE,
    RECOVERY_REQUIRED,
}
