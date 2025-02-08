package com.github.nullptroma.wallenc.app.di.modules.data

import android.content.Context
import com.github.nullptroma.wallenc.app.di.modules.app.IoDispatcher
import com.github.nullptroma.wallenc.data.db.app.dao.StorageKeyMapDao
import com.github.nullptroma.wallenc.data.db.app.dao.StorageMetaInfoDao
import com.github.nullptroma.wallenc.data.db.app.repository.StorageKeyMapRepository
import com.github.nullptroma.wallenc.data.db.app.repository.StorageMetaInfoRepository
import com.github.nullptroma.wallenc.data.storages.UnlockManager
import com.github.nullptroma.wallenc.data.vaults.VaultsManager
import com.github.nullptroma.wallenc.domain.interfaces.IUnlockManager
import com.github.nullptroma.wallenc.domain.interfaces.IVaultsManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class SingletonModule {
    @Provides
    @Singleton
    fun provideVaultsManager(
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
        @ApplicationContext context: Context
    ): IVaultsManager {
        return VaultsManager(ioDispatcher, context)
    }

    @Provides
    @Singleton
    fun provideStorageKeyMapRepository(
        dao: StorageKeyMapDao,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): StorageKeyMapRepository {
        return StorageKeyMapRepository(dao, ioDispatcher)
    }

    @Provides
    @Singleton
    fun provideStorageMetaInfoRepository(
        dao: StorageMetaInfoDao,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): StorageMetaInfoRepository {
        return StorageMetaInfoRepository(dao, ioDispatcher)
    }

    @Provides
    @Singleton
    fun provideUnlockManager(
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
        keyRepo: StorageKeyMapRepository,
        vaultsManager: IVaultsManager
    ): IUnlockManager {
        return UnlockManager(
            keymapRepository = keyRepo,
            ioDispatcher = ioDispatcher,
            vaultsManager = vaultsManager
        )
    }
}