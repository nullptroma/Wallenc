package com.github.nullptroma.wallenc.data.db.app

import androidx.room.Database
import androidx.room.RoomDatabase
import com.github.nullptroma.wallenc.data.db.app.dao.StorageKeyMapDao
import com.github.nullptroma.wallenc.data.db.app.dao.StorageMetaInfoDao
import com.github.nullptroma.wallenc.data.db.app.model.DbStorageKeyMap
import com.github.nullptroma.wallenc.data.db.app.model.DbStorageMetaInfo

interface IAppDb {
    val storageKeyMapDao: StorageKeyMapDao
    val storageMetaInfoDao: StorageMetaInfoDao
}

@Database(entities = [DbStorageKeyMap::class, DbStorageMetaInfo::class], version = 2, exportSchema = false)
abstract class AppDb : IAppDb, RoomDatabase() {
    abstract override val storageKeyMapDao: StorageKeyMapDao
    abstract override val storageMetaInfoDao: StorageMetaInfoDao
}