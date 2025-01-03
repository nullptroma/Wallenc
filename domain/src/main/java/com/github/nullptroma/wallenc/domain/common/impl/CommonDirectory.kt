package com.github.nullptroma.wallenc.domain.common.impl

import com.github.nullptroma.wallenc.domain.interfaces.IDirectory

data class CommonDirectory(
    override val metaInfo: CommonMetaInfo,
    override val elementsCount: Int?
) : IDirectory