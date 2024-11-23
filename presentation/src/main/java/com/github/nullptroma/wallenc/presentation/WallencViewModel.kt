package com.github.nullptroma.wallenc.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import com.github.nullptroma.wallenc.presentation.screens.ScreenRoute
import com.github.nullptroma.wallenc.presentation.screens.main.MainRoute
import com.github.nullptroma.wallenc.presentation.screens.settings.SettingsRoute
import com.github.nullptroma.wallenc.presentation.viewmodel.ViewModelBase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlin.collections.set

@HiltViewModel
class WallencViewModel @javax.inject.Inject constructor(savedStateHandle: SavedStateHandle) :
    ViewModelBase<Unit>(Unit) {
    @OptIn(SavedStateHandleSaveableApi::class)
    var routes by savedStateHandle.saveable {
        mutableStateOf(
            mapOf<String, ScreenRoute>(
                MainRoute::class.qualifiedName!! to MainRoute(),
                SettingsRoute::class.qualifiedName!! to SettingsRoute()
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