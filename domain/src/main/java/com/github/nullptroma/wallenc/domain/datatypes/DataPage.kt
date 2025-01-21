package com.github.nullptroma.wallenc.domain.datatypes

class DataPage<T>(
    list: List<T>,
    isLoading: Boolean? = null,
    isError: Boolean? = null,
    val hasNext: Boolean? = null,
    val pageLength: Int,
    val pageIndex: Int
) : DataPackage<List<T>>(data = list, isLoading = isLoading, isError = isError)