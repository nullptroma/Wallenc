package com.github.nullptroma.wallenc.domain.interfaces

interface IStorage: IStorageInfo {
    val accessor: IStorageAccessor

    suspend fun rename(newName: String)
}
