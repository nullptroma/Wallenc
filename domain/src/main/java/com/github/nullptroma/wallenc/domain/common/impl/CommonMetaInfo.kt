package com.github.nullptroma.wallenc.domain.common.impl

import com.github.nullptroma.wallenc.domain.interfaces.IMetaInfo
import java.time.Clock
import java.time.Instant


data class CommonMetaInfo(
    override val size: Long,
    override val isDeleted: Boolean = false,
    override val isHidden: Boolean = false,
    override val lastModified: Instant = Clock.systemUTC().instant(),
    override val path: String
) : IMetaInfo