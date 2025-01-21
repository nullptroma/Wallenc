package com.github.nullptroma.wallenc.domain.encrypt

import com.github.nullptroma.wallenc.domain.common.impl.CommonStorageMetaInfo
import com.github.nullptroma.wallenc.domain.datatypes.EncryptKey
import com.github.nullptroma.wallenc.domain.datatypes.StorageEncryptionInfo
import com.github.nullptroma.wallenc.domain.interfaces.IStorage
import com.github.nullptroma.wallenc.domain.interfaces.IStorageMetaInfo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.UUID

class EncryptedStorage(
    private val _source: IStorage,
    key: EncryptKey,
    private val ioDispatcher: CoroutineDispatcher,
    override val uuid: UUID = UUID.randomUUID()
) : IStorage, DisposableHandle {
    override val size: StateFlow<Long?>
        get() = _source.size
    override val numberOfFiles: StateFlow<Int?>
        get() = _source.numberOfFiles

    private val _metaInfo = MutableStateFlow<IStorageMetaInfo>(
        CommonStorageMetaInfo(
            encInfo = StorageEncryptionInfo(
                isEncrypted = false,
                encryptedTestData = null
            ),
            name = null
        )
    )
    override val metaInfo: StateFlow<IStorageMetaInfo>
        get() = _metaInfo

    override val isAvailable: StateFlow<Boolean>
        get() = _source.isAvailable
    override val accessor: EncryptedStorageAccessor =
        EncryptedStorageAccessor(_source.accessor, key, ioDispatcher)

    override suspend fun rename(newName: String) {
        TODO("Not yet implemented")
    }

    override fun dispose() {
        accessor.dispose()
    }
}