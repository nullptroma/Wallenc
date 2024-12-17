package com.github.nullptroma.wallenc.domain.usecases

import com.github.nullptroma.wallenc.domain.models.IVaultsManager

class GetAllRawStoragesUseCase(private val manager: IVaultsManager) {
    //    fun getStoragesFlow() = manager.remoteVaults.combine(manager.localVault) { remote, local ->
//        mutableListOf<IVault>().apply {
//            addAll(remote)
//            add(local)
//        }
//    }
    val localStorage
        get() = manager.localVault
}