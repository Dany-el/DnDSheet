package com.yablonskyi.dndsheet.ui.spell.launchers

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.yablonskyi.dndsheet.R
import com.yablonskyi.dndsheet.data.model.character.Spell
import com.yablonskyi.dndsheet.ui.utils.exportSpellsToJson
import kotlinx.coroutines.launch


@Composable
fun rememberExportLauncher(
    spellsToExport: List<Spell>,
    onLoadingStateChange: (Boolean) -> Unit,
    successMsg: String = stringResource(R.string.success_export),
    failureMsg: String = stringResource(R.string.failure_export),
    onExportComplete: () -> Unit = {}
): ManagedActivityResultLauncher<String, Uri?> {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val currentSpells by rememberUpdatedState(spellsToExport)
    val currentOnComplete by rememberUpdatedState(onExportComplete)

    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                onLoadingStateChange(true)
                try {
                    if (currentSpells.isEmpty()) return@launch

                    val result = exportSpellsToJson(context, uri, currentSpells)

                    result.onSuccess {
                        Toast.makeText(context, successMsg, Toast.LENGTH_SHORT).show()
                        currentOnComplete()
                    }.onFailure {
                        Toast.makeText(context, failureMsg, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, failureMsg, Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                } finally {
                    onLoadingStateChange(false)
                }
            }
        }
    }
}