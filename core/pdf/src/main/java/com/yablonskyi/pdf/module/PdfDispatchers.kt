package com.yablonskyi.pdf.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PdfIoDispatcher

@Module
@InstallIn(SingletonComponent::class)
object PdfDispatchers {
    @Provides
    @PdfIoDispatcher
    fun ioDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
