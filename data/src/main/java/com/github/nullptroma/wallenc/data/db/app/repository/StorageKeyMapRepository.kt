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
    suspend fun add(vararg keymaps: StorageKeyMap) = withContext(ioDispatcher)  {
        val dbModels = keymaps.map { DbStorageKeyMap.fromModel(it) }
        dao.add(*dbModels.toTypedArray())
    }

    suspend fun delete(vararg keymaps: StorageKeyMap) = withContext(ioDispatcher)  {
        val dbModels = keymaps.map { DbStorageKeyMap.fromModel(it) }
        dao.delete(*dbModels.toTypedArray())
    }
}