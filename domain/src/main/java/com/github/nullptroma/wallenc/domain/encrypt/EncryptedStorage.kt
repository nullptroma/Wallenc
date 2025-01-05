package com.github.nullptroma.wallenc.domain.encrypt

import com.github.nullptroma.wallenc.domain.datatypes.EncryptKey
import com.github.nullptroma.wallenc.domain.datatypes.StorageEncryptionInfo
import com.github.nullptroma.wallenc.domain.interfaces.ILogger
import com.github.nullptroma.wallenc.domain.interfaces.IStorage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class EncryptedStorage(
    source: IStorage,
    key: EncryptKey,
    logger: ILogger,
    ioDispatcher: CoroutineDispatcher,
) : IStorage, DisposableHandle {
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
    override val encInfo: StateFlow<StorageEncryptionInfo>
        get() = TODO("Not yet implemented")
    override val accessor: EncryptedStorageAccessor =
        EncryptedStorageAccessor(source.accessor, key, logger, ioDispatcher)

    override suspend fun rename(newName: String) {
        TODO("Not yet implemented")
    }

    override fun dispose() {
        accessor.dispose()
    }
}