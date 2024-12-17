package com.github.nullptroma.wallenc.domain.usecases

import com.github.nullptroma.wallenc.domain.models.IVault
import com.github.nullptroma.wallenc.domain.models.IVaultsManager
import kotlinx.coroutines.flow.combine

class GetAllRawStoragesUseCase(val manager: IVaultsManager) {
    fun getStoragesFlow() = manager.remoteVaults.combine(manager.localVault) { remote, local ->
        mutableListOf<IVault>().apply {
            addAll(remote)
            add(local)
        }
    }
}