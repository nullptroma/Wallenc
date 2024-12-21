package com.github.nullptroma.wallenc.domain.models

import java.time.LocalDateTime


interface IMetaInfo {
    val size: Long
    val isDeleted: Boolean
    val isHidden: Boolean
    val lastModified: LocalDateTime
    val path: String
}