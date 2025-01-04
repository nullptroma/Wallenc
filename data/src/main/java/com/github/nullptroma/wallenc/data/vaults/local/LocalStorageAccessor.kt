package com.github.nullptroma.wallenc.data.vaults.local

import com.fasterxml.jackson.core.JacksonException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.nullptroma.wallenc.domain.common.impl.CommonDirectory
import com.github.nullptroma.wallenc.domain.common.impl.CommonFile
import com.github.nullptroma.wallenc.domain.common.impl.CommonMetaInfo
import com.github.nullptroma.wallenc.domain.datatypes.DataPackage
import com.github.nullptroma.wallenc.domain.datatypes.DataPage
import com.github.nullptroma.wallenc.domain.interfaces.IDirectory
import com.github.nullptroma.wallenc.domain.interfaces.IFile
import com.github.nullptroma.wallenc.domain.interfaces.IStorageAccessor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.fileSize
import kotlin.io.path.pathString
import kotlin.io.path.relativeTo

class LocalStorageAccessor(
    filesystemBasePath: String,
    private val ioDispatcher: CoroutineDispatcher
) : IStorageAccessor {
    private val _filesystemBasePath: Path = Path(filesystemBasePath).normalize().absolute()
    private val _jackson = jacksonObjectMapper().apply { findAndRegisterModules() }

    private val _size = MutableStateFlow<Long?>(null)
    override val size: StateFlow<Long?> = _size

    private val _numberOfFiles = MutableStateFlow<Int?>(null)
    override val numberOfFiles: StateFlow<Int?> = _numberOfFiles

    private val _isAvailable = MutableStateFlow(false)
    override val isAvailable: StateFlow<Boolean> = _isAvailable

    private val _filesUpdates = MutableSharedFlow<DataPage<IFile>>()
    override val filesUpdates: SharedFlow<DataPage<IFile>> = _filesUpdates

    private val _dirsUpdates = MutableSharedFlow<DataPage<IDirectory>>()
    override val dirsUpdates: SharedFlow<DataPage<IDirectory>> = _dirsUpdates

    init {
        // запускам сканирование хранилища
        CoroutineScope(ioDispatcher).launch {
            scanSizeAndNumOfFiles()
        }
    }

    /**
     * Проверяет существование корневого пути Storage в файловой системе, изменяет _isAvailable
     */
    private fun checkAvailable(): Boolean {
        _isAvailable.value = _filesystemBasePath.toFile().exists()
        return _isAvailable.value
    }

    /**
     * Перебирает все файлы в файловой системе
     * @param dir стартовый каталог
     * @param maxDepth максимальная глубина (отрицательное для бесконечной)
     * @param callback метод обратного вызова для каждого файла и директории
     */
    private suspend fun scanFileSystem(
        dir: File,
        maxDepth: Int,
        callback: suspend (File) -> Unit,
        useCallbackForSelf: Boolean = true
    ) {
        if (!dir.exists())
            return


        val children = dir.listFiles()
        if (children != null) {
            // вызвать коллбек для каждого элемента директории
            for (child in children) {
                callback(child)
            }

            if (useCallbackForSelf)
                callback(dir)

            if (maxDepth != 0) {
                val nextMaxDepth = if (maxDepth > 0) maxDepth - 1 else maxDepth
                for (child in children) {
                    if (child.isDirectory) {
                        scanFileSystem(child, nextMaxDepth, callback, false)
                    }
                }
            }
        } else if (useCallbackForSelf) {
            callback(dir)
        }
    }

    private class LocalStorageFilePair private constructor(
        val file: File,
        val metaFile: File,
        val meta: CommonMetaInfo
    ) {

        companion object {
            private val _jackson = jacksonObjectMapper().apply { findAndRegisterModules() }

            fun fromFile(filesystemBasePath: Path, file: File): LocalStorageFilePair? {
                if (!file.exists())
                    return null
                if (file.name.endsWith(META_INFO_POSTFIX))
                    return fromMetaFile(filesystemBasePath, file)

                val filePath = file.toPath()
                val metaFilePath = Path(
                    if (file.isFile) {
                        file.absolutePath + META_INFO_POSTFIX
                    } else {
                        Path(file.absolutePath, META_INFO_POSTFIX).pathString
                    }
                )
                val metaFile = metaFilePath.toFile()
                val metaInfo: CommonMetaInfo
                val storageFilePath = "/" + filePath.relativeTo(filesystemBasePath)

                if (!metaFile.exists()) {
                    metaInfo = CommonMetaInfo(
                        size = filePath.fileSize(),
                        path = storageFilePath
                    )
                    _jackson.writeValue(metaFile, metaInfo)
                } else {
                    var readMeta: CommonMetaInfo
                    try {
                        val reader = metaFile.bufferedReader()
                        readMeta = _jackson.readValue(reader)
                    } catch (e: JacksonException) {
                        // если файл повреждён - пересоздать
                        readMeta = CommonMetaInfo(
                            size = filePath.fileSize(),
                            path = storageFilePath
                        )
                        _jackson.writeValue(metaFile, readMeta)
                    }
                    metaInfo = readMeta
                }
                return LocalStorageFilePair(
                    file = file,
                    metaFile = metaFile,
                    meta = metaInfo
                )
            }

            fun fromMetaFile(filesystemBasePath: Path, metaFile: File): LocalStorageFilePair? {
                if (!metaFile.exists())
                    return null
                if (!metaFile.name.endsWith(META_INFO_POSTFIX))
                    return fromFile(filesystemBasePath, metaFile)
                var pair: LocalStorageFilePair? = null
                try {
                    val reader = metaFile.bufferedReader()
                    val metaInfo: CommonMetaInfo = _jackson.readValue(reader)
                    val pathString = Path(filesystemBasePath.pathString, metaInfo.path).pathString
                    val file = File(pathString)
                    if (!file.exists()) {
                        metaFile.delete()
                    } else {
                        pair = LocalStorageFilePair(
                            file = file,
                            metaFile = metaFile,
                            meta = metaInfo
                        )
                    }
                } catch (e: JacksonException) {
                    metaFile.delete()
                }
                return pair
            }

            fun from(filesystemBasePath: Path, anyFile: File): LocalStorageFilePair? {
                return if (anyFile.name.endsWith(META_INFO_POSTFIX))
                    fromMetaFile(filesystemBasePath, anyFile)
                else
                    fromFile(filesystemBasePath, anyFile)
            }

            fun from(filesystemBasePath: Path, storagePath: String): LocalStorageFilePair? {
                val filePath = Path(filesystemBasePath.pathString, storagePath)
                return from(filesystemBasePath, filePath.toFile())
            }
        }
    }

    /**
     * Перебирает все файлы и каталоги в relativePath и возвращает с мета-информацией
     *
     */
    private suspend fun scanStorage(
        baseStoragePath: String,
        maxDepth: Int,
        fileCallback: (suspend (File, CommonFile) -> Unit)? = null,
        dirCallback: (suspend (File, CommonDirectory) -> Unit)? = null
    ) {
        if (!checkAvailable())
            throw Exception("Not available")
        val basePath = Path(_filesystemBasePath.pathString, baseStoragePath)
        val workedFiles = mutableSetOf<String>()
        val workedMetaFiles = mutableSetOf<String>()

        scanFileSystem(basePath.toFile(), maxDepth, { file ->
            // Если парный файл уже был обработан - скип. Это позволит не читать metaFile дважды
            if (workedFiles.contains(file.absolutePath) || workedMetaFiles.contains(file.absolutePath)) {
                return@scanFileSystem
            }

            val pair = LocalStorageFilePair.from(_filesystemBasePath, file)
            if(pair != null) {
                workedFiles.add(pair.file.absolutePath)
                workedMetaFiles.add(pair.metaFile.absolutePath)

                if (pair.file.isFile) {
                    fileCallback?.invoke(pair.file, CommonFile(pair.meta))
                } else {
                    dirCallback?.invoke(pair.file, CommonDirectory(pair.meta, null))
                }
            }
        })
    }


    /**
     * Считает файлы и их размер. Не бросает исключения, если файлы недоступны
     * @throws none Если возникла ошибка, оставляет размер и количества файлов равными null
     */
    private suspend fun scanSizeAndNumOfFiles() {
        if (!checkAvailable()) {
            _size.value = null
            _numberOfFiles.value = null
            return
        }

        var size = 0L
        var numOfFiles = 0

        scanStorage(baseStoragePath = "/", maxDepth = -1, fileCallback = { _, CommonFile ->
            size += CommonFile.metaInfo.size
            numOfFiles++

            if(numOfFiles % DATA_PAGE_LENGTH == 0) {
                _size.value = size
                _numberOfFiles.value = numOfFiles
            }
        })

        _size.value = size
        _numberOfFiles.value = numOfFiles
    }

    override suspend fun getAllFiles(): List<IFile> = withContext(ioDispatcher) {
        if (!checkAvailable())
            return@withContext listOf()

        val list = mutableListOf<IFile>()
        scanStorage(baseStoragePath = "/", maxDepth = -1, fileCallback = { _, CommonFile ->
            list.add(CommonFile)
        })
        return@withContext list
    }

    override suspend fun getFiles(path: String): List<IFile> = withContext(ioDispatcher) {
        if (!checkAvailable())
            return@withContext listOf()

        val list = mutableListOf<IFile>()
        scanStorage(baseStoragePath = path, maxDepth = 0, fileCallback = { _, CommonFile ->
            list.add(CommonFile)
        })
        return@withContext list
    }

    override fun getFilesFlow(path: String): Flow<DataPackage<List<IFile>>> = flow {
        if (!checkAvailable())
            return@flow

        val buf = mutableListOf<IFile>()
        var pageNumber = 0
        scanStorage(baseStoragePath = path, maxDepth = 0, fileCallback = { _, CommonFile ->
            if (buf.size == DATA_PAGE_LENGTH) {
                val page = DataPage(
                    list = buf.toList(),
                    isLoading = false,
                    isError = false,
                    hasNext = true,
                    pageLength = DATA_PAGE_LENGTH,
                    pageIndex = pageNumber++
                )
                emit(page)
                buf.clear()
            }
            buf.add(CommonFile)
        })
        // отправка последней страницы
        val page = DataPage(
            list = buf.toList(),
            isLoading = false,
            isError = false,
            hasNext = false,
            pageLength = DATA_PAGE_LENGTH,
            pageIndex = pageNumber++
        )
        emit(page)
    }.flowOn(ioDispatcher)

    override suspend fun getAllDirs(): List<IDirectory> = withContext(ioDispatcher) {
        if (!checkAvailable())
            return@withContext listOf()

        val list = mutableListOf<IDirectory>()
        scanStorage(baseStoragePath = "/", maxDepth = -1, dirCallback = { _, localDir ->
            list.add(localDir)
        })
        return@withContext list
    }

    override suspend fun getDirs(path: String): List<IDirectory> = withContext(ioDispatcher) {
        if (!checkAvailable())
            return@withContext listOf()

        val list = mutableListOf<IDirectory>()
        scanStorage(baseStoragePath = path, maxDepth = 0, dirCallback = { _, localDir ->
            list.add(localDir)
        })
        return@withContext list
    }

    override fun getDirsFlow(path: String): Flow<DataPackage<List<IDirectory>>> = flow {
        if (!checkAvailable())
            return@flow

        val buf = mutableListOf<IDirectory>()
        var pageNumber = 0
        scanStorage(baseStoragePath = path, maxDepth = 0, dirCallback = { _, localDir ->
            if (buf.size == DATA_PAGE_LENGTH) {
                val page = DataPage(
                    list = buf.toList(),
                    isLoading = false,
                    isError = false,
                    hasNext = true,
                    pageLength = DATA_PAGE_LENGTH,
                    pageIndex = pageNumber++
                )
                emit(page)
                buf.clear()
            }
            buf.add(localDir)
        })
        // отправка последней страницы
        val page = DataPage(
            list = buf.toList(),
            isLoading = false,
            isError = false,
            hasNext = false,
            pageLength = DATA_PAGE_LENGTH,
            pageIndex = pageNumber++
        )
        emit(page)
    }.flowOn(ioDispatcher)

    private fun writeMeta(metaFile: File, meta: CommonMetaInfo) {
        _jackson.writeValue(metaFile, meta)
    }

    private fun createFile(storagePath: String): CommonFile {
        val path = Path(_filesystemBasePath.pathString, storagePath)
        val file = path.toFile()
        if(file.exists() && file.isDirectory) {
            throw Exception("Что то пошло не так") // TODO
        }
        else {
            file.createNewFile()
        }

        val pair = LocalStorageFilePair.from(_filesystemBasePath, file) ?: throw Exception("Что то пошло не так") // TODO
        val newMeta = pair.meta.copy(lastModified = java.time.Clock.systemUTC().instant())
        writeMeta(pair.metaFile, newMeta)
        return CommonFile(newMeta)
    }

    private fun createDir(storagePath: String): CommonDirectory {
        val path = Path(_filesystemBasePath.pathString, storagePath)
        val file = path.toFile()
        if(file.exists() && !file.isDirectory) {
            throw Exception("Что то пошло не так") // TODO
        }
        else {
            Files.createDirectories(path)
        }

        val pair = LocalStorageFilePair.from(_filesystemBasePath, file) ?: throw Exception("Что то пошло не так") // TODO
        val newMeta = pair.meta.copy(lastModified = java.time.Clock.systemUTC().instant())
        writeMeta(pair.metaFile, newMeta)
        return CommonDirectory(newMeta, 0)
    }

    override suspend fun touchFile(path: String): Unit = withContext(ioDispatcher) {
        createFile(path)
    }

    override suspend fun touchDir(path: String): Unit = withContext(ioDispatcher) {
        createDir(path)
    }

    override suspend fun delete(path: String) = withContext(ioDispatcher) {
        val pair = LocalStorageFilePair.from(_filesystemBasePath, path)
        if(pair != null) {
            pair.file.delete()
            pair.metaFile.delete()
        }
    }

    override suspend fun openWrite(path: String): OutputStream = withContext(ioDispatcher) {
        touchFile(path)
        val pair = LocalStorageFilePair.from(_filesystemBasePath, path) ?: throw Exception("Файла нет") // TODO
        return@withContext pair.file.outputStream()
    }

    override suspend fun openRead(path: String): InputStream = withContext(ioDispatcher) {
        val pair = LocalStorageFilePair.from(_filesystemBasePath, path) ?: throw Exception("Файла нет") // TODO
        return@withContext pair.file.inputStream()
    }

    override suspend fun moveToTrash(path: String) = withContext(ioDispatcher) {
        val pair = LocalStorageFilePair.from(_filesystemBasePath, path) ?: throw Exception("Файла нет") // TODO
        val newMeta = pair.meta.copy(isDeleted = true)
        writeMeta(pair.metaFile, newMeta)
    }

    companion object {
        private const val META_INFO_POSTFIX = ".wallenc-meta"
        private const val DATA_PAGE_LENGTH = 10
    }
}