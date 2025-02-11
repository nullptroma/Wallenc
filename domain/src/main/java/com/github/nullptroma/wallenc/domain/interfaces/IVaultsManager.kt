package com.github.nullptroma.wallenc.domain.interfaces

import kotlinx.coroutines.flow.StateFlow

interface IVaultsManager {
    val localVault: IVault
    val unlockManager: IUnlockManager
    val remoteVaults: StateFlow<List<IVault>>
    val allStorages: StateFlow<List<IStorage>>
    val allVaults: StateFlow<List<IVault>>
    fun addYandexVault(email: String, token: String)
}