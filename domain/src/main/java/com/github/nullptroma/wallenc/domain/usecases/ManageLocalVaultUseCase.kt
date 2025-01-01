package com.github.nullptroma.wallenc.domain.usecases

import com.github.nullptroma.wallenc.domain.interfaces.IVaultsManager

class ManageLocalVaultUseCase(private val manager: IVaultsManager) {
    val localStorages
        get() = manager.localVault.storages

    suspend fun createStorage() {
        manager.localVault.createStorage()
    }
}