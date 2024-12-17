package com.github.nullptroma.wallenc.domain.models

import com.github.nullptroma.wallenc.domain.datatypes.EncryptKey
import java.util.UUID

interface IVault : IVaultInfo {
    suspend fun createStorage(name: String): IStorage
    suspend fun createStorage(name: String, key: EncryptKey): IStorage
    suspend fun createStorage(name: String, key: EncryptKey, uuid: UUID): IStorage
    suspend fun remove(storage: IStorage)
}