package com.github.nullptroma.wallenc.presentation.screens.main.screens.local.vault

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalVaultScreen(modifier: Modifier = Modifier,
               viewModel: LocalVaultViewModel = hiltViewModel()) {

    val uiState by viewModel.state.collectAsStateWithLifecycle()
    LazyColumn(modifier = modifier) {
        items(uiState.storagesList) {
            Card(modifier = Modifier.clickable {
                viewModel.printAllFilesToLog(it)
            }) {
                val available by it.isAvailable.collectAsStateWithLifecycle()
                val numOfFiles by it.numberOfFiles.collectAsStateWithLifecycle()
                val size by it.size.collectAsStateWithLifecycle()
                Column {
                    Text(it.uuid.toString())
                    Text("IsAvailable: $available")
                    Text("Files: $numOfFiles")
                    Text("Size: $size")
                }
            }
        }
    }
}