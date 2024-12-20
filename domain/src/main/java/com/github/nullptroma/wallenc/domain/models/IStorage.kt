package com.github.nullptroma.wallenc.domain.models

import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

interface IStorage {
    val size: StateFlow<Long?>
    val numberOfFiles: StateFlow<Int?>
    val uuid: UUID
    val name: StateFlow<String>
    val isAvailable: StateFlow<Boolean>
    val accessor: IStorageAccessor

    suspend fun rename(newName: String)
}
