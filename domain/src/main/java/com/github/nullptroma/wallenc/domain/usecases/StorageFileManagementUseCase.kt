package com.github.nullptroma.wallenc.domain.usecases

import com.github.nullptroma.wallenc.domain.interfaces.IDirectory
import com.github.nullptroma.wallenc.domain.interfaces.IFile
import com.github.nullptroma.wallenc.domain.interfaces.IStorage

class StorageFileManagementUseCase {
    private var _storage: IStorage? = null

    fun setStorage(storage: IStorage) {
        _storage = storage
    }

    suspend fun getAllFiles(): List<IFile> {
        val storage = _storage ?: return listOf()
        return storage.accessor.getAllFiles()
    }

    suspend fun getAllDirs(): List<IDirectory> {
        val storage = _storage ?: return listOf()
        return storage.accessor.getAllDirs()
    }
}