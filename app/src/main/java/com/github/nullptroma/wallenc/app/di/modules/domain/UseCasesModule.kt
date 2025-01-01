package com.github.nullptroma.wallenc.app.di.modules.domain

import com.github.nullptroma.wallenc.domain.interfaces.IVaultsManager
import com.github.nullptroma.wallenc.domain.usecases.GetAllRawStoragesUseCase
import com.github.nullptroma.wallenc.domain.usecases.ManageLocalVaultUseCase
import com.github.nullptroma.wallenc.domain.usecases.StorageFileManagementUseCase
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

    @Provides
    @Singleton
    fun provideManageLocalVaultUseCase(vaultsManager: IVaultsManager): ManageLocalVaultUseCase {
        return ManageLocalVaultUseCase(vaultsManager)
    }

    @Provides
    @Singleton
    fun provideStorageFileManagementUseCase(): StorageFileManagementUseCase {
        return StorageFileManagementUseCase()
    }
}