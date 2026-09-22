package com.yablonskyi.domain.backup

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/** One application-scoped instance must guard all portable-store writes and sync operations.
 * Starts closed: startup must complete recovery before normal access is allowed.
 * Nested repository calls share the lease; only structured child coroutines may inherit it.
 */
class BackupAccessGate {
    private val mutex = Mutex()
    private val mutableState = MutableStateFlow(BackupAccessState.RECOVERY_REQUIRED)
    val state: StateFlow<BackupAccessState> = mutableState.asStateFlow()

    suspend fun <T> access(block: suspend () -> T): T {
        if (currentCoroutineContext()[Lease]?.gate === this) return block()
        return locked {
            if (mutableState.value != BackupAccessState.READY) throw BackupRecoveryRequiredException()
            block()
        }
    }

    /** The only way to reopen a closed gate. Failures keep it closed for an explicit retry. */
    suspend fun recover(block: suspend () -> Unit) = locked {
        mutableState.value = BackupAccessState.RECOVERING
        var completed = false
        try {
            block()
            completed = true
        } finally {
            mutableState.value = if (completed) BackupAccessState.READY else BackupAccessState.RECOVERY_REQUIRED
        }
    }

    suspend fun requireRecovery() {
        check(currentCoroutineContext()[Lease]?.gate === this) { "A gate lease is required" }
        mutableState.value = BackupAccessState.RECOVERY_REQUIRED
    }

    private suspend fun <T> locked(block: suspend () -> T): T {
        if (currentCoroutineContext()[Lease]?.gate === this) return block()
        return mutex.withLock { withContext(Lease(this)) { block() } }
    }

    private class Lease(val gate: BackupAccessGate) : AbstractCoroutineContextElement(Key) {
        companion object Key : CoroutineContext.Key<Lease>
    }
}

enum class BackupAccessState { RECOVERY_REQUIRED, RECOVERING, READY }

class BackupRecoveryRequiredException : IllegalStateException("Backup recovery must complete before accessing app data")
