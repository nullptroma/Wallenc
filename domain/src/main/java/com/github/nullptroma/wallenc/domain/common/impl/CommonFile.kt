package com.github.nullptroma.wallenc.domain.common.impl

import com.github.nullptroma.wallenc.domain.interfaces.IFile
import com.github.nullptroma.wallenc.domain.interfaces.IMetaInfo

data class CommonFile(override val metaInfo: IMetaInfo) : IFile