package com.github.nullptroma.wallenc.presentation.screens.main.screens.local.vault

import androidx.lifecycle.viewModelScope
import com.github.nullptroma.wallenc.domain.datatypes.EncryptKey
import com.github.nullptroma.wallenc.domain.interfaces.IDirectory
import com.github.nullptroma.wallenc.domain.interfaces.IFile
import com.github.nullptroma.wallenc.domain.interfaces.ILogger
import com.github.nullptroma.wallenc.domain.interfaces.IStorageInfo
import com.github.nullptroma.wallenc.domain.usecases.GetOpenedStoragesUseCase
import com.github.nullptroma.wallenc.domain.usecases.ManageLocalVaultUseCase
import com.github.nullptroma.wallenc.domain.usecases.StorageFileManagementUseCase
import com.github.nullptroma.wallenc.presentation.extensions.toPrintable
import com.github.nullptroma.wallenc.presentation.viewmodel.ViewModelBase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.system.measureTimeMillis

@HiltViewModel
class LocalVaultViewModel @Inject constructor(
    private val _manageLocalVaultUseCase: ManageLocalVaultUseCase,
    private val _getOpenedStoragesUseCase: GetOpenedStoragesUseCase,
    private val _storageFileManagementUseCase: StorageFileManagementUseCase,
    private val logger: ILogger
) :
    ViewModelBase<LocalVaultScreenState>(LocalVaultScreenState(listOf())) {
    init {
        viewModelScope.launch {
            _manageLocalVaultUseCase.localStorages.combine(_getOpenedStoragesUseCase.openedStorages) { local, opened ->
                local + (opened?.map { it.value } ?: listOf())
            }.collectLatest {
                val newState = state.value.copy(
                    storagesList = it
                )
                updateState(newState)
            }
        }
    }

    fun printStorageInfoToLog(storage: IStorageInfo) {
        _storageFileManagementUseCase.setStorage(storage)
        viewModelScope.launch {
            val files: List<IFile>
            val dirs: List<IDirectory>
            val time = measureTimeMillis {
                files = _storageFileManagementUseCase.getAllFiles()
                dirs = _storageFileManagementUseCase.getAllDirs()
            }
            for (file in files) {
                logger.debug("Files", file.metaInfo.toString())
            }
            for (dir in dirs) {
                logger.debug("Dirs", dir.metaInfo.toString())
            }
            logger.debug("Time", "Time: $time ms")
            logger.debug("Storage", storage.toPrintable())
        }
    }

    fun createStorage() {
        viewModelScope.launch {
            _manageLocalVaultUseCase.createStorage(EncryptKey("hello"))
        }
    }
}