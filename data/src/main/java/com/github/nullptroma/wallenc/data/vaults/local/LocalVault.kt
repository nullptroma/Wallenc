package com.github.nullptroma.wallenc.data.vaults.local

import android.content.Context
import com.github.nullptroma.wallenc.data.MockStorage
import com.github.nullptroma.wallenc.domain.datatypes.EncryptKey
import com.github.nullptroma.wallenc.domain.enums.VaultType
import com.github.nullptroma.wallenc.domain.models.IStorage
import com.github.nullptroma.wallenc.domain.models.IVault
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.io.path.Path
import kotlin.io.path.createDirectory

class LocalVault(private val ioDispatcher: CoroutineDispatcher, context: Context) : IVault {
    private val path = context.getExternalFilesDir("LocalVault")
    private val _storages = MutableStateFlow(listOf<IStorage>())

    init {
        CoroutineScope(ioDispatcher).launch {
            if(path == null)
                return@launch

            val dirs = path.listFiles()?.filter { it.isDirectory }
            if(dirs != null)
                _storages.value = dirs.map {
                    MockStorage(uuid = UUID.fromString(it.name), accessor = LocalStorageAccessor())
                }
            val next = Path(path.path, UUID.randomUUID().toString())
            next.createDirectory()
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
        get() = _storages
    override val isAvailable: StateFlow<Boolean>
        get() = TODO("Not yet implemented")

}