package com.github.nullptroma.wallenc.domain.encrypt.entity

import com.github.nullptroma.wallenc.domain.interfaces.IFile

data class EncryptedFile(override val metaInfo: EncryptedMetaInfo) : IFile