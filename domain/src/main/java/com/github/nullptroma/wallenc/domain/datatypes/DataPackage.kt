package com.github.nullptroma.wallenc.domain.datatypes

class DataPackage<T>(
    val data: T,
    val isLoading: Boolean? = false,
    val isError: Boolean? = false
)