package com.github.nullptroma.wallenc.domain.interfaces

import com.github.nullptroma.wallenc.domain.datatypes.StorageEncryptionInfo

interface IStorage: IStorageInfo {
    val accessor: IStorageAccessor

    suspend fun rename(newName: String)
    suspend fun setEncInfo(encInfo: StorageEncryptionInfo)
}
