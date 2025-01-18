package com.github.nullptroma.wallenc.data.model

import androidx.room.ColumnInfo
import com.github.nullptroma.wallenc.domain.datatypes.EncryptKey
import java.util.UUID

data class StorageKeyMap(
    val sourceUuid: UUID,
    val destUuid: UUID,
    val key: EncryptKey
)
