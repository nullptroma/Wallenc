package com.github.nullptroma.wallenc.domain.encrypt

import com.github.nullptroma.wallenc.domain.common.impl.CommonDirectory
import com.github.nullptroma.wallenc.domain.common.impl.CommonFile
import com.github.nullptroma.wallenc.domain.common.impl.CommonMetaInfo
import com.github.nullptroma.wallenc.domain.datatypes.DataPackage
import com.github.nullptroma.wallenc.domain.datatypes.EncryptKey
import com.github.nullptroma.wallenc.domain.interfaces.IDirectory
import com.github.nullptroma.wallenc.domain.interfaces.IFile
import com.github.nullptroma.wallenc.domain.interfaces.ILogger
import com.github.nullptroma.wallenc.domain.interfaces.IMetaInfo
import com.github.nullptroma.wallenc.domain.interfaces.IStorageAccessor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
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
        collectSourceState(CoroutineScope(ioDispatcher))
    }

    private fun collectSourceState(coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            launch {
                source.filesUpdates.collect {
                    val files = it.data.map(::decryptEntity)
                    _filesUpdates.emit(DataPackage(
                        data = files,
                        isLoading = it.isLoading,
                        isError = it.isError
                    ))
                }
            }

            launch {
                source.dirsUpdates.collect {
                    val dirs = it.data.map(::decryptEntity)
                    _dirsUpdates.emit(DataPackage(
                        data = dirs,
                        isLoading = it.isLoading,
                        isError = it.isError
                    ))
                }
            }
        }
    }

    private fun encryptEntity(file: IFile): IFile {
        return CommonFile(encryptMeta(file.metaInfo))
    }

    private fun decryptEntity(file: IFile): IFile {
        return CommonFile(decryptMeta(file.metaInfo))
    }

    private fun encryptEntity(dir: IDirectory): IDirectory {
        return CommonDirectory(encryptMeta(dir.metaInfo), dir.elementsCount)
    }

    private fun decryptEntity(dir: IDirectory): IDirectory {
        return CommonDirectory(decryptMeta(dir.metaInfo), dir.elementsCount)
    }

    private fun encryptMeta(meta: IMetaInfo): CommonMetaInfo {
        return CommonMetaInfo(
            size = meta.size,
            isDeleted = meta.isDeleted,
            isHidden = meta.isHidden,
            lastModified = meta.lastModified,
            path = encryptPath(meta.path)
        )
    }

    private fun decryptMeta(meta: IMetaInfo): CommonMetaInfo {
        return CommonMetaInfo(
            size = meta.size,
            isDeleted = meta.isDeleted,
            isHidden = meta.isHidden,
            lastModified = meta.lastModified,
            path = decryptPath(meta.path)
        )
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
        return res.pathString
    }

    private fun decryptPath(pathStr: String): String {
        val path = Path(pathStr)
        val segments = mutableListOf<String>()
        for (segment in path)
            segments.add(decryptString(segment.pathString))
        val res = Path("/",*(segments.toTypedArray()))
        return res.pathString
    }

    override suspend fun getAllFiles(): List<IFile> {
        return source.getAllFiles().map(::decryptEntity)
    }

    override suspend fun getFiles(path: String): List<IFile> {
        return source.getFiles(encryptPath(path)).map(::decryptEntity)
    }

    override fun getFilesFlow(path: String): Flow<DataPackage<List<IFile>>> {
        val flow = source.getFilesFlow(encryptPath(path)).map {
            DataPackage(
                data = it.data.map(::decryptEntity),
                isLoading = it.isLoading,
                isError = it.isError
            )
        }
        return flow
    }

    override suspend fun getAllDirs(): List<IDirectory> {
        return source.getAllDirs().map(::decryptEntity)
    }

    override suspend fun getDirs(path: String): List<IDirectory> {
        return source.getDirs(encryptPath(path)).map(::decryptEntity)
    }

    override fun getDirsFlow(path: String): Flow<DataPackage<List<IDirectory>>> {
        val flow = source.getDirsFlow(encryptPath(path)).map {
            DataPackage(
                data = it.data.map(::decryptEntity),
                isLoading = it.isLoading,
                isError = it.isError
            )
        }
        return flow
    }

    override suspend fun touchFile(path: String) {
        source.touchFile(encryptPath(path))
    }

    override suspend fun touchDir(path: String) {
        source.touchDir(encryptPath(path))
    }

    override suspend fun delete(path: String) {
        source.delete(encryptPath(path))
    }

    override suspend fun openWrite(path: String): OutputStream {
        val stream = source.openWrite(encryptPath(path))
        val iv = IvParameterSpec(Random.nextBytes(IV_LEN))
        stream.write(iv.iv) // Запись инициализационного вектора сырой файл
        val cipher = Cipher.getInstance(AES_FOR_STRINGS)
        cipher.init(Cipher.ENCRYPT_MODE, _secretKey, iv) // инициализация шифратора
        return CipherOutputStream(stream, cipher)
    }

    override suspend fun openRead(path: String): InputStream {
        val stream = source.openRead(encryptPath(path))
        val ivBytes = ByteArray(IV_LEN) // Буфер для 16 байт IV
        val bytesRead = stream.read(ivBytes) // Чтение IV вектора
        if(bytesRead != IV_LEN)
            throw Exception("TODO iv не прочитан")
        val iv = IvParameterSpec(ivBytes)

        val cipher = Cipher.getInstance(AES_FOR_STRINGS)
        cipher.init(Cipher.DECRYPT_MODE, _secretKey, iv)
        return CipherInputStream(stream, cipher)
    }

    override suspend fun moveToTrash(path: String) {
        source.moveToTrash(encryptPath(path))
    }


    companion object {
        private const val IV_LEN = 16
        private const val AES_FOR_STRINGS = "AES/CBC/PKCS5Padding"
    }
}