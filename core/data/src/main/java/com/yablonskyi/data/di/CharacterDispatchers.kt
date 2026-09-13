package com.yablonskyi.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CharacterIoDispatcher

@Module
@InstallIn(SingletonComponent::class)
object CharacterDispatchers {
    @Provides @CharacterIoDispatcher
    fun io(): CoroutineDispatcher = Dispatchers.IO
}
