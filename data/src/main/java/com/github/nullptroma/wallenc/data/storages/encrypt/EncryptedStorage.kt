package com.github.nullptroma.wallenc.data.storages.encrypt

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.util.UUID

class EncryptedStorage private constructor(
    private val source: IStorage,
    private val key: EncryptKey,
    ioDispatcher: CoroutineDispatcher,
    override val uuid: UUID = UUID.randomUUID()
) : IStorage, DisposableHandle {
    private val job = Job()
    private val scope = CoroutineScope(ioDispatcher + job)
    private val encInfo =
        source.metaInfo.value.encInfo ?: throw Exception("Storage is not encrypted") // TODO
    private val metaInfoFileName: String = "${uuid.toString().take(8)}$STORAGE_INFO_FILE_POSTFIX"

    override val size: StateFlow<Long?>
        get() = accessor.size
    override val numberOfFiles: StateFlow<Int?>
        get() = accessor.numberOfFiles

    private val _metaInfo = MutableStateFlow<IStorageMetaInfo>(
        CommonStorageMetaInfo()
    )
    override val metaInfo: StateFlow<IStorageMetaInfo>
        get() = _metaInfo
    override val isVirtualStorage: Boolean = true

    override val isAvailable: StateFlow<Boolean>
        get() = source.isAvailable
    override val accessor: EncryptedStorageAccessor =
        EncryptedStorageAccessor(
            source = source.accessor,
            pathIv = encInfo.pathIv,
            key = key,
            systemHiddenDirName = "${uuid.toString().take(8)}$SYSTEM_HIDDEN_DIRNAME_POSTFIX",
            scope = scope
        )

    private suspend fun init() {
        checkKey()
        readMetaInfo()
    }

    private fun checkKey() {
        if (!Encryptor.checkKey(key, encInfo))
            throw Exception("Incorrect key") // TODO
    }

    private suspend fun readMetaInfo() = scope.run {
        var meta: CommonStorageMetaInfo
        var reader: InputStream? = null
        try {
            reader = accessor.openReadSystemFile(metaInfoFileName)
            meta = jackson.readValue(reader, CommonStorageMetaInfo::class.java)
        } catch (e: Exception) {
            // чтение не удалось, значит нужно записать файл
            meta = CommonStorageMetaInfo()
            updateMetaInfo(meta)
        } finally {
            reader?.close()
        }
        _metaInfo.value = meta
    }

    private suspend fun updateMetaInfo(meta: IStorageMetaInfo) = scope.run {
        val writer = accessor.openWriteSystemFile(metaInfoFileName)
        try {
            jackson.writeValue(writer, meta)
        } catch (e: Exception) {
            throw e
        } finally {
            writer.close()
        }
        _metaInfo.value = meta
    }

    override suspend fun rename(newName: String) = scope.run {
        val curMeta = metaInfo.value
        updateMetaInfo(
            CommonStorageMetaInfo(
                encInfo = curMeta.encInfo,
                name = newName
            )
        )
    }

    override suspend fun setEncInfo(encInfo: StorageEncryptionInfo) = scope.run {
        val curMeta = metaInfo.value
        updateMetaInfo(
            CommonStorageMetaInfo(
                encInfo = encInfo,
                name = curMeta.name
            )
        )
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
            uuid: UUID = UUID.randomUUID()
        ): EncryptedStorage = withContext(ioDispatcher) {
            val storage = EncryptedStorage(
                source = source,
                key = key,
                ioDispatcher = ioDispatcher,
                uuid = uuid
            )
            try {
                storage.init()
            } catch (e: Exception) {
                storage.dispose()
                throw e
            }
            return@withContext storage
        }

        private const val SYSTEM_HIDDEN_DIRNAME_POSTFIX = "-enc-dir"
        const val STORAGE_INFO_FILE_POSTFIX = ".enc-meta"
        private val jackson = jacksonObjectMapper().apply { findAndRegisterModules() }
    }
}