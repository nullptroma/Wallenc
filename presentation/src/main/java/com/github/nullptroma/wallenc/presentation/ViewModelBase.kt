package com.github.nullptroma.wallenc.presentation


import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

abstract class ViewModelBase<TState>(initState: TState) : ViewModel() {
    private val _state = MutableStateFlow<TState>(initState)

    val state: StateFlow<TState>
        get() = _state

    protected fun updateState(newState: TState) {
        _state.value = newState
    }
}