package com.yablonskyi.data.di

import androidx.room.Room
import com.yablonskyi.data.AppDatabase
import com.yablonskyi.domain.backup.BackupAccessState
import com.yablonskyi.domain.provider.AppVersionProvider
import com.yablonskyi.domain.repository.BackupError
import com.yablonskyi.domain.repository.BackupResult
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.ByteArrayOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BackupModuleTest {
    @Test
    fun givenProductionProviders_whenRecoveryAndExportRun_thenRepositoryUsesSharedGateAndDatabase() = runTest {
        val context = RuntimeEnvironment.getApplication()
        val database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val gate = BackupModule.provideBackupAccessGate()
            val repository = BackupModule.provideFullBackupRepository(
                context = context,
                database = database,
                gate = gate,
                appVersion = object : AppVersionProvider {
                    override val versionName = "test"
                    override val versionCode = 1L
                },
                io = StandardTestDispatcher(testScheduler),
            )

            assertEquals(
                BackupResult.Failure(BackupError.RECOVERY_REQUIRED),
                repository.createBackup({ ByteArrayOutputStream() }, {}),
            )
            assertEquals(BackupResult.Success(Unit), repository.recover())
            assertEquals(BackupAccessState.READY, gate.state.value)
            assertEquals(
                BackupResult.Success(Unit),
                repository.createBackup({ ByteArrayOutputStream() }, {}),
            )
        } finally {
            database.close()
        }
    }
}
