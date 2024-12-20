package com.github.nullptroma.wallenc.domain.models

import java.time.LocalDateTime


interface IMetaInfo {
    val name: String
    val size: Int
    val isDeleted: Boolean
    val isHidden: Boolean
    val lastModified: LocalDateTime
    val path: String
}