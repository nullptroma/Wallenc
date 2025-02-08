package com.github.nullptroma.wallenc.data.storages

import com.github.nullptroma.wallenc.data.db.app.repository.StorageKeyMapRepository
import com.github.nullptroma.wallenc.data.db.app.repository.StorageMetaInfoRepository
import com.github.nullptroma.wallenc.data.model.StorageKeyMap
import com.github.nullptroma.wallenc.domain.datatypes.EncryptKey
import com.github.nullptroma.wallenc.data.storages.encrypt.EncryptedStorage
import com.github.nullptroma.wallenc.domain.encrypt.Encryptor
import com.github.nullptroma.wallenc.domain.interfaces.IStorage
import com.github.nullptroma.wallenc.domain.interfaces.IUnlockManager
import com.github.nullptroma.wallenc.domain.interfaces.IVaultsManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import java.util.UUID

class UnlockManager(
    private val keymapRepository: StorageKeyMapRepository,
    private val ioDispatcher: CoroutineDispatcher,
    vaultsManager: IVaultsManager
) : IUnlockManager {
    private val _openedStorages = MutableStateFlow<Map<UUID, EncryptedStorage>?>(null)
    override val openedStorages: StateFlow<Map<UUID, IStorage>?>
        get() = _openedStorages
    private val mutex = Mutex()

    init {
        CoroutineScope(ioDispatcher).launch {
            vaultsManager.allStorages.collectLatest {
                mutex.lock()
                val allKeys = keymapRepository.getAll()
                val usedKeys = mutableListOf<StorageKeyMap>()
                val keysToRemove = mutableListOf<StorageKeyMap>()
                val allStorages = it.toMutableList()
                val map = _openedStorages.value?.toMutableMap() ?: mutableMapOf()
                while(allStorages.size > 0) {
                    val storage = allStorages[allStorages.size-1]
                    val key = allKeys.find { key -> key.sourceUuid == storage.uuid }
                    if(key == null) {
                        allStorages.removeAt(allStorages.size - 1)
                        continue
                    }
                    try {
                        val encStorage = createEncryptedStorage(storage, key.key, key.destUuid)
                        map[storage.uuid] = encStorage
                        usedKeys.add(key)
                        allStorages.removeAt(allStorages.size - 1)
                        allStorages.add(encStorage)
                    }
                    catch (_: Exception) {
                        // ключ не подошёл
                        keysToRemove.add(key)
                        allStorages.removeAt(allStorages.size - 1)
                    }
                }
                keymapRepository.delete(*keysToRemove.toTypedArray()) // удалить мёртвые ключи
                _openedStorages.value = map.toMap()
                mutex.unlock()
            }
        }
    }

    private suspend fun createEncryptedStorage(storage: IStorage, key: EncryptKey, uuid: UUID): EncryptedStorage {
        return EncryptedStorage.create(
            source = storage,
            key = key,
            ioDispatcher = ioDispatcher,
            uuid = uuid
        )
    }

    override suspend fun open(
        storage: IStorage,
        key: EncryptKey
    ): EncryptedStorage = withContext(ioDispatcher) {
        mutex.lock()
        val encInfo = storage.metaInfo.value.encInfo ?: throw Exception("EncInfo is null") // TODO
        if (!Encryptor.checkKey(key, encInfo))
            throw Exception("Incorrect Key")

        val opened = _openedStorages.first { it != null }!!.toMutableMap()
        val cur = opened[storage.uuid]
        if (cur != null)
            throw Exception("Storage is already open")

        val keymap = StorageKeyMap(
            sourceUuid = storage.uuid,
            destUuid = UUID.randomUUID(),
            key = key
        )
        val encStorage = createEncryptedStorage(storage, keymap.key, keymap.destUuid)
        opened[storage.uuid] = encStorage
        _openedStorages.value = opened
        keymapRepository.add(keymap)
        mutex.unlock()
        return@withContext encStorage
    }

    override suspend fun close(storage: IStorage) = withContext(ioDispatcher) {
        mutex.lock()
        val opened = _openedStorages.first { it != null }!!
        val enc = opened[storage.uuid] ?: return@withContext
        val model = StorageKeyMap(
            sourceUuid = storage.uuid,
            destUuid = enc.uuid,
            key = EncryptKey("")
        )
        _openedStorages.value = opened.toMutableMap().apply {
            remove(storage.uuid)
        }
        enc.dispose()
        keymapRepository.delete(model)
        mutex.unlock()
    }
}