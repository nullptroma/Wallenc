package com.github.nullptroma.wallenc.app.di.modules.data

import android.content.Context
import com.github.nullptroma.wallenc.app.di.modules.app.IoDispatcher
import com.github.nullptroma.wallenc.data.vaults.VaultsManager
import com.github.nullptroma.wallenc.data.vaults.local.LocalVault
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
    fun provideLocalVault(@IoDispatcher ioDispatcher: CoroutineDispatcher,
                          @ApplicationContext context: Context): LocalVault {
        return LocalVault(ioDispatcher, context)
    }

    @Provides
    @Singleton
    fun provideVaultsManager(localVault: LocalVault): IVaultsManager {
        return VaultsManager(localVault)
    }
}