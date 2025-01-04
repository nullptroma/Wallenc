package com.github.nullptroma.wallenc.data.db.app

import androidx.room.Database
import androidx.room.RoomDatabase
import com.github.nullptroma.wallenc.data.db.app.dao.StorageKeyDao
import com.github.nullptroma.wallenc.data.db.app.model.DbStorageKey

interface IAppDb {
    val storageKeyDao: StorageKeyDao
}

@Database(entities = [DbStorageKey::class], version = 1, exportSchema = false)
abstract class AppDb : IAppDb, RoomDatabase() {
    abstract override val storageKeyDao: StorageKeyDao
}