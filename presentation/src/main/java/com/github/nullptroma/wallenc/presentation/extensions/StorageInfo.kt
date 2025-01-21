package com.github.nullptroma.wallenc.presentation.extensions

import com.github.nullptroma.wallenc.domain.interfaces.IStorageInfo

fun IStorageInfo.toPrintable(): String {
    return "{ uuid: $uuid, enc: ${metaInfo.value.encInfo} }"
}