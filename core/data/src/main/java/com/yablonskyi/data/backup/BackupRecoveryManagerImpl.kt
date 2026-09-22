package com.yablonskyi.data.backup

import com.yablonskyi.domain.backup.BackupRecoveryManager
import com.yablonskyi.domain.backup.BackupRecoveryState
import com.yablonskyi.domain.repository.BackupError
import com.yablonskyi.domain.repository.BackupResult
import com.yablonskyi.domain.repository.FullBackupRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRecoveryManagerImpl @Inject constructor(
    private val repository: FullBackupRepository,
) : BackupRecoveryManager {
    private val mutex = Mutex()
    private val mutableState = MutableStateFlow<BackupRecoveryState>(BackupRecoveryState.Pending)
    override val state = mutableState.asStateFlow()

    override suspend fun recover() = mutex.withLock {
        if (mutableState.value == BackupRecoveryState.Ready) return@withLock
        mutableState.value = BackupRecoveryState.Recovering
        try {
            mutableState.value = when (val result = repository.recover()) {
                is BackupResult.Success -> BackupRecoveryState.Ready
                is BackupResult.Failure -> BackupRecoveryState.Failed(result.error)
            }
        } catch (cancelled: CancellationException) {
            mutableState.value = BackupRecoveryState.Pending
            throw cancelled
        } catch (_: RuntimeException) {
            mutableState.value = BackupRecoveryState.Failed(BackupError.RECOVERY_REQUIRED)
        }
    }
}
