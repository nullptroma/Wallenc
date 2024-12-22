package com.github.nullptroma.wallenc.domain.models

import java.time.Instant


interface IMetaInfo {
    val size: Long
    val isDeleted: Boolean
    val isHidden: Boolean
    val lastModified: Instant
    val path: String
}