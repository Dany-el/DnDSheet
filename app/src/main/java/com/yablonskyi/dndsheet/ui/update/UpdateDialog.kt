package com.yablonskyi.dndsheet.ui.update

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yablonskyi.dndsheet.R
import com.yablonskyi.dndsheet.data.update.AppUpdate

@Composable
fun UpdateDialog(
    state: UpdateViewModel.UpdateState,
    onUpdate: (AppUpdate) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (state) {
        is UpdateViewModel.UpdateState.Available -> {
            AlertDialog(
                modifier = modifier,
                onDismissRequest = onDismiss,
                icon = {
                    Icon(Icons.Default.SystemUpdate, contentDescription = null)
                },
                title = {
                    Text(stringResource(R.string.update_available, state.update.versionName))
                },
                text = {
                    Text(state.update.releaseNotes)
                },
                confirmButton = {
                    Button(onClick = { onUpdate(state.update) }) {
                        Text(stringResource(R.string.update_now))
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.later))
                    }
                }
            )
        }

        is UpdateViewModel.UpdateState.Downloading -> {
            AlertDialog(
                modifier = modifier,
                onDismissRequest = {},  // block dismiss during download
                icon = {
                    Icon(Icons.Default.Download, contentDescription = null)
                },
                title = {
                    Text(stringResource(R.string.downloading_update))
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        LinearProgressIndicator(
                            progress = { state.progress },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "${(state.progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                },
                confirmButton = {}
            )
        }

        is UpdateViewModel.UpdateState.Error -> {
            AlertDialog(
                modifier = modifier,
                onDismissRequest = onDismiss,
                icon = {
                    Icon(
                        Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                title = { Text(stringResource(R.string.update_failed)) },
                text = { Text(stringResource(R.string.update_failed_message)) },
                confirmButton = {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.ok))
                    }
                }
            )
        }

        else -> {}
    }
}