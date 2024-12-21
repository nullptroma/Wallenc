package com.github.nullptroma.wallenc.data.vaults.local

import com.fasterxml.jackson.core.JacksonException
import com.fasterxml.jackson.core.JsonParseException
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
import java.time.LocalDateTime
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
    private val _jackson = jacksonObjectMapper().apply { findAndRegisterModules() }

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
            updateSizeAndNumOfFiles()
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

            if (maxDepth != 0) {
                val nextMaxDepth = if (maxDepth > 0) maxDepth - 1 else maxDepth
                for (child in children) {
                    if (child.isDirectory) {
                        scanFileSystem(child, nextMaxDepth, callback, false)
                    }
                }
            }
        }

        if (useCallbackForSelf)
            callback(dir)
    }

    /**
     * Перебирает все файлы и каталоги в relativePath и возвращает с мета-информацией
     *
     */
    private suspend fun scanStorage(
        baseStoragePath: String,
        maxDepth: Int,
        fileCallback: suspend (LocalFile) -> Unit = {},
        dirCallback: suspend (LocalDirectory) -> Unit = {}
    ) {
        if (!checkAvailable())
            throw Exception("Not available")
        val basePath = Path(_absolutePath.pathString, baseStoragePath)
        scanFileSystem(basePath.toFile(), maxDepth, { file ->
            val filePath = Path(file.absolutePath)

            // если это файл с мета-информацией - пропустить
            if (filePath.pathString.endsWith(META_INFO_POSTFIX)) {
                // Если не удаётся прочитать метаданные или они указывают на несуществующий файл - удалить
                try {
                    val reader = file.bufferedReader()
                    val meta : LocalMetaInfo = _jackson.readValue(reader)
                    val fileInMeta = File(Path(_absolutePath.pathString, meta.path).pathString)
                    if (!fileInMeta.exists())
                        file.delete()
                } catch (e: JacksonException) {
                    file.delete()
                }
                return@scanFileSystem
            }

            val metaFilePath = Path(
                if (file.isFile) {
                    file.absolutePath + META_INFO_POSTFIX
                } else {
                    Path(file.absolutePath, META_INFO_POSTFIX).pathString
                }
            )
            val metaFile = metaFilePath.toFile()
            val metaInfo: LocalMetaInfo
            val storageFilePath = "/" + filePath.relativeTo(_absolutePath)

            if (!metaFile.exists()) {
                metaInfo = createNewLocalMetaInfo(storageFilePath, filePath.fileSize())
                _jackson.writeValue(metaFile, metaInfo)
            } else {
                var readMeta: LocalMetaInfo
                try {
                    val reader = metaFile.bufferedReader()
                    readMeta = _jackson.readValue(reader)
                } catch (e: JacksonException) {
                    // если файл повреждён - пересоздать
                    readMeta = createNewLocalMetaInfo(storageFilePath, filePath.fileSize())
                    _jackson.writeValue(metaFile, readMeta)
                }
                metaInfo = readMeta
            }

            if (file.isFile) {
                fileCallback(LocalFile(metaInfo))
            } else {
                dirCallback(LocalDirectory(metaInfo, null))
            }
        })
    }

    /**
     * Создаёт LocalMetaInfo, не требуя наличие файла в файловой системе
     * @param storagePath полный путь в Storage
     * @param size размер файла
     */
    private fun createNewLocalMetaInfo(storagePath: String, size: Long): LocalMetaInfo {
        return LocalMetaInfo(
            size = size,
            isDeleted = false,
            isHidden = false,
            lastModified = LocalDateTime.now(),
            path = storagePath
        )
    }


    /**
     * Считает файлы и их размер. Не бросает исключения, если файлы недоступны
     * @throws none Если возникла ошибка, оставляет размер и количества файлов равными null
     */
    private suspend fun updateSizeAndNumOfFiles() {
        if (!checkAvailable()) {
            _size.value = null
            _numberOfFiles.value = null
            return
        }

        var size = 0L
        var numOfFiles = 0

        scanStorage(baseStoragePath = "/", maxDepth = -1, fileCallback = {
            size += it.metaInfo.size
            numOfFiles++
        })

        _size.value = size
        _numberOfFiles.value = numOfFiles
    }

    override suspend fun getAllFiles(): List<IFile> = withContext(ioDispatcher) {
        if (!checkAvailable())
            return@withContext listOf()

        val list = mutableListOf<IFile>()
        scanStorage(baseStoragePath = "/", maxDepth = -1, fileCallback = {
            list.add(it)
        })
        return@withContext list
    }

    override suspend fun getFiles(path: String): List<IFile> = withContext(ioDispatcher) {
        if (!checkAvailable())
            return@withContext listOf()

        val list = mutableListOf<IFile>()
        scanStorage(baseStoragePath = path, maxDepth = 0, fileCallback = {
            list.add(it)
        })
        return@withContext list
    }

    override fun getFilesFlow(path: String): Flow<DataPackage<List<IFile>>> = flow {
        if (!checkAvailable())
            return@flow

        val buf = mutableListOf<IFile>()
        var pageNumber = 0
        scanStorage(baseStoragePath = path, maxDepth = 0, fileCallback = {
            if(buf.size == DATA_PAGE_LENGTH) {
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
            buf.add(it)
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
        TODO("Not yet implemented")
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