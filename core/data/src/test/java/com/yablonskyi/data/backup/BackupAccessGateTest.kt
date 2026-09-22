package com.yablonskyi.data.backup

import com.yablonskyi.domain.backup.BackupAccessGate
import com.yablonskyi.domain.backup.BackupAccessState
import com.yablonskyi.domain.backup.BackupRecoveryRequiredException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class BackupAccessGateTest {
    @Test fun givenFreshGate_whenAccessedBeforeRecovery_thenFailsClosed() = runTest {
        val gate = BackupAccessGate()
        assertTrue(runCatching { gate.access { fail("Must not run") } }.exceptionOrNull() is BackupRecoveryRequiredException)
        gate.recover { }
        assertEquals(42, gate.access { gate.access { 42 } })
    }

    @Test fun givenRecoveryInProgress_whenWriterArrives_thenWaitsUntilRecoveryCompletes() = runTest {
        val gate = BackupAccessGate()
        val entered = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        val recovering = async { gate.recover { entered.complete(Unit); release.await() } }
        entered.await()
        val writing = async { gate.access { "written" } }
        testScheduler.runCurrent()
        assertFalse(writing.isCompleted)
        release.complete(Unit)
        recovering.await()
        assertEquals("written", writing.await())
    }

    @Test fun givenRecoveryFailure_whenWriterArrives_thenRemainsClosedUntilRetry() = runTest {
        val gate = BackupAccessGate()
        assertTrue(runCatching { gate.recover { throw IOException("Disk full") } }.isFailure)
        assertEquals(BackupAccessState.RECOVERY_REQUIRED, gate.state.value)
        assertTrue(runCatching { gate.access { fail("Must not run") } }.isFailure)
        gate.recover { }
        assertEquals(BackupAccessState.READY, gate.state.value)
    }
}
