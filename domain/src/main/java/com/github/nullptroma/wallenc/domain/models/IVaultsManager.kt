package com.github.nullptroma.wallenc.domain.models

import kotlinx.coroutines.flow.StateFlow

interface IVaultsManager {
    val localVault: IVault
    val remoteVaults: StateFlow<List<IVault>>

    fun addYandexVault(email: String, token: String)
}