package com.github.nullptroma.wallenc.app.di.modules.data

import android.content.Context
import com.github.nullptroma.wallenc.data.db.RoomFactory
import com.github.nullptroma.wallenc.data.db.app.IAppDb
import com.github.nullptroma.wallenc.data.db.app.dao.StorageKeyDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(dagger.hilt.components.SingletonComponent::class)
class RoomModule {
    @Provides
    @Singleton
    fun provideRoomFactory(@ApplicationContext appContext: Context) : RoomFactory {
        return RoomFactory(appContext)
    }

    @Provides
    @Singleton
    fun provideStorageKeyDao(database: IAppDb): StorageKeyDao {
        return database.storageKeyDao
    }

    @Provides
    @Singleton
    fun provideAppDb(
        factory: RoomFactory
    ): IAppDb {
        return factory.buildAppDb()
    }
}