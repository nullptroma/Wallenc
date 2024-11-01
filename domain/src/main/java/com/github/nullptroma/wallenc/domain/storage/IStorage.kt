package com.github.nullptroma.wallenc.domain.storage

import kotlinx.coroutines.flow.StateFlow

interface IStorage {
    val size: StateFlow<Int>
    val numberOfFiles: StateFlow<Int>
    val uuid: String
    val name: StateFlow<String>
    val totalSpace: StateFlow<Int?>
    val availableSpace: StateFlow<Int?>
    val isAvailable: StateFlow<Boolean>
    val accessor: IStorageAccessor

    suspend fun rename(newName: String)
}
