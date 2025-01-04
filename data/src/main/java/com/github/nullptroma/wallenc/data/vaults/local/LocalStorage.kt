package com.github.nullptroma.wallenc.data.vaults.local

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.nullptroma.wallenc.domain.datatypes.StorageEncryptionInfo
import com.github.nullptroma.wallenc.domain.interfaces.IStorage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID


class LocalStorage(
    override val uuid: UUID,
    absolutePath: String,
    ioDispatcher: CoroutineDispatcher,
) : IStorage {
    override val size: StateFlow<Long?>
        get() = accessor.size
    override val numberOfFiles: StateFlow<Int?>
        get() = accessor.numberOfFiles
    override val isAvailable: StateFlow<Boolean>
        get() = accessor.isAvailable
    override val accessor = LocalStorageAccessor(absolutePath, ioDispatcher)

    private val _encInfo = MutableStateFlow<StorageEncryptionInfo?>(null)
    override val encInfo: StateFlow<StorageEncryptionInfo?>
        get() = _encInfo
    override val name: StateFlow<String>
        get() = TODO("Добавить класс в Domain, который с помощью accessor будет читать и сохранять имя в скрытую папку")

    private val encInfoFileName: String = "$uuid$ENC_INFO_FILE_POSTFIX"

    suspend fun init() {
        accessor.init()
        readEncInfo()
    }

    private suspend fun readEncInfo() {
        accessor.touchFile(encInfoFileName)
        accessor.setHidden(encInfoFileName, true)
        val reader = accessor.openRead(encInfoFileName)
        var enc: StorageEncryptionInfo? = null
        try {
            enc = _jackson.readValue(reader, StorageEncryptionInfo::class.java)
            reader.close()
        }
        catch(e: Exception) {
            reader.close()
            // чтение не удалось, значит нужно записать файл
            enc = StorageEncryptionInfo(
                isEncrypted = false,
                encryptedTestData = null
            )
            val writer = accessor.openWrite(encInfoFileName)
            try {
                _jackson.writeValue(writer, enc)
            }
            catch (e: Exception) {
                TODO("Это никогда не должно произойти")
            }
            writer.close()
        }
        _encInfo.value = enc
    }

    suspend fun setEncInfo(enc: StorageEncryptionInfo) {
        accessor.touchFile(encInfoFileName)
        accessor.setHidden(encInfoFileName, true)

        val writer = accessor.openWrite(encInfoFileName)
        try {
            _jackson.writeValue(writer, enc)
        }
        catch (e: Exception) {
            TODO("Это никогда не должно произойти")
        }
        writer.close()
        _encInfo.value = enc
    }

    override suspend fun rename(newName: String) {
        TODO("Not yet implemented")
    }

    companion object {
        const val ENC_INFO_FILE_POSTFIX = ".enc-info"
        private val _jackson = jacksonObjectMapper().apply { findAndRegisterModules() }
    }
}