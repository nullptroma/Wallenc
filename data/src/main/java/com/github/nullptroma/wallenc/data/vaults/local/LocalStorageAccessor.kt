package com.github.nullptroma.wallenc.data.vaults.local

import com.github.nullptroma.wallenc.domain.datatypes.DataPackage
import com.github.nullptroma.wallenc.domain.models.IDirectory
import com.github.nullptroma.wallenc.domain.models.IFile
import com.github.nullptroma.wallenc.domain.models.IStorageAccessor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import kotlin.io.path.Path
import kotlin.io.path.fileSize

class LocalStorageAccessor(
    private val absolutePath: String,
    private val ioDispatcher: CoroutineDispatcher
) : IStorageAccessor {
    private val _size = MutableStateFlow<Long?>(null)
    private val _numberOfFiles = MutableStateFlow<Int?>(null)
    private val _isAvailable = MutableStateFlow(false)

    override val size: StateFlow<Long?>
        get() = _size
    override val numberOfFiles: StateFlow<Int?>
        get() = _numberOfFiles
    override val isAvailable: StateFlow<Boolean>
        get() = _isAvailable
    override val filesUpdates: SharedFlow<DataPackage<IFile>>
        get() = TODO("Not yet implemented")
    override val dirsUpdates: SharedFlow<DataPackage<IDirectory>>
        get() = TODO("Not yet implemented")

    init {
        CoroutineScope(ioDispatcher).launch {
            scanStorage()
        }
    }

    private fun checkAvailable(): Boolean {
        _isAvailable.value = File(absolutePath).exists()
        return _isAvailable.value
    }

    private fun forAllFiles(dir: File, callback: (File) -> Unit) {
        if (dir.exists() == false)
            return
        callback(dir)

        val nextDirs = dir.listFiles()
        if (nextDirs != null) {
            for (nextDir in nextDirs) {
                forAllFiles(nextDir, callback)
            }
        }
    }

    private suspend fun scanStorage() = withContext(ioDispatcher) {
        _isAvailable.value = File(absolutePath).exists()

        var size = 0L
        var numOfFiles = 0

        forAllFiles(File(absolutePath)) {
            if (it.isFile) {
                numOfFiles++
                size += Path(it.path).fileSize()
            }
        }
        _size.value = size
        _numberOfFiles.value = numOfFiles
    }

    override suspend fun getAllFiles(): List<IFile> {
        if(checkAvailable() == false)
            return listOf()

        val list = mutableListOf<IFile>()

    }

    override suspend fun getFiles(path: String): List<IFile> {
        TODO("Not yet implemented")
    }

    override fun getFilesFlow(path: String): Flow<DataPackage<IFile>> {
        TODO("Not yet implemented")
    }

    override suspend fun getAllDirs(): List<IDirectory> {
        TODO("Not yet implemented")
    }

    override suspend fun getDirs(path: String): List<IDirectory> {
        TODO("Not yet implemented")
    }

    override fun getDirsFlow(path: String): Flow<DataPackage<IDirectory>> {
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

    override suspend fun getFileInfo(path: String) {
        TODO("Not yet implemented")
    }

    override suspend fun getDirInfo(path: String) {
        TODO("Not yet implemented")
    }

    override suspend fun openWrite(path: String): InputStream {
        TODO("Not yet implemented")
    }

    override suspend fun openRead(path: String): OutputStream {
        TODO("Not yet implemented")
    }

    override suspend fun moveToTrash(path: String) {
        TODO("Not yet implemented")
    }
}