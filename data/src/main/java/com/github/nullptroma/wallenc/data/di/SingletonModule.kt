package com.github.nullptroma.wallenc.data.di

import com.github.nullptroma.wallenc.data.TestImpl
import com.github.nullptroma.wallenc.domain.models.IMetaInfo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class SingletonModule {

    @Provides
    fun provideIMeta() : IMetaInfo {
        return TestImpl()
    }
}