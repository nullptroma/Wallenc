package com.github.nullptroma.wallenc.data.db.app.repository

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.nullptroma.wallenc.data.db.app.dao.StorageMetaInfoDao
import com.github.nullptroma.wallenc.data.db.app.model.DbStorageMetaInfo
import com.github.nullptroma.wallenc.domain.common.impl.CommonStorageMetaInfo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class StorageMetaInfoRepository(
    private val dao: StorageMetaInfoDao,
    private val ioDispatcher: CoroutineDispatcher
) {
    fun getAllFlow() = dao.getAllFlow()
    suspend fun getAll() = withContext(ioDispatcher) { dao.getAll() }
    suspend fun getMeta(uuid: UUID): CommonStorageMetaInfo? = withContext(ioDispatcher)  {
        val json = dao.getMetaInfo(uuid)?.metaInfoJson ?: return@withContext null
        return@withContext jackson.readValue(
            json,
            CommonStorageMetaInfo::class.java
        )
    }

    fun observeMeta(uuid: UUID): Flow<CommonStorageMetaInfo> {
        return dao.getMetaInfoFlow(uuid)
            .map { jackson.readValue(it.metaInfoJson, CommonStorageMetaInfo::class.java) }
    }

    suspend fun setMeta(uuid: UUID, metaInfo: CommonStorageMetaInfo) = withContext(ioDispatcher)  {
        val json = jackson.writeValueAsString(metaInfo)
        dao.add(DbStorageMetaInfo(uuid, json))
    }

    fun createSingleStorageProvider(uuid: UUID): SingleStorageMetaInfoProvider {
        return SingleStorageMetaInfoProvider(this, uuid)
    }

    class SingleStorageMetaInfoProvider (
        private val repo: StorageMetaInfoRepository,
        val uuid: UUID
    ) {
        suspend fun get(): CommonStorageMetaInfo? = repo.getMeta(uuid)
        suspend fun set(metaInfo: CommonStorageMetaInfo) = repo.setMeta(uuid, metaInfo)
    }

    companion object {
        private val jackson = jacksonObjectMapper().apply { findAndRegisterModules() }
    }
}