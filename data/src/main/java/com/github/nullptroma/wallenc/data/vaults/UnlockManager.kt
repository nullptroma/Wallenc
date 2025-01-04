package com.github.nullptroma.wallenc.data.vaults

import com.github.nullptroma.wallenc.domain.datatypes.EncryptKey
import com.github.nullptroma.wallenc.domain.interfaces.IStorage
import com.github.nullptroma.wallenc.domain.interfaces.IUnlockManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class UnlockManager: IUnlockManager {
    private val _openedStorages = MutableStateFlow<Map<UUID, IStorage>>(mapOf())
    override val openedStorages: StateFlow<Map<UUID, IStorage>>
        get() = _openedStorages

    override fun open(
        storage: IStorage,
        key: EncryptKey
    ) {
        TODO("Not yet implemented")
    }

    override fun close(storage: IStorage) {
        TODO("Not yet implemented")
    }
}