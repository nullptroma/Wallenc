package com.github.nullptroma.wallenc.data.db.app.repository

import com.github.nullptroma.wallenc.data.db.app.dao.StorageKeyMapDao
import com.github.nullptroma.wallenc.data.db.app.model.DbStorageKeyMap
import com.github.nullptroma.wallenc.data.model.StorageKeyMap

class StorageKeyMapRepository(private val dao: StorageKeyMapDao) {
    fun getAll() = dao.getAll().map { it.toModel() }
    fun add(keymap: StorageKeyMap) {
        val dbModel = DbStorageKeyMap.fromModel(keymap)
        dao.add(dbModel)
    }
    fun delete(keymap: StorageKeyMap) {
        val dbModel = DbStorageKeyMap.fromModel(keymap)
        dao.delete(dbModel)
    }
}