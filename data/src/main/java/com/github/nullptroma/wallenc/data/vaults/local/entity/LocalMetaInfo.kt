package com.github.nullptroma.wallenc.data.vaults.local.entity

import com.github.nullptroma.wallenc.domain.models.IMetaInfo
import java.time.Instant


data class LocalMetaInfo(
    override val size: Long,
    override val isDeleted: Boolean = false,
    override val isHidden: Boolean = false,
    override val lastModified: Instant = java.time.Clock.systemUTC().instant(),
    override val path: String
) : IMetaInfo