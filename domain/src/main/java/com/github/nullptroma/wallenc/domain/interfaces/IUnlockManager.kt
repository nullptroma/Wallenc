package com.github.nullptroma.wallenc.domain.interfaces

import com.github.nullptroma.wallenc.domain.datatypes.EncryptKey
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

interface IUnlockManager {
    /**
     * Хранилища, для которых есть ключ шифрования
     */
    val openedStorages: StateFlow<Map<UUID, IStorage>>

    fun open(storage: IStorage, key: EncryptKey)
    fun close(storage: UUID)
}