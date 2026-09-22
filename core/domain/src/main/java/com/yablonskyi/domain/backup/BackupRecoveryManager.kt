package com.yablonskyi.domain.backup

import com.yablonskyi.domain.repository.BackupError
import kotlinx.coroutines.flow.StateFlow

interface BackupRecoveryManager {
    val state: StateFlow<BackupRecoveryState>

    /** Safe to retry. Concurrent callers share one serialized recovery attempt. */
    suspend fun recover()
}

sealed interface BackupRecoveryState {
    data object Pending : BackupRecoveryState
    data object Recovering : BackupRecoveryState
    data object Ready : BackupRecoveryState
    data class Failed(val error: BackupError) : BackupRecoveryState
}
