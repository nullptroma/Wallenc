package com.github.nullptroma.wallenc.presentation.screens.main.screens.local.vault

import androidx.lifecycle.viewModelScope
import com.github.nullptroma.wallenc.domain.datatypes.EncryptKey
import com.github.nullptroma.wallenc.domain.datatypes.Tree
import com.github.nullptroma.wallenc.domain.interfaces.IDirectory
import com.github.nullptroma.wallenc.domain.interfaces.IFile
import com.github.nullptroma.wallenc.domain.interfaces.ILogger
import com.github.nullptroma.wallenc.domain.interfaces.IStorageInfo
import com.github.nullptroma.wallenc.domain.usecases.GetOpenedStoragesUseCase
import com.github.nullptroma.wallenc.domain.usecases.ManageLocalVaultUseCase
import com.github.nullptroma.wallenc.domain.usecases.ManageStoragesEncryptionUseCase
import com.github.nullptroma.wallenc.domain.usecases.RenameStorageUseCase
import com.github.nullptroma.wallenc.domain.usecases.StorageFileManagementUseCase
import com.github.nullptroma.wallenc.presentation.ViewModelBase
import com.github.nullptroma.wallenc.presentation.extensions.toPrintable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.system.measureTimeMillis

@HiltViewModel
class LocalVaultViewModel @Inject constructor(
    private val manageLocalVaultUseCase: ManageLocalVaultUseCase,
    private val getOpenedStoragesUseCase: GetOpenedStoragesUseCase,
    private val storageFileManagementUseCase: StorageFileManagementUseCase,
    private val manageStoragesEncryptionUseCase: ManageStoragesEncryptionUseCase,
    private val renameStorageUseCase: RenameStorageUseCase,
    private val logger: ILogger
) : ViewModelBase<LocalVaultScreenState>(LocalVaultScreenState(listOf(), true)) {
    private var _taskCount: Int = 0
    private var tasksCount
        get() = _taskCount
        set(value) {
            _taskCount = value
            updateStateLoading()
        }

    private var _isLoading: Boolean = false
    private var isLoading
        get() = _isLoading
        set(value) {
            _isLoading = value
            updateStateLoading()
        }

    init {
        collectFlows()
    }

    private fun updateStateLoading() {
        updateState(state.value.copy(
            isLoading = this.isLoading || this.tasksCount > 0
        ))
    }

    private fun collectFlows() {
        viewModelScope.launch {
            manageLocalVaultUseCase.localStorages.combine(getOpenedStoragesUseCase.openedStorages) { local, opened ->
                if(local == null || opened == null)
                    return@combine null
                val list = mutableListOf<Tree<IStorageInfo>>()
                for (storage in local) {
                    var tree = Tree(storage)
                    list.add(tree)
                    while(opened.containsKey(tree.value.uuid)) {
                        val child = opened.getValue(tree.value.uuid)
                        val nextTree = Tree(child)
                        tree.children = listOf(nextTree)
                        tree = nextTree
                    }
                }
                return@combine list
            }.collectLatest {
                isLoading = it == null
                val newState = state.value.copy(
                    storagesList = it ?: listOf()
                )
                updateState(newState)
            }
        }
    }

    fun printStorageInfoToLog(storage: IStorageInfo) {
        storageFileManagementUseCase.setStorage(storage)
        viewModelScope.launch {
            val files: List<IFile>
            val dirs: List<IDirectory>
            val time = measureTimeMillis {
                files = storageFileManagementUseCase.getAllFiles()
                dirs = storageFileManagementUseCase.getAllDirs()
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
        tasksCount++
        viewModelScope.launch {
            manageLocalVaultUseCase.createStorage()
            tasksCount--
        }
    }

    private val runningStorages = mutableSetOf<IStorageInfo>()
    fun enableEncryptionAndOpenStorage(storage: IStorageInfo) {
        if(runningStorages.contains(storage))
            return
        tasksCount++
        runningStorages.add(storage)
        val key = EncryptKey("Hello")
        viewModelScope.launch {
            try {
                manageStoragesEncryptionUseCase.enableEncryption(storage, key, false)
                manageStoragesEncryptionUseCase.openStorage(storage, key)
            }
            finally {
                runningStorages.remove(storage)
                tasksCount--
            }
        }
    }

    fun rename(storage: IStorageInfo, newName: String) {
        viewModelScope.launch {
            renameStorageUseCase.rename(storage, newName)
        }
    }

    fun remove(storage: IStorageInfo) {
        viewModelScope.launch {
            manageLocalVaultUseCase.remove(storage)
        }
    }
}