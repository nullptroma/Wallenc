package com.github.nullptroma.wallenc.domain.interfaces

import com.github.nullptroma.wallenc.domain.datatypes.EncryptKey
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

interface IUnlockManager: IVault {
    /**
     * Хранилища, для которых есть ключ шифрования
     */
    val openedStorages: StateFlow<Map<UUID, IStorage>?>

    suspend fun open(storage: IStorage, key: EncryptKey): IStorage
    suspend fun close(storage: IStorage)
    suspend fun close(uuid: UUID): Unit
}