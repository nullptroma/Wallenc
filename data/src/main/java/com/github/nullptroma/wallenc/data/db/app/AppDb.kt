package com.github.nullptroma.wallenc.data.db.app

import androidx.room.Database
import androidx.room.RoomDatabase
import com.github.nullptroma.wallenc.data.db.app.dao.StorageKeyMapDao
import com.github.nullptroma.wallenc.data.db.app.model.DbStorageKeyMap

interface IAppDb {
    val storageKeyMapDao: StorageKeyMapDao
}

@Database(entities = [DbStorageKeyMap::class], version = 2, exportSchema = false)
abstract class AppDb : IAppDb, RoomDatabase() {
    abstract override val storageKeyMapDao: StorageKeyMapDao
}