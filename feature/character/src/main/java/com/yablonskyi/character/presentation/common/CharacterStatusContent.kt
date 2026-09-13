package com.yablonskyi.character.presentation.common

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yablonskyi.ui.R

@Composable
internal fun CharacterStatusContent(
    status: CharacterLoadStatus,
    errors: Set<CharacterUiError>,
    onRetry: () -> Unit,
    onDismissError: () -> Unit,
    onBack: () -> Unit,
    content: @Composable () -> Unit,
) {
    if (status == CharacterLoadStatus.CONTENT) content()
    else Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
        if (status == CharacterLoadStatus.LOADING) CircularProgressIndicator()
        else Text(stringResource(if (status == CharacterLoadStatus.NOT_FOUND) R.string.character_not_found else R.string.character_load_failed))
        TextButton(onClick = onBack) { Text(stringResource(R.string.character_back)) }
        if (status != CharacterLoadStatus.LOADING) TextButton(onClick = onRetry) { Text(stringResource(R.string.character_retry)) }
    }
    if (errors.isNotEmpty()) AlertDialog(
        onDismissRequest = onDismissError,
        title = { Text(stringResource(R.string.character_operation_failed)) },
        text = { Text(stringResource(when (errors.first()) {
            CharacterUiError.LOAD -> R.string.character_load_failed
            CharacterUiError.SPELLS -> R.string.character_spells_failed
            CharacterUiError.ATTACKS -> R.string.character_attacks_failed
            CharacterUiError.SAVE -> R.string.character_save_failed
            CharacterUiError.IMAGE -> R.string.character_image_failed
        })) },
        confirmButton = { TextButton(onClick = onRetry) { Text(stringResource(R.string.character_retry)) } },
        dismissButton = { TextButton(onClick = onDismissError) { Text(stringResource(R.string.cancel)) } },
    )
}
