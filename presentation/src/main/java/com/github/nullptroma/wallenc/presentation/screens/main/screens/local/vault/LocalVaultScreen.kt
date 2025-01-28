package com.github.nullptroma.wallenc.presentation.screens.main.screens.local.vault

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.nullptroma.wallenc.domain.datatypes.Tree
import com.github.nullptroma.wallenc.domain.interfaces.IStorageInfo
import com.github.nullptroma.wallenc.presentation.R
import com.github.nullptroma.wallenc.presentation.elements.StorageTree
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalVaultScreen(
    modifier: Modifier = Modifier,
    viewModel: LocalVaultViewModel = hiltViewModel(),
    openTextEdit: (String) -> Unit
) {

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
            items(uiState.storagesList) { listItem ->
                StorageTree(
                    modifier = Modifier.padding(8.dp),
                    tree = listItem,
                    onClick = {
                        openTextEdit(it.value.uuid.toString())
                    },
                    onRename = { tree, newName ->
                        viewModel.rename(tree.value, newName)
                    }
                )
            }
        }
    }
}

