package com.github.nullptroma.wallenc.data.storages.encrypt

import com.github.nullptroma.wallenc.data.db.app.repository.StorageMetaInfoRepository
import com.github.nullptroma.wallenc.domain.common.impl.CommonStorageMetaInfo
import com.github.nullptroma.wallenc.domain.datatypes.EncryptKey
import com.github.nullptroma.wallenc.domain.datatypes.StorageEncryptionInfo
import com.github.nullptroma.wallenc.domain.encrypt.Encryptor
import com.github.nullptroma.wallenc.domain.interfaces.IStorage
import com.github.nullptroma.wallenc.domain.interfaces.IStorageMetaInfo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.util.UUID

class EncryptedStorage private constructor(
    private val source: IStorage,
    private val key: EncryptKey,
    ioDispatcher: CoroutineDispatcher,
    private val metaInfoProvider: StorageMetaInfoRepository.SingleStorageMetaInfoProvider,
    override val uuid: UUID = UUID.randomUUID()
) : IStorage, DisposableHandle {
    private val job = Job()
    private val scope = CoroutineScope(ioDispatcher + job)
    private val encInfo = source.metaInfo.value.encInfo ?: throw Exception("Storage is not encrypted") // TODO

    override val size: StateFlow<Long?>
        get() = source.size
    override val numberOfFiles: StateFlow<Int?>
        get() = source.numberOfFiles

    private val _metaInfo = MutableStateFlow<IStorageMetaInfo>(
        CommonStorageMetaInfo()
    )
    override val metaInfo: StateFlow<IStorageMetaInfo>
        get() = _metaInfo
    override val isVirtualStorage: Boolean = true

    override val isAvailable: StateFlow<Boolean>
        get() = source.isAvailable
    override val accessor: EncryptedStorageAccessor =
        EncryptedStorageAccessor(source.accessor, encInfo.pathIv, key, scope)

    private suspend fun init() {
        checkKey()
        readMeta()
    }

    private fun checkKey() {
        if(!Encryptor.checkKey(key, encInfo))
            throw Exception("Incorrect key") // TODO
    }

    private suspend fun readMeta() = scope.run {
        var meta = metaInfoProvider.get()
        if(meta == null) {
            meta = CommonStorageMetaInfo()
            metaInfoProvider.set(meta)
        }
        _metaInfo.value = meta
    }

    override suspend fun rename(newName: String) = scope.run {
        val cur = _metaInfo.value
        val newMeta = CommonStorageMetaInfo(
            encInfo = cur.encInfo,
            name = newName
        )
        _metaInfo.value = newMeta
        metaInfoProvider.set(newMeta)
    }

    override suspend fun setEncInfo(encInfo: StorageEncryptionInfo) = scope.run {
        val cur = _metaInfo.value
        val newMeta = CommonStorageMetaInfo(
            encInfo = encInfo,
            name = cur.name
        )
        _metaInfo.value = newMeta
        metaInfoProvider.set(newMeta)
    }

    override fun dispose() {
        accessor.dispose()
        job.cancel()
    }

    companion object {
        suspend fun create(
            source: IStorage,
            key: EncryptKey,
            ioDispatcher: CoroutineDispatcher,
            metaInfoProvider: StorageMetaInfoRepository.SingleStorageMetaInfoProvider,
            uuid: UUID = UUID.randomUUID()
        ): EncryptedStorage = withContext(ioDispatcher) {
            val storage = EncryptedStorage(
                source = source,
                key = key,
                ioDispatcher = ioDispatcher,
                metaInfoProvider = metaInfoProvider,
                uuid = uuid
            )
            try {
                storage.init()
            }
            catch (e: Exception) {
                storage.dispose()
                throw e
            }
            return@withContext storage
        }
    }
}