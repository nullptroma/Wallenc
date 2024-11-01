package com.github.nullptroma.wallenc.app.di.modules.domain

import com.github.nullptroma.wallenc.domain.models.IMetaInfo
import com.github.nullptroma.wallenc.domain.usecases.TestUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class UseCasesModule {
    var count = 0

    @Provides
    @Singleton
    fun provideTestUseCase(meta: IMetaInfo): TestUseCase {
        return TestUseCase(meta, count++)
    }
}