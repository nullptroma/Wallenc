package com.github.nullptroma.wallenc.presentation.elements

import android.app.Dialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.nullptroma.wallenc.domain.datatypes.Tree
import com.github.nullptroma.wallenc.domain.interfaces.IStorageInfo
import com.github.nullptroma.wallenc.presentation.R

@Composable
fun StorageTree(
    modifier: Modifier,
    tree: Tree<IStorageInfo>,
    onClick: (Tree<IStorageInfo>) -> Unit,
    onRename: (Tree<IStorageInfo>, String) -> Unit,
) {
    val cur = tree.value
    val cardShape = RoundedCornerShape(30.dp)
    Column(modifier) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .clickable {
                    onClick(tree)
                    //viewModel.printStorageInfoToLog(cur)
                },
            shape = cardShape,
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            ),
        ) {
            val available by cur.isAvailable.collectAsStateWithLifecycle()
            val numOfFiles by cur.numberOfFiles.collectAsStateWithLifecycle()
            val size by cur.size.collectAsStateWithLifecycle()
            val metaInfo by cur.metaInfo.collectAsStateWithLifecycle()

            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(metaInfo.name ?: stringResource(R.string.no_name))
                    Text(
                        text = "IsAvailable: $available"
                    )
                    Text("Files: $numOfFiles")
                    Text("Size: $size")
                    Text("IsVirtual: ${cur.isVirtualStorage}")
                }
                Column(
                    modifier = Modifier,
                    horizontalAlignment = Alignment.End
                ) {
                    Box(modifier = Modifier.padding(0.dp, 8.dp, 8.dp, 0.dp)) {
                        var expanded by remember { mutableStateOf(false) }
                        var showRenameDialog by remember { mutableStateOf(false) }
                        IconButton(onClick = { expanded = !expanded }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.show_storage_item_menu)
                            )
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                onClick = {
                                    expanded = false
                                    showRenameDialog = true
                                },
                                text = { Text(stringResource(R.string.rename)) }
                            )
                        }

                        if (showRenameDialog) {
                            RenameDialog(
                                onDismiss = { showRenameDialog = false },
                                onConfirmation = { newName ->
                                    showRenameDialog = false
                                    onRename(tree, newName)
                                },
                                startName = tree.value.metaInfo.value.name ?: ""
                            )
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp, 0.dp, 12.dp, 8.dp)
                            .align(Alignment.End),
                        text = cur.uuid.toString(),
                        textAlign = TextAlign.End,
                        fontSize = 8.sp,
                        style = LocalTextStyle.current.copy(
                            platformStyle = PlatformTextStyle(
                                includeFontPadding = true
                            )
                        )
                    )
                }
            }
        }
        for (i in tree.children ?: listOf()) {
            StorageTree(Modifier.padding(16.dp, 0.dp, 0.dp, 0.dp), i, onClick, onRename)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenameDialog(onDismiss: () -> Unit, onConfirmation: (String) -> Unit, startName: String = "") {
    var name by remember { mutableStateOf(startName) }
    BasicAlertDialog(
        onDismissRequest = { onDismiss() }
    ) {
        Card {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("New name", style = MaterialTheme.typography.titleLarge)
                TextField(name, {
                    name = it
                })
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    Button(modifier = Modifier.weight(1f), onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(modifier = Modifier.weight(1f), onClick = {
                        onConfirmation(name)
                    }) {
                        Text("Ok")
                    }
                }
            }
        }
    }
}
