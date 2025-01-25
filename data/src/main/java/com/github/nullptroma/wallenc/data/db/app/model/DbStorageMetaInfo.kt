package com.github.nullptroma.wallenc.data.db.app.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.nullptroma.wallenc.domain.common.impl.CommonStorageMetaInfo
import java.util.UUID

@Entity(tableName = "storage_meta_infos")
data class DbStorageMetaInfo(
    @PrimaryKey @ColumnInfo(name = "uuid") val uuid: UUID,
    @ColumnInfo(name = "meta_info") val metaInfoJson: String
)