package com.github.nullptroma.wallenc.presentation.screens.main.screens.local.vault

import androidx.lifecycle.viewModelScope
import com.github.nullptroma.wallenc.domain.interfaces.IDirectory
import com.github.nullptroma.wallenc.domain.interfaces.IFile
import com.github.nullptroma.wallenc.domain.interfaces.IStorageInfo
import com.github.nullptroma.wallenc.domain.usecases.ManageLocalVaultUseCase
import com.github.nullptroma.wallenc.domain.usecases.StorageFileManagementUseCase
import com.github.nullptroma.wallenc.presentation.viewmodel.ViewModelBase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.system.measureTimeMillis

@HiltViewModel
class LocalVaultViewModel @Inject constructor(
    private val _manageLocalVaultUseCase: ManageLocalVaultUseCase,
    private val _storageFileManagementUseCase: StorageFileManagementUseCase,
) :
    ViewModelBase<LocalVaultScreenState>(LocalVaultScreenState(listOf())) {
    init {
        viewModelScope.launch {
            _manageLocalVaultUseCase.localStorages.collect {
                val newState = state.value.copy(
                    storagesList = it
                )
                updateState(newState)
            }
        }
    }

    fun printAllFilesToLog(storage: IStorageInfo) {
        _storageFileManagementUseCase.setStorage(storage)
        viewModelScope.launch {
            val files: List<IFile>
            val dirs: List<IDirectory>
            val time = measureTimeMillis {
                files = _storageFileManagementUseCase.getAllFiles()
                dirs = _storageFileManagementUseCase.getAllDirs()
            }
            for (file in files) {
                Timber.tag("Files")
                Timber.d(file.metaInfo.toString())
            }
            for (dir in dirs) {
                Timber.tag("Dirs")
                Timber.d(dir.metaInfo.toString())
            }
            Timber.d("Time: $time ms")
        }
    }

    fun createStorage() {
        viewModelScope.launch {
            _manageLocalVaultUseCase.createStorage()
        }
    }
}