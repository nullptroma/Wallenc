package com.github.nullptroma.wallenc.presentation.elements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextEditCancelOkDialog(onDismiss: () -> Unit, onConfirmation: (String) -> Unit, title: String, startString: String = "") {
    var name by remember { mutableStateOf(startString) }
    val focusRequester = remember { FocusRequester() }
    BasicAlertDialog(
        onDismissRequest = { onDismiss() }
    ) {
        Card {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                TextField(modifier = Modifier.focusRequester(focusRequester), value = name, onValueChange = {
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

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmationCancelOkDialog(onDismiss: () -> Unit, onConfirmation: () -> Unit, title: String) {
    BasicAlertDialog(
        onDismissRequest = { onDismiss() }
    ) {
        Card {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge)
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
                        onConfirmation()
                    }) {
                        Text("Ok")
                    }
                }
            }
        }
    }
}