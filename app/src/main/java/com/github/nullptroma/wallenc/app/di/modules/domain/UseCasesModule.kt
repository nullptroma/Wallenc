package com.github.nullptroma.wallenc.app.di.modules.domain

import com.github.nullptroma.wallenc.domain.models.IVaultsManager
import com.github.nullptroma.wallenc.domain.usecases.GetAllRawStoragesUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class UseCasesModule {
    @Provides
    @Singleton
    fun provideGetAllRawStoragesUseCase(vaultsManager: IVaultsManager): GetAllRawStoragesUseCase {
        return GetAllRawStoragesUseCase(vaultsManager)
    }
}