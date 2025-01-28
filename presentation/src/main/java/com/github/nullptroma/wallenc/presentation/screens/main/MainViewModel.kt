package com.github.nullptroma.wallenc.presentation.screens.main

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import com.github.nullptroma.wallenc.presentation.screens.ScreenRoute
import com.github.nullptroma.wallenc.presentation.screens.main.screens.local.vault.LocalVaultRoute
import com.github.nullptroma.wallenc.presentation.screens.main.screens.remotes.RemoteVaultsRoute
import com.github.nullptroma.wallenc.presentation.ViewModelBase
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class MainViewModel @javax.inject.Inject constructor(savedStateHandle: SavedStateHandle) :
    ViewModelBase<MainScreenState>(MainScreenState()) {

    @OptIn(SavedStateHandleSaveableApi::class)
    var routes by savedStateHandle.saveable {
        mutableStateOf(
            mapOf<String, ScreenRoute>(
                LocalVaultRoute::class.qualifiedName!! to LocalVaultRoute(),
                RemoteVaultsRoute::class.qualifiedName!! to RemoteVaultsRoute()
            )
        )
    }
        private set

    fun updateRoute(qualifiedName: String, route: ScreenRoute) {
        routes = routes.toMutableMap().apply {
            this[qualifiedName] = route
        }
    }
}