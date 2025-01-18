package com.github.nullptroma.wallenc.data.vaults

import com.github.nullptroma.wallenc.data.db.app.repository.StorageKeyMapRepository
import com.github.nullptroma.wallenc.data.model.StorageKeyMap
import com.github.nullptroma.wallenc.domain.datatypes.EncryptKey
import com.github.nullptroma.wallenc.domain.encrypt.EncryptedStorage
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
    private val repo: StorageKeyMapRepository,
    private val ioDispatcher: CoroutineDispatcher,
    vaultsManager: IVaultsManager
) : IUnlockManager {
    private val _openedStorages = MutableStateFlow<Map<UUID, EncryptedStorage>?>(null)
    override val openedStorages: StateFlow<Map<UUID, IStorage>?>
        get() = _openedStorages
    val mutex = Mutex()

    init {
        CoroutineScope(ioDispatcher).launch {
            vaultsManager.allStorages.collectLatest {
                mutex.lock()
                val allKeys = repo.getAll()
                val allStorages = it.associateBy({ it.uuid }, { it })
                val map = _openedStorages.value?.toMutableMap() ?: mutableMapOf()
                for(keymap in allKeys) {
                    if(map.contains(keymap.sourceUuid))
                        continue
                    val storage = allStorages[keymap.sourceUuid] ?: continue
                    val encStorage = createEncryptedStorage(storage, keymap.key, keymap.destUuid)
                    map[storage.uuid] = encStorage
                }
                _openedStorages.value = map
                mutex.unlock()
            }
        }
    }

    private fun createEncryptedStorage(storage: IStorage, key: EncryptKey, uuid: UUID): EncryptedStorage {
        return EncryptedStorage(
            source = storage,
            key = key,
            ioDispatcher = ioDispatcher,
            uuid = uuid
        )
    }

    override suspend fun open(
        storage: IStorage,
        key: EncryptKey
    ) = withContext(ioDispatcher) {
        mutex.lock()
        val encInfo = storage.encInfo.value ?: throw Exception("EncInfo is null") // TODO
        if (!Encryptor.checkKey(key, encInfo))
            throw Exception("Incorrect Key")

        if (_openedStorages.value == null) {
            val childScope = CoroutineScope(ioDispatcher)
        }
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
        repo.add(keymap)
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
        repo.delete(model)
        mutex.unlock()
    }
}