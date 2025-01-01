package com.github.nullptroma.wallenc.domain.encrypt.entity

import com.github.nullptroma.wallenc.domain.interfaces.IDirectory

data class EncryptedDirectory(
    override val metaInfo: EncryptedMetaInfo,
    override val elementsCount: Int?
) : IDirectory