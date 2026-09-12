package com.yablonskyi.character.launchers

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.yablonskyi.data.utils.importCharactersFromJson
import com.yablonskyi.model.character.CharacterSheet
import com.yablonskyi.ui.R
import kotlinx.coroutines.launch
import java.lang.Exception

@Composable
fun rememberCharacterImportLauncher(
    onLoadingStateChange: (Boolean) -> Unit,
    failureMsg: String = stringResource(R.string.failure_export),
    onImportCharacters: (List<CharacterSheet>) -> Unit
): ManagedActivityResultLauncher<Array<String>, Uri?> {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                onLoadingStateChange(true)
                try {
                    val result = importCharactersFromJson(context, uri)
                    result.onSuccess { sheet ->
                        onImportCharacters(sheet)
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