package com.github.nullptroma.wallenc.data.vaults.local

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

    private val _metaInfo = MutableStateFlow(
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
        get() = accessor.isAvailable
    private val _accessor = LocalStorageAccessor(absolutePath, ioDispatcher)
    override val accessor: IStorageAccessor = _accessor

    private val encInfoFileName: String = "$uuid$ENC_INFO_FILE_POSTFIX"

    suspend fun init() {
        _accessor.init()
        readEncInfo()
    }

    private suspend fun readEncInfo() = withContext(ioDispatcher) {
        var enc: StorageEncryptionInfo? = null
        var reader: InputStream? = null
        try {
            reader = _accessor.openReadSystemFile(encInfoFileName)
            enc = jackson.readValue(reader, StorageEncryptionInfo::class.java)
        }
        catch(e: Exception) {
            // чтение не удалось, значит нужно записать файл
            enc = StorageEncryptionInfo(
                isEncrypted = false,
                encryptedTestData = null
            )
            setEncInfo(enc)
        }
        finally {
            reader?.close()
        }
        _metaInfo.value = _metaInfo.value.copy(encInfo = enc)
    }

    suspend fun setEncInfo(enc: StorageEncryptionInfo) = withContext(ioDispatcher) {
        val writer = _accessor.openWriteSystemFile(encInfoFileName)
        try {
            jackson.writeValue(writer, enc)
        }
        catch (e: Exception) {
            TODO("Это никогда не должно произойти")
        }
        writer.close()
        _metaInfo.value = _metaInfo.value.copy(encInfo = enc)
    }

    override suspend fun rename(newName: String) {
        TODO("Not yet implemented")
    }

    companion object {
        const val ENC_INFO_FILE_POSTFIX = ".enc-info"
        private val jackson = jacksonObjectMapper().apply { findAndRegisterModules() }
    }
}