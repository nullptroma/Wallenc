package com.github.nullptroma.wallenc.data.db.app.model

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "storage_keys", primaryKeys = [ "source_uuid", "dest_uuid" ])
data class DbStorageKey(
    @ColumnInfo(name = "source_uuid") val sourceUuid: String,
    @ColumnInfo(name = "dest_uuid") val destUuid: String,
    @ColumnInfo(name = "key") val key: String
)
