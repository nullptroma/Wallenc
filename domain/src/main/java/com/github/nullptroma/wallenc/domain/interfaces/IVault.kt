package com.github.nullptroma.wallenc.domain.interfaces

import com.github.nullptroma.wallenc.domain.datatypes.StorageEncryptionInfo
import kotlinx.coroutines.flow.StateFlow

interface IVault : IVaultInfo {
    override val storages: StateFlow<List<IStorage>?>

    suspend fun createStorage(): IStorage
    suspend fun createStorage(enc: StorageEncryptionInfo): IStorage
    suspend fun remove(storage: IStorage)
}