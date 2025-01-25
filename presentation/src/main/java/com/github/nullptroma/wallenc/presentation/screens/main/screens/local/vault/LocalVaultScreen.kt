package com.github.nullptroma.wallenc.presentation.screens.main.screens.local.vault

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalVaultScreen(modifier: Modifier = Modifier,
               viewModel: LocalVaultViewModel = hiltViewModel()) {

    val uiState by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(modifier = modifier, contentWindowInsets = WindowInsets(0.dp), floatingActionButton = {
        FloatingActionButton(
            onClick = {
                viewModel.createStorage()
            },
        ) {
            Icon(Icons.Filled.Add, "Floating action button.")
        }
    }) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            items(uiState.storagesList) {
                Card(modifier = Modifier.clickable { }.pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { _ -> viewModel.printStorageInfoToLog(it) }
                    )
                }.padding(8.dp)) {
                    val available by it.isAvailable.collectAsStateWithLifecycle()
                    val numOfFiles by it.numberOfFiles.collectAsStateWithLifecycle()
                    val size by it.size.collectAsStateWithLifecycle()
                    val metaInfo by it.metaInfo.collectAsStateWithLifecycle()

                    val enc = metaInfo.encInfo
                    Column(modifier = Modifier.padding(4.dp)) {
                        Text(it.uuid.toString())
                        Text("IsAvailable: $available")
                        Text("Files: $numOfFiles")
                        Text("Size: $size")
                        Text("Enc: $enc")
                    }
                }
            }
        }
    }
}