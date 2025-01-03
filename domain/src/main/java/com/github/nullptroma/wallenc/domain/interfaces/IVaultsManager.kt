package com.github.nullptroma.wallenc.domain.interfaces

import kotlinx.coroutines.flow.StateFlow

interface IVaultsManager {
    val localVault: IVault
    val remoteVaults: StateFlow<List<IVault>>

    fun addYandexVault(email: String, token: String)
}