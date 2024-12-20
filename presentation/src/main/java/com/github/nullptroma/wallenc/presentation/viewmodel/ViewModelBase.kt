package com.github.nullptroma.wallenc.presentation.viewmodel


import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber

abstract class ViewModelBase<TState>(initState: TState) : ViewModel() {
    private val _state = MutableStateFlow<TState>(initState)

    init {
        Timber.d("Init ViewModel ${this.javaClass.name}")
    }

    val state: StateFlow<TState>
        get() = _state

    protected fun updateState(newState: TState) {
        _state.value = newState
    }
}