package com.github.nullptroma.wallenc.domain.encrypt

import com.github.nullptroma.wallenc.domain.datatypes.EncryptKey
import com.github.nullptroma.wallenc.domain.datatypes.StorageEncryptionInfo
import com.github.nullptroma.wallenc.domain.interfaces.ILogger
import com.github.nullptroma.wallenc.domain.interfaces.IStorage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class EncryptedStorage(
    private val source: IStorage,
    key: EncryptKey,
    ioDispatcher: CoroutineDispatcher,
    override val uuid: UUID = UUID.randomUUID()
) : IStorage, DisposableHandle {
    override val size: StateFlow<Long?>
        get() = source.size
    override val numberOfFiles: StateFlow<Int?>
        get() = source.numberOfFiles
    override val name: StateFlow<String>
        get() = TODO("Not yet implemented")
    override val isAvailable: StateFlow<Boolean>
        get() = source.isAvailable
    override val encInfo: StateFlow<StorageEncryptionInfo?>
        get() = MutableStateFlow(
            StorageEncryptionInfo(
                isEncrypted = false,
                encryptedTestData = null
            )
        )
    override val accessor: EncryptedStorageAccessor =
        EncryptedStorageAccessor(source.accessor, key, ioDispatcher)

    override suspend fun rename(newName: String) {
        TODO("Not yet implemented")
    }

    override fun dispose() {
        accessor.dispose()
    }
}