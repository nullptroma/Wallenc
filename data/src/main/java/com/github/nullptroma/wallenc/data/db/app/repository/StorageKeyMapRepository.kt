package com.github.nullptroma.wallenc.data.db.app.repository

import com.github.nullptroma.wallenc.data.db.app.dao.StorageKeyMapDao
import com.github.nullptroma.wallenc.data.db.app.model.DbStorageKeyMap
import com.github.nullptroma.wallenc.data.model.StorageKeyMap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class StorageKeyMapRepository(
    private val dao: StorageKeyMapDao,
    private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun getAll() = withContext(ioDispatcher) { dao.getAll().map { it.toModel() } }
    suspend fun add(keymap: StorageKeyMap) = withContext(ioDispatcher)  {
        val dbModel = DbStorageKeyMap.fromModel(keymap)
        dao.add(dbModel)
    }

    suspend fun delete(keymap: StorageKeyMap) = withContext(ioDispatcher)  {
        val dbModel = DbStorageKeyMap.fromModel(keymap)
        dao.delete(dbModel)
    }
}