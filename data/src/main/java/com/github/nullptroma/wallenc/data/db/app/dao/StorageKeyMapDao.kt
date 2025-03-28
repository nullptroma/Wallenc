package com.github.nullptroma.wallenc.data.db.app.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.nullptroma.wallenc.data.db.app.model.DbStorageKeyMap

@Dao
interface StorageKeyMapDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(vararg keymaps: DbStorageKeyMap)

    @Query("SELECT * FROM storage_key_maps")
    suspend fun getAll(): List<DbStorageKeyMap>

    @Delete
    suspend fun delete(vararg keymaps: DbStorageKeyMap)
}