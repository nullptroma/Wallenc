package com.github.nullptroma.wallenc.domain.models

import com.github.nullptroma.wallenc.domain.enums.VaultType
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

interface IVaultInfo {
    val type: VaultType
    val uuid: UUID
    val storages: StateFlow<List<IStorage>>
    val isAvailable: StateFlow<Boolean>
}