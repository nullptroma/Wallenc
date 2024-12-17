package com.github.nullptroma.wallenc.data

import com.github.nullptroma.wallenc.domain.models.IStorage
import com.github.nullptroma.wallenc.domain.models.IStorageAccessor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class MockStorage(
    override val size: StateFlow<Int?> = MutableStateFlow(null),
    override val numberOfFiles: StateFlow<Int?> = MutableStateFlow(null),
    override val uuid: UUID,
    override val name: StateFlow<String> = MutableStateFlow(""),
    override val totalSpace: StateFlow<Int?> = MutableStateFlow(null),
    override val availableSpace: StateFlow<Int?> = MutableStateFlow(null),
    override val isAvailable: StateFlow<Boolean> = MutableStateFlow(false),
    override val accessor: IStorageAccessor
) : IStorage {

    override suspend fun rename(newName: String) {
        TODO("Not yet implemented")
    }
}