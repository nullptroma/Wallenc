package com.github.nullptroma.wallenc.domain.encrypt

import com.github.nullptroma.wallenc.domain.datatypes.EncryptKey
import com.github.nullptroma.wallenc.domain.interfaces.ILogger
import com.github.nullptroma.wallenc.domain.interfaces.IStorage
import com.github.nullptroma.wallenc.domain.interfaces.IStorageAccessor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class EncryptedStorage(
    source: IStorage,
    key: EncryptKey,
    logger: ILogger,
    ioDispatcher: CoroutineDispatcher,
    override val isEncrypted: Boolean
) : IStorage {
    override val size: StateFlow<Long?>
        get() = TODO("Not yet implemented")
    override val numberOfFiles: StateFlow<Int?>
        get() = TODO("Not yet implemented")
    override val uuid: UUID
        get() = TODO("Not yet implemented")
    override val name: StateFlow<String>
        get() = TODO("Not yet implemented")
    override val isAvailable: StateFlow<Boolean>
        get() = TODO("Not yet implemented")
    override val accessor: IStorageAccessor =
        EncryptedStorageAccessor(source.accessor, key, logger, ioDispatcher)

    override suspend fun rename(newName: String) {
        TODO("Not yet implemented")
    }
}