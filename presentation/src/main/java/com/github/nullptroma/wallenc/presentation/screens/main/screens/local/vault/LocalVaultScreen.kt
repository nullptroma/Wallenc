package com.github.nullptroma.wallenc.presentation.screens.main.screens.local.vault

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalVaultScreen(modifier: Modifier = Modifier,
               viewModel: LocalVaultViewModel = hiltViewModel()) {
    Text("Local vault screen")
}