package com.github.nullptroma.wallenc.domain.interfaces

import java.time.Instant


interface IMetaInfo {
    val size: Long
    val isDeleted: Boolean
    val isHidden: Boolean
    val lastModified: Instant
    val path: String
}