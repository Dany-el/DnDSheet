package com.yablonskyi.data.di

import com.yablonskyi.data.language.AndroidAppLanguageManager
import com.yablonskyi.data.language.AndroidNetworkStatusProvider
import com.yablonskyi.data.language.PlayLanguageDownloadRepository
import com.yablonskyi.domain.provider.AppLanguageManager
import com.yablonskyi.domain.provider.NetworkStatusProvider
import com.yablonskyi.domain.repository.LanguageDownloadRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LanguageModule {
    @Binds
    @Singleton
    abstract fun bindLanguageDownloadRepository(
        implementation: PlayLanguageDownloadRepository,
    ): LanguageDownloadRepository

    @Binds
    @Singleton
    abstract fun bindNetworkStatusProvider(
        implementation: AndroidNetworkStatusProvider,
    ): NetworkStatusProvider

    @Binds
    @Singleton
    abstract fun bindAppLanguageManager(
        implementation: AndroidAppLanguageManager,
    ): AppLanguageManager
}
