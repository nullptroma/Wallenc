package com.github.nullptroma.wallenc.domain.usecases

import com.github.nullptroma.wallenc.domain.models.IFile
import com.github.nullptroma.wallenc.domain.models.IStorage

class StorageFileManagementUseCase {
    private var _storage: IStorage? = null

    fun setStorage(storage: IStorage) {
        _storage = storage
    }

    suspend fun getAllFiles(): List<IFile> {
        val storage = _storage ?: return listOf()
        return storage.accessor.getAllFiles()
    }
}