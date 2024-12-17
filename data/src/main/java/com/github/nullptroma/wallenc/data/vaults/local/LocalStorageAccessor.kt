package com.github.nullptroma.wallenc.data.vaults.local

import com.github.nullptroma.wallenc.domain.datatypes.DataPackage
import com.github.nullptroma.wallenc.domain.models.IDirectory
import com.github.nullptroma.wallenc.domain.models.IFile
import com.github.nullptroma.wallenc.domain.models.IStorageAccessor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.InputStream
import java.io.OutputStream
import java.net.URI

class LocalStorageAccessor : IStorageAccessor {
    override val isAvailable: StateFlow<Boolean>
        get() = TODO("Not yet implemented")
    override val filesUpdates: SharedFlow<DataPackage<IFile>>
        get() = TODO("Not yet implemented")
    override val dirsUpdates: SharedFlow<DataPackage<IDirectory>>
        get() = TODO("Not yet implemented")

    override suspend fun getAllFiles(): List<IFile> {
        TODO("Not yet implemented")
    }

    override suspend fun getFiles(path: URI): List<IFile> {
        TODO("Not yet implemented")
    }

    override fun getFilesFlow(path: URI): Flow<DataPackage<IFile>> {
        TODO("Not yet implemented")
    }

    override suspend fun getAllDirs(): List<IDirectory> {
        TODO("Not yet implemented")
    }

    override suspend fun getDirs(path: URI): List<IDirectory> {
        TODO("Not yet implemented")
    }

    override fun getDirsFlow(path: URI): Flow<DataPackage<IDirectory>> {
        TODO("Not yet implemented")
    }

    override suspend fun touchFile(path: URI) {
        TODO("Not yet implemented")
    }

    override suspend fun touchDir(path: URI) {
        TODO("Not yet implemented")
    }

    override suspend fun delete(path: URI) {
        TODO("Not yet implemented")
    }

    override suspend fun getFileInfo(path: URI) {
        TODO("Not yet implemented")
    }

    override suspend fun getDirInfo(path: URI) {
        TODO("Not yet implemented")
    }

    override suspend fun openWrite(path: URI): InputStream {
        TODO("Not yet implemented")
    }

    override suspend fun openRead(path: URI): OutputStream {
        TODO("Not yet implemented")
    }

    override suspend fun moveToTrash(path: URI) {
        TODO("Not yet implemented")
    }
}