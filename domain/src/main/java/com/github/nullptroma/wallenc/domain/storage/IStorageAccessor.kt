package com.github.nullptroma.wallenc.domain.storage

import com.github.nullptroma.wallenc.domain.models.IDirectory
import com.github.nullptroma.wallenc.domain.models.IFile
import com.github.nullptroma.wallenc.domain.utils.DataPackage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import java.net.URI

interface IStorageAccessor {
    val isAvailable: StateFlow<Boolean>
    val filesUpdates: SharedFlow<IFile>
    val dirsUpdates: SharedFlow<IDirectory>

    suspend fun getAllFiles(): List<IFile>
    suspend fun getFiles(path: URI): List<IFile>
    fun getFilesStream(path: URI): Flow<DataPackage<IFile>>

    suspend fun getAllDirs(): List<IDirectory>
    suspend fun getDirs(path: URI): List<IDirectory>
    fun getDirsStream(path: URI): Flow<DataPackage<IDirectory>>

    suspend fun touchFile(path: URI)
    suspend fun touchDir(path: URI)
    suspend fun delete(path: URI)
    suspend fun getFileInfo(path: URI)
    suspend fun getDirInfo(path: URI)
    suspend fun openWrite(path: URI)
    suspend fun openRead(path: URI)
    suspend fun moveToTrash(path: URI)
}