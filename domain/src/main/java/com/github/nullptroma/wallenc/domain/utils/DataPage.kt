package com.github.nullptroma.wallenc.domain.utils

class DataPage<T>(
    list: List<T>,
    val pageLength: Int,
    val pageNumber: Int
) : DataPackage<List<T>>(list)