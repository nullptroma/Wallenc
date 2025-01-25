package com.github.nullptroma.wallenc.domain.interfaces

import com.github.nullptroma.wallenc.domain.datatypes.StorageEncryptionInfo
import java.time.Instant

interface IStorageMetaInfo {
    val encInfo: StorageEncryptionInfo
    val name: String?
    val lastModified: Instant

}