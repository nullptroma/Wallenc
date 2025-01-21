package com.github.nullptroma.wallenc.domain.common.impl

import com.github.nullptroma.wallenc.domain.datatypes.StorageEncryptionInfo
import com.github.nullptroma.wallenc.domain.interfaces.IMetaInfo
import com.github.nullptroma.wallenc.domain.interfaces.IStorage
import com.github.nullptroma.wallenc.domain.interfaces.IStorageMetaInfo
import java.time.Clock
import java.time.Instant


data class CommonStorageMetaInfo(
    override val encInfo: StorageEncryptionInfo?,
    override val name: String?,
    override val lastModified: Instant = Clock.systemUTC().instant()
) : IStorageMetaInfo