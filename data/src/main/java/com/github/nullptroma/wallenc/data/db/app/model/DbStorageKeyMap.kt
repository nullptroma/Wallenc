package com.github.nullptroma.wallenc.data.db.app.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.github.nullptroma.wallenc.data.db.app.repository.StorageKeyMapRepository
import com.github.nullptroma.wallenc.data.model.StorageKeyMap
import com.github.nullptroma.wallenc.domain.datatypes.EncryptKey
import java.util.UUID

@Entity(tableName = "storage_key_maps", primaryKeys = [ "source_uuid", "dest_uuid" ])
data class DbStorageKeyMap(
    @ColumnInfo(name = "source_uuid") val sourceUuid: String,
    @ColumnInfo(name = "dest_uuid") val destUuid: String,
    @ColumnInfo(name = "key") val key: ByteArray
) {
    fun toModel(): StorageKeyMap {
        return StorageKeyMap(
            sourceUuid = UUID.fromString(sourceUuid),
            destUuid = UUID.fromString(destUuid),
            key = EncryptKey(key)
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DbStorageKeyMap

        if (sourceUuid != other.sourceUuid) return false
        if (destUuid != other.destUuid) return false
        if (!key.contentEquals(other.key)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sourceUuid.hashCode()
        result = 31 * result + destUuid.hashCode()
        result = 31 * result + key.contentHashCode()
        return result
    }

    companion object {
        fun fromModel(keymap: StorageKeyMap): DbStorageKeyMap {
            return DbStorageKeyMap(
                sourceUuid = keymap.sourceUuid.toString(),
                destUuid = keymap.destUuid.toString(),
                key = keymap.key.bytes
            )
        }
    }
}
