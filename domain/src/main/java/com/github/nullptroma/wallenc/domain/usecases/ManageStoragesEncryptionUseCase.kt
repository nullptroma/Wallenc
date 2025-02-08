package com.github.nullptroma.wallenc.domain.usecases

import com.github.nullptroma.wallenc.domain.datatypes.EncryptKey
import com.github.nullptroma.wallenc.domain.encrypt.Encryptor
import com.github.nullptroma.wallenc.domain.interfaces.IStorage
import com.github.nullptroma.wallenc.domain.interfaces.IStorageInfo
import com.github.nullptroma.wallenc.domain.interfaces.IUnlockManager

class ManageStoragesEncryptionUseCase(private val unlockManager: IUnlockManager) {
    suspend fun enableEncryption(storage: IStorageInfo, key: EncryptKey, encryptPath: Boolean) {
        when(storage) {
            is IStorage -> {
                if(storage.metaInfo.value.encInfo != null)
                    throw Exception() // TODO
                storage.setEncInfo(Encryptor.generateEncryptionInfo(key, encryptPath))
            }
        }
    }

    suspend fun openStorage(storage: IStorageInfo, key: EncryptKey): IStorageInfo {
        when(storage) {
            is IStorage -> {
                return unlockManager.open(storage, key)
            }
        }
    }
}