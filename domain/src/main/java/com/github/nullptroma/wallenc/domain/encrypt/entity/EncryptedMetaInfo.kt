package com.github.nullptroma.wallenc.domain.encrypt.entity

import com.github.nullptroma.wallenc.domain.interfaces.IMetaInfo
import java.time.Instant


data class EncryptedMetaInfo(
    override val size: Long,
    override val isDeleted: Boolean = false,
    override val isHidden: Boolean = false,
    override val lastModified: Instant = java.time.Clock.systemUTC().instant(),
    override val path: String
) : IMetaInfo