package com.github.nullptroma.wallenc.domain.models

import com.github.nullptroma.wallenc.domain.datatypes.DataPackage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.InputStream
import java.io.OutputStream

interface IStorageAccessor {
    val size: StateFlow<Long?>
    val numberOfFiles: StateFlow<Int?>
    val isAvailable: StateFlow<Boolean>
    val filesUpdates: SharedFlow<DataPackage<IFile>>
    val dirsUpdates: SharedFlow<DataPackage<IDirectory>>

    suspend fun getAllFiles(): List<IFile>
    suspend fun getFiles(path: String): List<IFile>
    /**
     * Получение списка файлов в директории
     * @param path Путь к директории
     * @return Поток файлов
     */
    fun getFilesFlow(path: String): Flow<DataPackage<List<IFile>>>

    suspend fun getAllDirs(): List<IDirectory>
    suspend fun getDirs(path: String): List<IDirectory>
    /**
     * Получение списка директорий в директории
     * @param path Путь к директории
     * @return Поток директорий
     */
    fun getDirsFlow(path: String): Flow<DataPackage<List<IDirectory>>>

    suspend fun touchFile(path: String)
    suspend fun touchDir(path: String)
    suspend fun delete(path: String)
    suspend fun openWrite(path: String): OutputStream
    suspend fun openRead(path: String): InputStream
    suspend fun moveToTrash(path: String)
}