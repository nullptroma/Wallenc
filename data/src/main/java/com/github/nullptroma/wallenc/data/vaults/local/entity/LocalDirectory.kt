package com.github.nullptroma.wallenc.data.vaults.local.entity

import com.github.nullptroma.wallenc.domain.models.IDirectory

class LocalDirectory(
    override val metaInfo: LocalMetaInfo,
    override val elementsCount: Int
) : IDirectory