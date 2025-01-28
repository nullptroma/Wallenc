package com.github.nullptroma.wallenc.domain.usecases

import com.github.nullptroma.wallenc.domain.interfaces.IStorage
import com.github.nullptroma.wallenc.domain.interfaces.IStorageInfo

class RenameStorageUseCase {
    suspend fun rename(storage: IStorageInfo, newName: String) {
        when (storage) {
            is IStorage -> storage.rename(newName)
        }
    }
}