package com.github.nullptroma.wallenc.domain.utils

open class DataPackage<T>(
    val data: T,
    val hasNext: Boolean? = false,
    val isLoading: Boolean? = false,
    val isError: Boolean? = false
)