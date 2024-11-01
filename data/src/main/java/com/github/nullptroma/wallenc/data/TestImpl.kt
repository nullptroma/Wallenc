package com.github.nullptroma.wallenc.data

import com.github.nullptroma.wallenc.domain.models.IMetaInfo
import java.net.URI
import java.time.LocalDateTime

class TestImpl : IMetaInfo {
    override val name: String
        get() = "Hello225"
    override val size: Int
        get() = 10
    override val isDeleted: Boolean
        get() = true
    override val isHidden: Boolean
        get() = true
    override val lastModified: LocalDateTime
        get() = TODO("Not yet implemented")
    override val path: URI
        get() = URI("/Hello/path")
}