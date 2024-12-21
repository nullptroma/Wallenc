package com.github.nullptroma.wallenc.data.vaults.local

import android.content.Context
import com.github.nullptroma.wallenc.domain.datatypes.EncryptKey
import com.github.nullptroma.wallenc.domain.enums.VaultType
import com.github.nullptroma.wallenc.domain.models.IStorage
import com.github.nullptroma.wallenc.domain.models.IVault
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import kotlin.io.path.Path
import kotlin.io.path.createDirectory
import kotlin.io.path.pathString

class LocalVault(private val ioDispatcher: CoroutineDispatcher, context: Context) : IVault {
    override val type: VaultType = VaultType.LOCAL
    override val uuid: UUID
        get() = TODO("Not yet implemented")

    private val _storages = MutableStateFlow(listOf<IStorage>())
    override val storages: StateFlow<List<IStorage>> = _storages

    private val _isAvailable = MutableStateFlow(false)
    override val isAvailable: StateFlow<Boolean> = _isAvailable

    private val _totalSpace = MutableStateFlow(null)
    override val totalSpace: StateFlow<Int?> = _totalSpace

    private val _availableSpace = MutableStateFlow(null)
    override val availableSpace: StateFlow<Int?> = _availableSpace

    private val _path = MutableStateFlow<File?>(null)

    init {
        CoroutineScope(ioDispatcher).launch {
            _path.value = context.getExternalFilesDir("LocalVault")
            _isAvailable.value = _path.value != null
            readStorages()
        }
    }

    private fun readStorages() {
        val path = _path.value
        if (path == null || !_isAvailable.value)
            return

        val dirs = path.listFiles()?.filter { it.isDirectory }
        if (dirs != null) {
            _storages.value = dirs.map {
                val uuid = UUID.fromString(it.name)
                LocalStorage(uuid, it.path, ioDispatcher)
            }
        }
    }

    override suspend fun createStorage(): IStorage = withContext(ioDispatcher) {
        val path = _path.value
        if (path == null || !_isAvailable.value)
            throw Exception("Not available")

        val uuid = UUID.randomUUID()
        val next = Path(path.path, uuid.toString())
        next.createDirectory()
        val newStorage = LocalStorage(uuid, next.pathString, ioDispatcher)
        _storages.value = _storages.value.toMutableList().apply {
            add(newStorage)
        }
        return@withContext newStorage
    }

    override suspend fun createStorage(
        key: EncryptKey
    ): IStorage = withContext(ioDispatcher) {
        TODO("Not yet implemented")
    }

    override suspend fun createStorage(
        key: EncryptKey,
        uuid: UUID
    ): IStorage = withContext(ioDispatcher) {
        TODO("Not yet implemented")
    }

    override suspend fun remove(storage: IStorage) = withContext(ioDispatcher) {
        TODO("Not yet implemented")
    }
}