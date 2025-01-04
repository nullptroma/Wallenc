package com.github.nullptroma.wallenc.domain.interfaces

import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

interface IStorageInfo {
    val size: StateFlow<Long?>
    val numberOfFiles: StateFlow<Int?>
    val uuid: UUID
    val isEncrypted: Boolean
    val name: StateFlow<String>
    val isAvailable: StateFlow<Boolean>
}