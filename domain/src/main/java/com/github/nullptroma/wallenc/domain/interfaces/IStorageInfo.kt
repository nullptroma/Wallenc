package com.github.nullptroma.wallenc.domain.interfaces

import com.github.nullptroma.wallenc.domain.datatypes.StorageEncryptionInfo
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

sealed interface IStorageInfo {
    val uuid: UUID
    val isAvailable: StateFlow<Boolean>
    val size: StateFlow<Long?>
    val numberOfFiles: StateFlow<Int?>
    val encInfo: StateFlow<StorageEncryptionInfo?>
    val name: StateFlow<String?>
}