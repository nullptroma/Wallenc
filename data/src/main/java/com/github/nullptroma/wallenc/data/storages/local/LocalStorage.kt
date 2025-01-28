package com.github.nullptroma.wallenc.data.storages.local

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.nullptroma.wallenc.domain.common.impl.CommonStorageMetaInfo
import com.github.nullptroma.wallenc.domain.datatypes.StorageEncryptionInfo
import com.github.nullptroma.wallenc.domain.interfaces.IStorage
import com.github.nullptroma.wallenc.domain.interfaces.IStorageAccessor
import com.github.nullptroma.wallenc.domain.interfaces.IStorageMetaInfo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.util.UUID


class LocalStorage(
    override val uuid: UUID,
    absolutePath: String,
    private val ioDispatcher: CoroutineDispatcher,
) : IStorage {
    override val size: StateFlow<Long?>
        get() = accessor.size
    override val numberOfFiles: StateFlow<Int?>
        get() = accessor.numberOfFiles

    private val _metaInfo = MutableStateFlow<IStorageMetaInfo>(
        CommonStorageMetaInfo()
    )
    override val metaInfo: StateFlow<IStorageMetaInfo>
        get() = _metaInfo

    override val isAvailable: StateFlow<Boolean>
        get() = accessor.isAvailable
    private val _accessor = LocalStorageAccessor(absolutePath, ioDispatcher)
    override val accessor: IStorageAccessor = _accessor
    override val isVirtualStorage: Boolean = false
    private val metaInfoFileName: String = "$uuid$STORAGE_INFO_FILE_POSTFIX"

    suspend fun init() {
        _accessor.init()
        readMetaInfo()
    }

    private suspend fun readMetaInfo() = withContext(ioDispatcher) {
        var meta: CommonStorageMetaInfo
        var reader: InputStream? = null
        try {
            reader = _accessor.openReadSystemFile(metaInfoFileName)
            meta = jackson.readValue(reader, CommonStorageMetaInfo::class.java)
        }
        catch(e: Exception) {
            // чтение не удалось, значит нужно записать файл
            meta = CommonStorageMetaInfo()
            updateMetaInfo(meta)
        }
        finally {
            reader?.close()
        }
        _metaInfo.value = meta
    }

    private suspend fun updateMetaInfo(meta: IStorageMetaInfo) = withContext(ioDispatcher) {
        val writer = _accessor.openWriteSystemFile(metaInfoFileName)
        try {
            jackson.writeValue(writer, meta)
        }
        catch (e: Exception) {
            throw e
        }
        finally {
            writer.close()
        }
        _metaInfo.value = meta
    }

    override suspend fun rename(newName: String) = withContext(ioDispatcher) {
        val curMeta = metaInfo.value
        updateMetaInfo(CommonStorageMetaInfo(
            encInfo = curMeta.encInfo,
            name = newName
        ))
    }

    override suspend fun setEncInfo(encInfo: StorageEncryptionInfo) = withContext(ioDispatcher) {
        val curMeta = metaInfo.value
        updateMetaInfo(CommonStorageMetaInfo(
            encInfo = encInfo,
            name = curMeta.name
        ))
    }

    companion object {
        const val STORAGE_INFO_FILE_POSTFIX = ".storage-info"
        private val jackson = jacksonObjectMapper().apply { findAndRegisterModules() }
    }
}