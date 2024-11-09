package com.github.nullptroma.wallenc.presentation.screens.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.nullptroma.wallenc.presentation.screens.settings.SettingsRoute


@androidx.compose.runtime.Composable
fun MainScreen(modifier: Modifier = Modifier,
               viewModel: MainViewModel = hiltViewModel(),
               onSettingsRoute: (SettingsRoute) -> Unit) {
    val state = viewModel.stateFlow
    var text by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    Column(modifier = modifier.imePadding().fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        TextField(text, onValueChange = { s ->
            text = s
        })
        Button( onClick = {
            focusManager.clearFocus()
            onSettingsRoute(SettingsRoute(text))
        }) {
            Text("Press Me!")
        }
    }
}