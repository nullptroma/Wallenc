package com.github.nullptroma.wallenc.data.vaults.local.entity

import com.github.nullptroma.wallenc.domain.models.IMetaInfo
import java.time.LocalDateTime

data class LocalMetaInfo(
    override val size: Long,
    override val isDeleted: Boolean,
    override val isHidden: Boolean,
    override val lastModified: LocalDateTime,
    override val path: String
) : IMetaInfo