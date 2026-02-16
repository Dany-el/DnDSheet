package com.yablonskyi.dndsheet.ui.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.yablonskyi.dndsheet.R

@Composable
fun DeletingItemConfirmDialog(
    title: String,
    text: String,
    onDiscard: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDiscard,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDiscard()
                }
            ) {
                Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDiscard
            ) {
                Text(stringResource(R.string.cancel))
            }
        },
        icon = { Icon(Icons.Default.Delete, contentDescription = null) },
        modifier = modifier
    )
}