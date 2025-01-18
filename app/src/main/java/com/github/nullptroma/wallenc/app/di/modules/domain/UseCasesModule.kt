package com.github.nullptroma.wallenc.app.di.modules.domain

import com.github.nullptroma.wallenc.data.vaults.UnlockManager
import com.github.nullptroma.wallenc.domain.interfaces.IUnlockManager
import com.github.nullptroma.wallenc.domain.interfaces.IVaultsManager
import com.github.nullptroma.wallenc.domain.usecases.GetOpenedStoragesUseCase
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
    fun provideGetOpenedStoragesUseCase(unlockManager: IUnlockManager): GetOpenedStoragesUseCase {
        return GetOpenedStoragesUseCase(unlockManager)
    }

    @Provides
    @Singleton
    fun provideManageLocalVaultUseCase(vaultsManager: IVaultsManager, unlockManager: IUnlockManager): ManageLocalVaultUseCase {
        return ManageLocalVaultUseCase(vaultsManager, unlockManager)
    }

    @Provides
    @Singleton
    fun provideStorageFileManagementUseCase(): StorageFileManagementUseCase {
        return StorageFileManagementUseCase()
    }
}