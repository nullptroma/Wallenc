package com.github.nullptroma.wallenc.data.vaults.local.entity

import com.github.nullptroma.wallenc.domain.interfaces.IDirectory

data class LocalDirectory(
    override val metaInfo: LocalMetaInfo,
    override val elementsCount: Int?
) : IDirectory