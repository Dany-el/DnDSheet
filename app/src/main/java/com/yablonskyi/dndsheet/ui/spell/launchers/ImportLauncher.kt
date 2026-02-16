package com.yablonskyi.dndsheet.ui.spell.launchers

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.yablonskyi.dndsheet.R
import com.yablonskyi.dndsheet.data.model.character.Spell
import com.yablonskyi.dndsheet.ui.utils.importSpellsFromJson
import kotlinx.coroutines.launch

@Composable
fun rememberImportLauncher(
    onLoadingStateChange: (Boolean) -> Unit,
    failureMsg: String = stringResource(R.string.failure_export),
    onImportSpells: (List<Spell>) -> Unit
): ManagedActivityResultLauncher<Array<String>, Uri?> {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                onLoadingStateChange(true)
                try{
                    val result = importSpellsFromJson(context, uri)
                    result.onSuccess { spells ->
                        onImportSpells(spells)
                    }.onFailure {
                        Toast.makeText(
                            context,
                            failureMsg,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }  catch (e: Exception) {
                    Toast.makeText(context, failureMsg, Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                } finally {
                    onLoadingStateChange(false)
                }
            }
        }
    }
}