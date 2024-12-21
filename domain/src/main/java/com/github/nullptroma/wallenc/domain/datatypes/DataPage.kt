package com.github.nullptroma.wallenc.domain.datatypes

class DataPage<T>(
    list: List<T>,
    isLoading: Boolean? = false,
    isError: Boolean? = false,
    val hasNext: Boolean? = false,
    val pageLength: Int,
    val pageIndex: Int
) : DataPackage<List<T>>(data = list, isLoading = isLoading, isError = isError)