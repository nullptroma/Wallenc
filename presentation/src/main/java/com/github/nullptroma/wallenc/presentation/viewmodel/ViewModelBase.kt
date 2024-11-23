package com.github.nullptroma.wallenc.presentation.viewmodel


import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class ViewModelBase<TState>(initState: TState) : ViewModel() {
    protected val mutableUiState = MutableStateFlow<TState>(initState)

    val uiState: StateFlow<TState>
        get() = mutableUiState.asStateFlow()
}