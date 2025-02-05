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
import com.github.nullptroma.wallenc.domain.usecases.RenameStorageUseCase
import com.github.nullptroma.wallenc.domain.usecases.StorageFileManagementUseCase
import com.github.nullptroma.wallenc.presentation.extensions.toPrintable
import com.github.nullptroma.wallenc.presentation.ViewModelBase
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
    private val renameStorageUseCase: RenameStorageUseCase,
    private val logger: ILogger
) : ViewModelBase<LocalVaultScreenState>(LocalVaultScreenState(listOf())) {
    init {
        viewModelScope.launch {
            manageLocalVaultUseCase.localStorages.combine(getOpenedStoragesUseCase.openedStorages) { local, opened ->
                val list = mutableListOf<Tree<IStorageInfo>>()
                for (storage in local) {
                    var tree = Tree(storage)
                    list.add(tree)
                    while(opened != null && opened.containsKey(tree.value.uuid)) {
                        val child = opened.getValue(tree.value.uuid)
                        val nextTree = Tree(child)
                        tree.children = listOf(nextTree)
                        tree = nextTree
                    }
                }
                return@combine list
            }.collectLatest {
                val newState = state.value.copy(
                    storagesList = it
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
        viewModelScope.launch {
            manageLocalVaultUseCase.createStorage(EncryptKey("Hello"))
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