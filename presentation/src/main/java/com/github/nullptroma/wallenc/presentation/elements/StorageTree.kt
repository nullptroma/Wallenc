package com.github.nullptroma.wallenc.presentation.elements

import android.widget.FrameLayout
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.nullptroma.wallenc.domain.datatypes.Tree
import com.github.nullptroma.wallenc.domain.interfaces.IStorageInfo
import com.github.nullptroma.wallenc.presentation.R
import com.github.nullptroma.wallenc.presentation.utils.debouncedLambda

@Composable
fun StorageTree(
    modifier: Modifier,
    tree: Tree<IStorageInfo>,
    onClick: (Tree<IStorageInfo>) -> Unit,
    onRename: (Tree<IStorageInfo>, String) -> Unit,
    onRemove: (Tree<IStorageInfo>) -> Unit,
    onEncrypt: (Tree<IStorageInfo>) -> Unit,
) {
    val cur = tree.value
    val available by cur.isAvailable.collectAsStateWithLifecycle()
    val numOfFiles by cur.numberOfFiles.collectAsStateWithLifecycle()
    val size by cur.size.collectAsStateWithLifecycle()
    val metaInfo by cur.metaInfo.collectAsStateWithLifecycle()
    val borderColor =
        if (cur.isVirtualStorage) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
    Column(modifier) {
        Box(modifier = Modifier
            .height(IntrinsicSize.Min)
            .zIndex(100f)) {
            val interactionSource = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .clip(
                        CardDefaults.shape
                    )
                    .padding(0.dp, 0.dp, 16.dp, 0.dp)
                    .fillMaxSize()
                    .background(borderColor)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = ripple(),
                        enabled = false,
                        onClick = { }
                    )
            )
            Card(
                interactionSource = interactionSource,
                modifier = Modifier
                    .padding(8.dp, 0.dp, 0.dp, 0.dp)
                    .fillMaxWidth(),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                ),
                onClick = debouncedLambda(debounceMs = 500) {
                    onClick(tree)
                }
            ) {


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
                            var showRemoveConfirmationDiaglog by remember { mutableStateOf(false) }
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
                                HorizontalDivider()
                                DropdownMenuItem(
                                    onClick = {
                                        expanded = false
                                        showRemoveConfirmationDiaglog = true;
                                    },
                                    text = { Text(stringResource(R.string.remove)) }
                                )
                            }

                            if (showRenameDialog) {
                                TextEditCancelOkDialog(
                                    onDismiss = { showRenameDialog = false },
                                    onConfirmation = { newName ->
                                        showRenameDialog = false
                                        onRename(tree, newName)
                                    },
                                    title = stringResource(R.string.new_name_title),
                                    startString = metaInfo.name ?: ""
                                )
                            }

                            if (showRemoveConfirmationDiaglog) {
                                ConfirmationCancelOkDialog(
                                    onDismiss = {
                                        showRemoveConfirmationDiaglog = false
                                    },
                                    onConfirmation = {
                                        showRemoveConfirmationDiaglog = false
                                        onRemove(tree)
                                    },
                                    title = stringResource(
                                        R.string.remove_confirmation_dialog,
                                        metaInfo.name ?: "<noname>"
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Button(onClick = { onEncrypt(tree) }, enabled = metaInfo.encInfo == null) {
                            Text("Encrypt")
                        }
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
        }
        for (i in tree.children ?: listOf()) {
            StorageTree(
                Modifier
                    .padding(16.dp, 0.dp, 0.dp, 0.dp)
                    .offset(y = (-4).dp),
                i,
                onClick,
                onRename,
                onRemove,
                onEncrypt
            )
        }
    }
}

