package com.github.nullptroma.wallenc.data.db.app.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.nullptroma.wallenc.data.db.app.model.DbStorageMetaInfo
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface StorageMetaInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(metaInfo: DbStorageMetaInfo)

    @Query("SELECT * FROM storage_meta_infos")
    suspend fun getAll(): List<DbStorageMetaInfo>

    @Query("SELECT * FROM storage_meta_infos")
    fun getAllFlow(): Flow<List<DbStorageMetaInfo>>

    @Query("SELECT * FROM storage_meta_infos WHERE uuid == :uuid")
    fun getMetaInfoFlow(uuid: UUID): Flow<DbStorageMetaInfo>

    @Query("SELECT * FROM storage_meta_infos WHERE uuid == :uuid")
    suspend fun getMetaInfo(uuid: UUID): DbStorageMetaInfo?

    @Delete
    suspend fun delete(metaInfo: DbStorageMetaInfo)
}