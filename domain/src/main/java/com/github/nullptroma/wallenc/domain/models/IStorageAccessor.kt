package com.github.nullptroma.wallenc.domain.models

import com.github.nullptroma.wallenc.domain.datatypes.DataPackage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.InputStream
import java.io.OutputStream
import java.net.URI

interface IStorageAccessor {
    val isAvailable: StateFlow<Boolean>
    val filesUpdates: SharedFlow<DataPackage<IFile>>
    val dirsUpdates: SharedFlow<DataPackage<IDirectory>>

    suspend fun getAllFiles(): List<IFile>
    suspend fun getFiles(path: URI): List<IFile>
    /**
     * Получение списка файлов в директории
     * @param path Путь к директории
     * @return Поток файлов
     */
    fun getFilesFlow(path: URI): Flow<DataPackage<IFile>>

    suspend fun getAllDirs(): List<IDirectory>
    suspend fun getDirs(path: URI): List<IDirectory>
    /**
     * Получение списка директорий в директории
     * @param path Путь к директории
     * @return Поток директорий
     */
    fun getDirsFlow(path: URI): Flow<DataPackage<IDirectory>>

    suspend fun touchFile(path: URI)
    suspend fun touchDir(path: URI)
    suspend fun delete(path: URI)
    suspend fun getFileInfo(path: URI)
    suspend fun getDirInfo(path: URI)
    suspend fun openWrite(path: URI): InputStream
    suspend fun openRead(path: URI): OutputStream
    suspend fun moveToTrash(path: URI)
}