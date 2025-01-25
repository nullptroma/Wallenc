package com.github.nullptroma.wallenc.domain.interfaces

import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

sealed interface IStorageInfo {
    val uuid: UUID
    val isAvailable: StateFlow<Boolean>
    val size: StateFlow<Long?>
    val numberOfFiles: StateFlow<Int?>
    val metaInfo: StateFlow<IStorageMetaInfo>
    val isVirtualStorage: Boolean
}