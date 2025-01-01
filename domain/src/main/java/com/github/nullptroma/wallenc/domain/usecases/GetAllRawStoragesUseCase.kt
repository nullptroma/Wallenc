package com.github.nullptroma.wallenc.domain.usecases

import com.github.nullptroma.wallenc.domain.interfaces.IVaultsManager

class GetAllRawStoragesUseCase(private val manager: IVaultsManager) {
    //    fun getStoragesFlow() = manager.remoteVaults.combine(manager.localVault) { remote, local ->
//        mutableListOf<IVault>().apply {
//            addAll(remote)
//            add(local)
//        }
//    }
    val localStorages
        get() = manager.localVault.storages
}