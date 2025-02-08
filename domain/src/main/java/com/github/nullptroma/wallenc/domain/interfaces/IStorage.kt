package com.github.nullptroma.wallenc.domain.interfaces

import com.github.nullptroma.wallenc.domain.datatypes.StorageEncryptionInfo
import kotlinx.coroutines.flow.StateFlow
import java.time.Instant
import java.util.UUID

sealed interface IStorageInfo {
    val uuid: UUID
    val isAvailable: StateFlow<Boolean>
    val size: StateFlow<Long?>
    val numberOfFiles: StateFlow<Int?>
    val metaInfo: StateFlow<IStorageMetaInfo>
    val isVirtualStorage: Boolean
}

interface IStorage: IStorageInfo {
    val accessor: IStorageAccessor

    suspend fun rename(newName: String)
    suspend fun setEncInfo(encInfo: StorageEncryptionInfo)
}

interface IStorageMetaInfo {
    val encInfo: StorageEncryptionInfo?
    val name: String?
    val lastModified: Instant
}
