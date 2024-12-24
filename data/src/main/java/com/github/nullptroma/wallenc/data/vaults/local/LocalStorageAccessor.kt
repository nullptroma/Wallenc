package com.github.nullptroma.wallenc.data.vaults.local

import com.fasterxml.jackson.core.JacksonException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.nullptroma.wallenc.data.vaults.local.entity.LocalDirectory
import com.github.nullptroma.wallenc.data.vaults.local.entity.LocalFile
import com.github.nullptroma.wallenc.data.vaults.local.entity.LocalMetaInfo
import com.github.nullptroma.wallenc.domain.datatypes.DataPackage
import com.github.nullptroma.wallenc.domain.datatypes.DataPage
import com.github.nullptroma.wallenc.domain.models.IDirectory
import com.github.nullptroma.wallenc.domain.models.IFile
import com.github.nullptroma.wallenc.domain.models.IStorageAccessor
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
import timber.log.Timber
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.fileSize
import kotlin.io.path.pathString
import kotlin.io.path.relativeTo

class LocalStorageAccessor(
    absolutePath: String,
    private val ioDispatcher: CoroutineDispatcher
) : IStorageAccessor {
    private val _absolutePath: Path = Path(absolutePath).normalize().absolute()

    private val _size = MutableStateFlow<Long?>(null)
    override val size: StateFlow<Long?> = _size

    private val _numberOfFiles = MutableStateFlow<Int?>(null)
    override val numberOfFiles: StateFlow<Int?> = _numberOfFiles

    private val _isAvailable = MutableStateFlow(false)
    override val isAvailable: StateFlow<Boolean> = _isAvailable

    private val _filesUpdates = MutableSharedFlow<DataPackage<IFile>>()
    override val filesUpdates: SharedFlow<DataPackage<IFile>> = _filesUpdates

    private val _dirsUpdates = MutableSharedFlow<DataPackage<IDirectory>>()
    override val dirsUpdates: SharedFlow<DataPackage<IDirectory>> = _dirsUpdates

    init {
        // запускам сканирование хранилища
        CoroutineScope(ioDispatcher).launch {
            Timber.d("Local storage path: $_absolutePath")
            scanSizeAndNumOfFiles()
        }
    }

    /**
     * Проверяет существование корневого пути Storage в файловой системе, изменяет _isAvailable
     */
    private fun checkAvailable(): Boolean {
        _isAvailable.value = _absolutePath.toFile().exists()
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
        val meta: LocalMetaInfo
    ) {

        companion object {
            private val _jackson = jacksonObjectMapper().apply { findAndRegisterModules() }

            fun fromFile(basePath: Path, file: File): LocalStorageFilePair? {
                if (!file.exists())
                    return null
                if (file.name.endsWith(META_INFO_POSTFIX))
                    return fromMetaFile(basePath, file)

                val filePath = file.toPath()
                val metaFilePath = Path(
                    if (file.isFile) {
                        file.absolutePath + META_INFO_POSTFIX
                    } else {
                        Path(file.absolutePath, META_INFO_POSTFIX).pathString
                    }
                )
                val metaFile = metaFilePath.toFile()
                val metaInfo: LocalMetaInfo
                val storageFilePath = "/" + filePath.relativeTo(basePath)

                if (!metaFile.exists()) {
                    metaInfo = LocalMetaInfo(
                        size = filePath.fileSize(),
                        path = storageFilePath
                    )
                    _jackson.writeValue(metaFile, metaInfo)
                } else {
                    var readMeta: LocalMetaInfo
                    try {
                        val reader = metaFile.bufferedReader()
                        readMeta = _jackson.readValue(reader)
                    } catch (e: JacksonException) {
                        // если файл повреждён - пересоздать
                        readMeta = LocalMetaInfo(
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

            fun fromMetaFile(basePath: Path, metaFile: File): LocalStorageFilePair? {
                if (!metaFile.exists())
                    return null
                if (!metaFile.name.endsWith(META_INFO_POSTFIX))
                    return fromFile(basePath, metaFile)
                var pair: LocalStorageFilePair? = null
                try {
                    val reader = metaFile.bufferedReader()
                    val metaInfo: LocalMetaInfo = _jackson.readValue(reader)
                    val pathString = Path(basePath.pathString, metaInfo.path).pathString
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

            fun from(basePath: Path, anyFile: File): LocalStorageFilePair? {
                return if (anyFile.name.endsWith(META_INFO_POSTFIX))
                    fromMetaFile(basePath, anyFile)
                else
                    fromFile(basePath, anyFile)
            }

            fun from(basePath: Path, storagePath: String): LocalStorageFilePair? {
                val filePath = Path(basePath.pathString, storagePath)
                return from(basePath, filePath.toFile())
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
        fileCallback: (suspend (File, LocalFile) -> Unit)? = null,
        dirCallback: (suspend (File, LocalDirectory) -> Unit)? = null
    ) {
        if (!checkAvailable())
            throw Exception("Not available")
        val basePath = Path(_absolutePath.pathString, baseStoragePath)
        val workedFiles = mutableSetOf<String>()
        val workedMetaFiles = mutableSetOf<String>()

        scanFileSystem(basePath.toFile(), maxDepth, { file ->
            // Если парный файл уже был обработан - скип. Это позволит не читать metaFile дважды
            if (workedFiles.contains(file.absolutePath) || workedMetaFiles.contains(file.absolutePath)) {
                return@scanFileSystem
            }

            val pair = LocalStorageFilePair.from(_absolutePath, file)
            if(pair != null) {
                workedFiles.add(pair.file.absolutePath)
                workedMetaFiles.add(pair.metaFile.absolutePath)

                if (pair.file.isFile) {
                    fileCallback?.invoke(pair.file, LocalFile(pair.meta))
                } else {
                    dirCallback?.invoke(pair.file, LocalDirectory(pair.meta, null))
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

        scanStorage(baseStoragePath = "/", maxDepth = -1, fileCallback = { _, localFile ->
            size += localFile.metaInfo.size
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
        scanStorage(baseStoragePath = "/", maxDepth = -1, fileCallback = { _, localFile ->
            list.add(localFile)
        })
        return@withContext list
    }

    override suspend fun getFiles(path: String): List<IFile> = withContext(ioDispatcher) {
        if (!checkAvailable())
            return@withContext listOf()

        val list = mutableListOf<IFile>()
        scanStorage(baseStoragePath = path, maxDepth = 0, fileCallback = { _, localFile ->
            list.add(localFile)
        })
        return@withContext list
    }

    override fun getFilesFlow(path: String): Flow<DataPackage<List<IFile>>> = flow {
        if (!checkAvailable())
            return@flow

        val buf = mutableListOf<IFile>()
        var pageNumber = 0
        scanStorage(baseStoragePath = path, maxDepth = 0, fileCallback = { _, localFile ->
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
            buf.add(localFile)
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
        TODO("Not yet implemented")
    }

    override fun getDirsFlow(path: String): Flow<DataPackage<List<IDirectory>>> {
        TODO("Not yet implemented")
    }

    override suspend fun touchFile(path: String) = withContext(ioDispatcher) {
        TODO("Not yet implemented")
    }

    override suspend fun touchDir(path: String) = withContext(ioDispatcher) {
        TODO("Not yet implemented")
    }

    override suspend fun delete(path: String) = withContext(ioDispatcher) {
        TODO("Not yet implemented")
    }

    override suspend fun getFileInfo(path: String) = withContext(ioDispatcher) {
        TODO("Not yet implemented")
    }

    override suspend fun getDirInfo(path: String) = withContext(ioDispatcher) {
        TODO("Not yet implemented")
    }

    override suspend fun openWrite(path: String): InputStream = withContext(ioDispatcher) {
        TODO("Not yet implemented")
    }

    override suspend fun openRead(path: String): OutputStream = withContext(ioDispatcher) {
        TODO("Not yet implemented")
    }

    override suspend fun moveToTrash(path: String) = withContext(ioDispatcher) {
        TODO("Not yet implemented")
    }

    companion object {
        private const val META_INFO_POSTFIX = ".wallenc-meta"
        private const val DATA_PAGE_LENGTH = 10
    }
}