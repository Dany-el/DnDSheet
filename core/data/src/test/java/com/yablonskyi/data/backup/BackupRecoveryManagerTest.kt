package com.yablonskyi.data.backup

import com.yablonskyi.domain.backup.BackupRecoveryState
import com.yablonskyi.domain.repository.BackupError
import com.yablonskyi.domain.repository.BackupProgress
import com.yablonskyi.domain.repository.BackupResult
import com.yablonskyi.domain.repository.FullBackupRepository
import com.yablonskyi.domain.repository.PreparedRestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.InputStream
import java.io.OutputStream

class BackupRecoveryManagerTest {
    @Test fun givenFailedStartupRecovery_whenRetried_thenBecomesReady() = runTest {
        val repository = FakeFullBackupRepository(
            ArrayDeque(
                listOf(
                    BackupResult.Failure(BackupError.IO_FAILURE),
                    BackupResult.Success(Unit),
                ),
            ),
        )
        val manager = BackupRecoveryManagerImpl(repository)

        assertEquals(BackupRecoveryState.Pending, manager.state.value)
        manager.recover()
        assertEquals(BackupRecoveryState.Failed(BackupError.IO_FAILURE), manager.state.value)
        manager.recover()
        assertEquals(BackupRecoveryState.Ready, manager.state.value)
        manager.recover()
        assertEquals(2, repository.recoveryCount)
    }

    @Test fun givenUnexpectedRecoveryFailure_whenRecovering_thenExposesRetryableFailure() = runTest {
        val repository = FakeFullBackupRepository(ArrayDeque()).apply {
            unexpectedFailure = IllegalStateException("Damaged journal")
        }
        val manager = BackupRecoveryManagerImpl(repository)

        manager.recover()

        assertEquals(
            BackupRecoveryState.Failed(BackupError.RECOVERY_REQUIRED),
            manager.state.value,
        )
    }
}

private class FakeFullBackupRepository(
    private val recoveryResults: ArrayDeque<BackupResult<Unit>>,
) : FullBackupRepository {
    override val progress = MutableStateFlow(BackupProgress.IDLE)
    var recoveryCount = 0
        private set
    var unexpectedFailure: RuntimeException? = null

    override suspend fun recover(): BackupResult<Unit> {
        recoveryCount++
        unexpectedFailure?.let { throw it }
        return recoveryResults.removeFirst()
    }

    override suspend fun createBackup(
        openDestination: () -> OutputStream,
        removePartialDestination: () -> Unit,
    ): BackupResult<Unit> = error("Not used")

    override suspend fun prepareRestore(openSource: () -> InputStream): BackupResult<PreparedRestore> =
        error("Not used")

    override suspend fun restore(token: String): BackupResult<Unit> = error("Not used")

    override suspend fun discardPreparedRestore(token: String) = Unit
}
