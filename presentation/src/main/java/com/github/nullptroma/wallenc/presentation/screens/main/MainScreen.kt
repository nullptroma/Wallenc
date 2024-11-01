package com.github.nullptroma.wallenc.presentation.screens.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel


@androidx.compose.runtime.Composable
fun MainScreen(modifier: Modifier = Modifier.Companion, viewModel: MainViewModel = hiltViewModel()) {
    val state = viewModel.stateFlow
    Column(modifier = modifier.imePadding()) {

        Text(text = state.value)
        Box(
            modifier = Modifier.Companion.fillMaxSize(),
            contentAlignment = Alignment.Companion.BottomCenter
        ) {
            TextField("", onValueChange = {

            })
        }
    }
}