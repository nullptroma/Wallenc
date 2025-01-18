package com.github.nullptroma.wallenc.data.db.app.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.nullptroma.wallenc.data.db.app.model.DbStorageKeyMap
import kotlinx.coroutines.flow.StateFlow

@Dao
interface StorageKeyMapDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(keymap: DbStorageKeyMap)

    @Query("SELECT * FROM storage_key_maps")
    fun getAll(): List<DbStorageKeyMap>

    @Delete
    fun delete(keymap: DbStorageKeyMap)
}