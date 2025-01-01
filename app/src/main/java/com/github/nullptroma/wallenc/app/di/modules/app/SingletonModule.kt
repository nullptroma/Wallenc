package com.github.nullptroma.wallenc.app.di.modules.app

import com.github.nullptroma.wallenc.app.Logger
import com.github.nullptroma.wallenc.domain.interfaces.ILogger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class SingletonModule {
    @Provides
    @Singleton
    fun provideLogger(): ILogger {
        return Logger()
    }
}