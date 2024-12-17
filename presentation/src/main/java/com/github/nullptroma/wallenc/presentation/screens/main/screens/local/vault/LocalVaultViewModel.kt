package com.github.nullptroma.wallenc.presentation.screens.main.screens.local.vault

import androidx.lifecycle.viewModelScope
import com.github.nullptroma.wallenc.domain.usecases.GetAllRawStoragesUseCase
import com.github.nullptroma.wallenc.presentation.viewmodel.ViewModelBase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocalVaultViewModel @Inject constructor(private val getAllRawStoragesUseCase: GetAllRawStoragesUseCase) :
    ViewModelBase<LocalVaultScreenState>(LocalVaultScreenState(listOf())) {
    init {
        viewModelScope.launch {
            getAllRawStoragesUseCase.localStorage.storages.collect {
                mutableUiState.value = mutableUiState.value.copy(
                    storagesList = it
                )
            }
        }
    }
}