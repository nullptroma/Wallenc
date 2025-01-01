package com.github.nullptroma.wallenc.domain.interfaces

import com.github.nullptroma.wallenc.domain.datatypes.EncryptKey
import java.util.UUID

interface IVault : IVaultInfo {
    suspend fun createStorage(): IStorage
    suspend fun createStorage(key: EncryptKey): IStorage
    suspend fun createStorage(key: EncryptKey, uuid: UUID): IStorage
    suspend fun remove(storage: IStorage)
}