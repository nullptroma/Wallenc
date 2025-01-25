package com.github.nullptroma.wallenc.data.vaults

import android.content.Context
import com.github.nullptroma.wallenc.domain.interfaces.IStorage
import com.github.nullptroma.wallenc.domain.interfaces.IVault
import com.github.nullptroma.wallenc.domain.interfaces.IVaultsManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.StateFlow

class VaultsManager(ioDispatcher: CoroutineDispatcher, context: Context) : IVaultsManager {
    override val localVault = LocalVault(ioDispatcher, context)

    override val remoteVaults: StateFlow<List<IVault>>
        get() = TODO("Not yet implemented")
    override val allStorages: StateFlow<List<IStorage>>
        get() = localVault.storages

    override fun addYandexVault(email: String, token: String) {
        TODO("Not yet implemented")
    }

}