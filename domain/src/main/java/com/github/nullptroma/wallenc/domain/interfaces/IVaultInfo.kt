package com.github.nullptroma.wallenc.domain.interfaces

import com.github.nullptroma.wallenc.domain.enums.VaultType
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

sealed interface IVaultInfo {
    val type: VaultType
    val uuid: UUID
    val storages: StateFlow<List<IStorageInfo>?>
    val isAvailable: StateFlow<Boolean>
    val totalSpace: StateFlow<Int?>
    val availableSpace: StateFlow<Int?>
}