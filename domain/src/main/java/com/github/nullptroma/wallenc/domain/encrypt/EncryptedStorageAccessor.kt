package com.github.nullptroma.wallenc.domain.encrypt

import com.github.nullptroma.wallenc.domain.datatypes.DataPackage
import com.github.nullptroma.wallenc.domain.datatypes.EncryptKey
import com.github.nullptroma.wallenc.domain.encrypt.entity.EncryptedDirectory
import com.github.nullptroma.wallenc.domain.encrypt.entity.EncryptedFile
import com.github.nullptroma.wallenc.domain.encrypt.entity.EncryptedMetaInfo
import com.github.nullptroma.wallenc.domain.interfaces.IDirectory
import com.github.nullptroma.wallenc.domain.interfaces.IFile
import com.github.nullptroma.wallenc.domain.interfaces.ILogger
import com.github.nullptroma.wallenc.domain.interfaces.IStorageAccessor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.io.path.Path
import kotlin.io.path.pathString
import kotlin.random.Random

class EncryptedStorageAccessor(
    private val source: IStorageAccessor,
    private val key: EncryptKey,
    private val logger: ILogger,
    ioDispatcher: CoroutineDispatcher
) : IStorageAccessor {
    override val size: StateFlow<Long?> = source.size
    override val numberOfFiles: StateFlow<Int?> = source.numberOfFiles
    override val isAvailable: StateFlow<Boolean> = source.isAvailable

    private val _filesUpdates = MutableSharedFlow<DataPackage<List<IFile>>>()
    override val filesUpdates: SharedFlow<DataPackage<List<IFile>>> = _filesUpdates

    private val _dirsUpdates = MutableSharedFlow<DataPackage<List<IDirectory>>>()
    override val dirsUpdates: SharedFlow<DataPackage<List<IDirectory>>> = _dirsUpdates

    private val _secretKey = SecretKeySpec(key.to32Bytes(), "AES")

    init {
        val enc = encryptPath("/hello/world/test.txt")
        val dec = decryptPath(enc)
        collectSourceState(CoroutineScope(ioDispatcher))
    }

    private fun collectSourceState(coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            launch {
                source.filesUpdates.collect {
                    val files = it.data.map {
                        val meta = it.metaInfo
                        EncryptedFile(EncryptedMetaInfo(
                            size = meta.size,
                            isDeleted = meta.isDeleted,
                            isHidden = meta.isHidden,
                            lastModified = meta.lastModified,
                            path = decryptPath(meta.path)
                        ))
                    }
                    _filesUpdates.emit(DataPackage(
                        data = files,
                        isLoading = it.isLoading,
                        isError = it.isError
                    ))
                }
            }

            launch {
                source.dirsUpdates.collect {
                    val dirs = it.data.map {
                        val meta = it.metaInfo
                        EncryptedDirectory(EncryptedMetaInfo(
                            size = meta.size,
                            isDeleted = meta.isDeleted,
                            isHidden = meta.isHidden,
                            lastModified = meta.lastModified,
                            path = decryptPath(meta.path)
                        ), it.elementsCount)
                    }
                    _dirsUpdates.emit(DataPackage(
                        data = dirs,
                        isLoading = it.isLoading,
                        isError = it.isError
                    ))
                }
            }
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun encryptString(str: String): String {
        val cipher = Cipher.getInstance(AES_FOR_STRINGS)
        val iv = IvParameterSpec(Random.nextBytes(IV_LEN))
        cipher.init(Cipher.ENCRYPT_MODE, _secretKey, iv)
        val bytesToEncrypt = iv.iv + str.toByteArray(Charsets.UTF_8)
        val encryptedBytes = cipher.doFinal(bytesToEncrypt)
        return Base64.Default.encode(encryptedBytes).replace("/", ".")
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun decryptString(str: String): String {
        val cipher = Cipher.getInstance(AES_FOR_STRINGS)
        val bytesToDecrypt = Base64.Default.decode(str.replace(".", "/"))
        val iv = IvParameterSpec(bytesToDecrypt.take(IV_LEN).toByteArray())
        cipher.init(Cipher.DECRYPT_MODE, _secretKey, iv)
        val decryptedBytes = cipher.doFinal(bytesToDecrypt.drop(IV_LEN).toByteArray())
        return String(decryptedBytes, Charsets.UTF_8)
    }

    private fun encryptPath(pathStr: String): String {
        val path = Path(pathStr)
        val segments = mutableListOf<String>()
        for (segment in path)
            segments.add(encryptString(segment.pathString))
        val res = Path("/",*(segments.toTypedArray()))
        logger.debug("encryptPath", "$pathStr to $res")
        return res.pathString
    }

    private fun decryptPath(pathStr: String): String {
        val path = Path(pathStr)
        val segments = mutableListOf<String>()
        for (segment in path)
            segments.add(decryptString(segment.pathString))
        val res = Path("/",*(segments.toTypedArray()))
        logger.debug("decryptPath", "$pathStr to $res")
        return res.pathString
    }

    override suspend fun getAllFiles(): List<IFile> {
        TODO("Not yet implemented")
    }

    override suspend fun getFiles(path: String): List<IFile> {
        TODO("Not yet implemented")
    }

    override fun getFilesFlow(path: String): Flow<DataPackage<List<IFile>>> {
        TODO("Not yet implemented")
    }

    override suspend fun getAllDirs(): List<IDirectory> {
        TODO("Not yet implemented")
    }

    override suspend fun getDirs(path: String): List<IDirectory> {
        TODO("Not yet implemented")
    }

    override fun getDirsFlow(path: String): Flow<DataPackage<List<IDirectory>>> {
        TODO("Not yet implemented")
    }

    override suspend fun touchFile(path: String) {
        TODO("Not yet implemented")
    }

    override suspend fun touchDir(path: String) {
        TODO("Not yet implemented")
    }

    override suspend fun delete(path: String) {
        TODO("Not yet implemented")
    }

    override suspend fun openWrite(path: String): OutputStream {
        TODO("Not yet implemented")
    }

    override suspend fun openRead(path: String): InputStream {
        TODO("Not yet implemented")
    }

    override suspend fun moveToTrash(path: String) {
        TODO("Not yet implemented")
    }


    companion object {
        private const val IV_LEN = 16
        private const val AES_FOR_STRINGS = "AES/CBC/PKCS5Padding"
    }
}