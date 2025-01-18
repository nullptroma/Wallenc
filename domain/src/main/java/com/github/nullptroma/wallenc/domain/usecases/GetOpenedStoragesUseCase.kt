package com.github.nullptroma.wallenc.domain.usecases

import com.github.nullptroma.wallenc.domain.interfaces.IStorageInfo
import com.github.nullptroma.wallenc.domain.interfaces.IUnlockManager
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

class GetOpenedStoragesUseCase(private val unlockManager: IUnlockManager) {
    val openedStorages: StateFlow<Map<UUID, IStorageInfo>?>
        get() = unlockManager.openedStorages
}