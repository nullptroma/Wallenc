package com.github.nullptroma.wallenc.data.vaults.local

import com.github.nullptroma.wallenc.domain.interfaces.IStorage
import com.github.nullptroma.wallenc.domain.interfaces.IStorageAccessor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID


class LocalStorage(
    override val uuid: UUID,
    override val isEncrypted: Boolean,
    absolutePath: String,
    ioDispatcher: CoroutineDispatcher
) : IStorage {
    override val size: StateFlow<Long?>
        get() = accessor.size
    override val numberOfFiles: StateFlow<Int?>
        get() = accessor.numberOfFiles
    override val name: StateFlow<String>
        get() = TODO("Добавить класс в Domain, который с помощью accessor будет читать и сохранять имя в скрытую папку")
    override val isAvailable: StateFlow<Boolean>
        get() = accessor.isAvailable
    override val accessor: IStorageAccessor = LocalStorageAccessor(absolutePath, ioDispatcher)

    override suspend fun rename(newName: String) {
        TODO("Not yet implemented")
    }
}