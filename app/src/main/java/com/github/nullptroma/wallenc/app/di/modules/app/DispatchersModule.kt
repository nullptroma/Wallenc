package com.github.nullptroma.wallenc.app.di.modules.app

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
@Singleton
annotation class MainDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
@Singleton
annotation class IoDispatcher

@Module
@InstallIn(SingletonComponent::class)
class DispatchersModule {
    @MainDispatcher
    @Provides
    @Singleton
    fun providesMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    @IoDispatcher
    @Provides
    @Singleton
    fun providesIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}