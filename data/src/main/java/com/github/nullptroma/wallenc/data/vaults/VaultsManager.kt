package com.github.nullptroma.wallenc.data.vaults

import android.content.Context
import com.github.nullptroma.wallenc.data.db.app.repository.StorageKeyMapRepository
import com.github.nullptroma.wallenc.data.storages.UnlockManager
import com.github.nullptroma.wallenc.domain.interfaces.IStorage
import com.github.nullptroma.wallenc.domain.interfaces.IUnlockManager
import com.github.nullptroma.wallenc.domain.interfaces.IVault
import com.github.nullptroma.wallenc.domain.interfaces.IVaultsManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class VaultsManager(ioDispatcher: CoroutineDispatcher, context: Context, keyRepo: StorageKeyMapRepository) : IVaultsManager {
    override val localVault = LocalVault(ioDispatcher, context)
    override val unlockManager: IUnlockManager = UnlockManager(
        keymapRepository = keyRepo,
        ioDispatcher = ioDispatcher,
        vaultsManager = this
    )
    override val remoteVaults: StateFlow<List<IVault>>
        get() = TODO("Not yet implemented")
    override val allStorages: StateFlow<List<IStorage>>
        get() = localVault.storages
    override val allVaults: StateFlow<List<IVault>>
        get() = MutableStateFlow(listOf(localVault, unlockManager))


    override fun addYandexVault(email: String, token: String) {
        TODO("Not yet implemented")
    }

}