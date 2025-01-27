package com.github.nullptroma.wallenc.data.utils

interface IProvider<T> {
    suspend fun get(): T?
    suspend fun set(value: T)
    suspend fun clear()
}