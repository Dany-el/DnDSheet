package com.yablonskyi.ui.di

import com.yablonskyi.domain.provider.ResourceProvider
import com.yablonskyi.ui.provider.AndroidCharacterImageLoader
import com.yablonskyi.ui.provider.AndroidResourceProvider
import com.yablonskyi.ui.provider.CharacterImageLoader
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UiModule {
    @Binds
    @Singleton
    abstract fun bindsResourceProvider(
        impl: AndroidResourceProvider
    ): ResourceProvider

    @Binds
    @Singleton
    abstract fun bindsCharacterImageLoader(
        impl: AndroidCharacterImageLoader
    ): CharacterImageLoader
}
