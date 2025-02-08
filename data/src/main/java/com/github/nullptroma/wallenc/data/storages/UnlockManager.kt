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
    private val metaInfoRepository: StorageMetaInfoRepository,
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
                val keysToRemove = mutableListOf<StorageKeyMap>()
                val allStorages = it.associateBy({ it.uuid }, { it })
                val map = _openedStorages.value?.toMutableMap() ?: mutableMapOf()
                for(keymap in allKeys) {
                    if(map.contains(keymap.sourceUuid))
                        continue
                    try {
                        val storage = allStorages[keymap.sourceUuid] ?: continue
                        val encStorage = createEncryptedStorage(storage, keymap.key, keymap.destUuid)
                        map[storage.uuid] = encStorage
                    }
                    catch (_: Exception) {
                        keysToRemove.add(keymap)
                    }
                }
                _openedStorages.value = map
                keymapRepository.delete(*keysToRemove.toTypedArray()) // удалить мёртвые ключи
                mutex.unlock()
            }
        }
    }

    private suspend fun createEncryptedStorage(storage: IStorage, key: EncryptKey, uuid: UUID): EncryptedStorage {
        return EncryptedStorage.create(
            source = storage,
            key = key,
            ioDispatcher = ioDispatcher,
            metaInfoProvider = metaInfoRepository.createSingleStorageProvider(uuid),
            uuid = uuid
        )
    }

    override suspend fun open(
        storage: IStorage,
        key: EncryptKey
    ) = withContext(ioDispatcher) {
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