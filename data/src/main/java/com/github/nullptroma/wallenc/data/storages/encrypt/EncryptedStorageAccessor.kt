package com.github.nullptroma.wallenc.data.storages.encrypt

import android.util.Log
import com.github.nullptroma.wallenc.domain.common.impl.CommonDirectory
import com.github.nullptroma.wallenc.domain.common.impl.CommonFile
import com.github.nullptroma.wallenc.domain.common.impl.CommonMetaInfo
import com.github.nullptroma.wallenc.domain.datatypes.DataPackage
import com.github.nullptroma.wallenc.domain.datatypes.EncryptKey
import com.github.nullptroma.wallenc.domain.encrypt.Encryptor
import com.github.nullptroma.wallenc.domain.encrypt.EncryptorWithStaticIv
import com.github.nullptroma.wallenc.domain.interfaces.IDirectory
import com.github.nullptroma.wallenc.domain.interfaces.IFile
import com.github.nullptroma.wallenc.domain.interfaces.IMetaInfo
import com.github.nullptroma.wallenc.domain.interfaces.IStorageAccessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import kotlin.io.path.Path
import kotlin.io.path.pathString

class EncryptedStorageAccessor(
    private val source: IStorageAccessor,
    pathIv: ByteArray,
    key: EncryptKey,
    private val scope: CoroutineScope
) : IStorageAccessor, DisposableHandle {
    override val size: StateFlow<Long?> = source.size
    override val numberOfFiles: StateFlow<Int?> = source.numberOfFiles
    override val isAvailable: StateFlow<Boolean> = source.isAvailable

    private val _filesUpdates = MutableSharedFlow<DataPackage<List<IFile>>>()
    override val filesUpdates: SharedFlow<DataPackage<List<IFile>>> = _filesUpdates

    private val _dirsUpdates = MutableSharedFlow<DataPackage<List<IDirectory>>>()
    override val dirsUpdates: SharedFlow<DataPackage<List<IDirectory>>> = _dirsUpdates

    private val dataEncryptor = Encryptor(key.toAesKey())
    private val pathEncryptor = EncryptorWithStaticIv(key.toAesKey(), pathIv)

    init {
        collectSourceState()

        for(i in 1..5) {
            val orig = "/hello/path/test.txt"
            val enc = encryptPath(orig)
            val dec = decryptPath(enc)

            Log.d("MyTag", "Path $orig to $enc to $dec")
        }
    }

    private fun collectSourceState() {
        scope.launch {
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

    private fun encryptPath(pathStr: String): String {
        val path = Path(pathStr)
        val segments = mutableListOf<String>()
        for (segment in path)
            segments.add(pathEncryptor.encryptString(segment.pathString))
        val res = Path("/",*(segments.toTypedArray()))
        return res.pathString
    }

    private fun decryptPath(pathStr: String): String {
        val path = Path(pathStr)
        val segments = mutableListOf<String>()
        for (segment in path)
            segments.add(pathEncryptor.decryptString(segment.pathString))
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

    override suspend fun getFileInfo(path: String): IFile {
        val file = source.getFileInfo(encryptPath(path))
        val meta = decryptMeta(file.metaInfo)
        return CommonFile(meta)
    }

    override suspend fun getDirInfo(path: String): IDirectory {
        val dir = source.getDirInfo(encryptPath(path))
        val meta = decryptMeta(dir.metaInfo)
        return CommonDirectory(meta, dir.elementsCount)
    }

    override suspend fun setHidden(path: String, hidden: Boolean) {
        source.setHidden(encryptPath(path), hidden)
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
        return dataEncryptor.encryptStream(stream)
    }

    override suspend fun openRead(path: String): InputStream {
        val stream = source.openRead(encryptPath(path))
        return dataEncryptor.decryptStream(stream)
    }

    override suspend fun moveToTrash(path: String) {
        source.moveToTrash(encryptPath(path))
    }

    override fun dispose() {
        dataEncryptor.dispose()
    }

}