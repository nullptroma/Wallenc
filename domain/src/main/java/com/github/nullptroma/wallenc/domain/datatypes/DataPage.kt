package com.github.nullptroma.wallenc.domain.datatypes

class DataPage<T>(
    list: List<T>,
    val hasNext: Boolean? = false,
    val pageLength: Int,
    val pageNumber: Int
) : DataPackage<List<T>>(list)