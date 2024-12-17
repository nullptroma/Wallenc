package com.github.nullptroma.wallenc.data.vaults

import android.content.Context
import com.github.nullptroma.wallenc.domain.datatypes.EncryptKey
import com.github.nullptroma.wallenc.domain.enums.VaultType
import com.github.nullptroma.wallenc.domain.models.IStorage
import com.github.nullptroma.wallenc.domain.models.IVault
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class LocalVault(private val ioDispatcher: CoroutineDispatcher, context: Context) : IVault {
    init {
        CoroutineScope(ioDispatcher).launch {

        }
    }

    override suspend fun createStorage(name: String): IStorage = withContext(ioDispatcher) {
        TODO("Not yet implemented")
    }

    override suspend fun createStorage(
        name: String,
        key: EncryptKey
    ): IStorage = withContext(ioDispatcher) {
        TODO("Not yet implemented")
    }

    override suspend fun createStorage(
        name: String,
        key: EncryptKey,
        uuid: UUID
    ): IStorage = withContext(ioDispatcher) {
        TODO("Not yet implemented")
    }

    override suspend fun remove(storage: IStorage) = withContext(ioDispatcher) {
        TODO("Not yet implemented")
    }

    override val type: VaultType = VaultType.LOCAL
    override val uuid: UUID
        get() = TODO("Not yet implemented")
    override val storages: StateFlow<List<IStorage>>
        get() = TODO("Not yet implemented")
    override val isAvailable: StateFlow<Boolean>
        get() = TODO("Not yet implemented")

}