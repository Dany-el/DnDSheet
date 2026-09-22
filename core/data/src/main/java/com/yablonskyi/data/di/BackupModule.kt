package com.yablonskyi.data.di

import android.content.Context
import com.yablonskyi.data.AppDatabase
import com.yablonskyi.data.backup.BackupArchiveCodec
import com.yablonskyi.data.backup.BackupRestoreCoordinator
import com.yablonskyi.data.backup.FullBackupRepositoryImpl
import com.yablonskyi.data.backup.RestoreJournalStore
import com.yablonskyi.domain.backup.BackupAccessGate
import com.yablonskyi.domain.provider.AppVersionProvider
import com.yablonskyi.domain.repository.FullBackupRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import java.io.File
import java.time.Instant
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BackupModule {
    @Provides
    @Singleton
    fun provideBackupAccessGate() = BackupAccessGate()

    @Provides
    @Singleton
    fun provideFullBackupRepository(
        @ApplicationContext context: Context,
        database: AppDatabase,
        gate: BackupAccessGate,
        appVersion: AppVersionProvider,
        @CharacterIoDispatcher io: CoroutineDispatcher,
    ): FullBackupRepository {
        val coordinator = BackupRestoreCoordinator(
            database.backupDao(),
            gate,
            RestoreJournalStore(context.filesDir),
            io,
        )
        return FullBackupRepositoryImpl(
            database.backupDao(),
            gate,
            coordinator,
            File(context.cacheDir, "full-backup"),
            io,
            appVersion.versionName,
            Instant::now,
            BackupArchiveCodec(),
        )
    }
}
