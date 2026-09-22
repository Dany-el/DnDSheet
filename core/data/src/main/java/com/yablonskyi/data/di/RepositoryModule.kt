package com.yablonskyi.data.di

import com.yablonskyi.data.provider.AndroidAppVersionProvider
import com.yablonskyi.data.repository.character.CharacterFileRepositoryImpl
import com.yablonskyi.data.repository.character.CharacterImageRepositoryImpl
import com.yablonskyi.data.repository.update.UpdateRepositoryImpl
import com.yablonskyi.data.repository.dice.DiceRollRepositoryImpl
import com.yablonskyi.data.backup.BackupRecoveryManagerImpl
import com.yablonskyi.domain.backup.BackupRecoveryManager
import com.yablonskyi.domain.provider.AppVersionProvider
import com.yablonskyi.domain.repository.CharacterFileRepository
import com.yablonskyi.domain.repository.CharacterImageRepository
import com.yablonskyi.domain.repository.UpdateRepository
import com.yablonskyi.domain.repository.DiceRollRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindBackupRecoveryManager(
        impl: BackupRecoveryManagerImpl,
    ): BackupRecoveryManager

    @Binds
    @Singleton
    abstract fun bindDiceRollRepository(
        impl: DiceRollRepositoryImpl,
    ): DiceRollRepository

    @Binds
    abstract fun bindCharacterImages(
        impl: CharacterImageRepositoryImpl,
    ): CharacterImageRepository

    @Binds
    abstract fun bindCharacterFiles(
        impl: CharacterFileRepositoryImpl,
    ): CharacterFileRepository

    @Binds
    @Singleton
    abstract fun bindAppVersionProvider(
        impl: AndroidAppVersionProvider
    ): AppVersionProvider

    @Binds
    @Singleton
    abstract fun bindUpdateRepository(
        impl: UpdateRepositoryImpl
    ): UpdateRepository
}
