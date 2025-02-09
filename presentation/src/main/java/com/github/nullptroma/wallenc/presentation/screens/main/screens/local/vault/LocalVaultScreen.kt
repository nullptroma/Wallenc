package com.github.nullptroma.wallenc.presentation.screens.main.screens.local.vault

import android.widget.ProgressBar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.nullptroma.wallenc.presentation.elements.StorageTree
import com.github.nullptroma.wallenc.presentation.extensions.gesturesDisabled

@Composable
fun LocalVaultScreen(
    modifier: Modifier = Modifier,
    viewModel: LocalVaultViewModel = hiltViewModel(),
    openTextEdit: (String) -> Unit
) {

    val uiState by viewModel.state.collectAsStateWithLifecycle()
    Box {
        Scaffold(modifier = modifier, contentWindowInsets = WindowInsets(0.dp), floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.createStorage()
                },
            ) {
                Icon(Icons.Filled.Add, "Floating action button.")
            }
        }) { innerPadding ->
            LazyColumn(modifier = Modifier.padding(innerPadding).gesturesDisabled(uiState.isLoading)) {
                items(uiState.storagesList) { listItem ->
                    StorageTree(
                        modifier = Modifier.padding(8.dp, 8.dp, 8.dp, 0.dp),
                        tree = listItem,
                        onClick = {
                            openTextEdit(it.value.uuid.toString())
                        },
                        onRename = { tree, newName ->
                            viewModel.rename(tree.value, newName)
                        },
                        onRemove = { tree ->
                            viewModel.remove(tree.value)
                        },
                        onEncrypt = { tree ->
                            viewModel.enableEncryptionAndOpenStorage(tree.value)
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        if(uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.fillMaxSize().alpha(0.6f).background(Color.Black))
                CircularProgressIndicator(
                    modifier = Modifier.width(64.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        }
    }
}

