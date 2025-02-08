package com.github.nullptroma.wallenc.domain.usecases

import com.github.nullptroma.wallenc.domain.datatypes.EncryptKey
import com.github.nullptroma.wallenc.domain.encrypt.Encryptor
import com.github.nullptroma.wallenc.domain.interfaces.IStorage
import com.github.nullptroma.wallenc.domain.interfaces.IStorageInfo
import com.github.nullptroma.wallenc.domain.interfaces.IUnlockManager
import com.github.nullptroma.wallenc.domain.interfaces.IVaultsManager
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

class ManageLocalVaultUseCase(private val manager: IVaultsManager, private val unlockManager: IUnlockManager) {
    val localStorages: StateFlow<List<IStorageInfo>>
        get() = manager.localVault.storages

    suspend fun createStorage() {
        manager.localVault.createStorage()
    }

    suspend fun remove(storage: IStorageInfo) {
        when(storage) {
            is IStorage -> manager.localVault.remove(storage)
        }
    }
}