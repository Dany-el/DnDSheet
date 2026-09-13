package com.yablonskyi.ui.utils

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Composable
fun rememberFileOperationHandler(
    effects: Flow<FileOperationEffect>,
    onExportCompleted: (success: Boolean) -> Unit,
    onImportReady: (json: String) -> Unit,
    onShareFailed: () -> Unit = {},
) {
    val currentOnExportCompleted by rememberUpdatedState(onExportCompleted)
    val currentOnImportReady by rememberUpdatedState(onImportReady)
    val currentOnShareFailed by rememberUpdatedState(onShareFailed)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    var pendingJson by remember { mutableStateOf<String?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri ?: run {
            currentOnExportCompleted(false)
            return@rememberLauncherForActivityResult
        }
        pendingJson?.let { json ->
            scope.launch(Dispatchers.IO) {
                val success = runCatching {
                    context.contentResolver.openOutputStream(uri)
                        ?.use { it.write(json.toByteArray()) }
                }.getOrNull() != null
                currentOnExportCompleted(success)
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch(Dispatchers.IO) {
            val json = runCatching {
                context.contentResolver.openInputStream(uri)
                    ?.bufferedReader()
                    ?.use { it.readText() }
            }.getOrNull() ?: ""
            currentOnImportReady(json)
        }
    }

    LaunchedEffect(effects, lifecycleOwner, context) {
        effects.flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .collect { effect ->
                when (effect) {
                    is FileOperationEffect.ExportReady -> {
                        pendingJson = effect.json
                        exportLauncher.launch(effect.fileName)
                    }

                    is FileOperationEffect.ShareReady -> {
                        try {
                            val intent = prepareJsonShareIntent(context, effect)
                            context.startActivity(android.content.Intent.createChooser(intent, null))
                        } catch (cancelled: CancellationException) {
                            throw cancelled
                        } catch (_: Exception) {
                            currentOnShareFailed()
                        }
                    }

                    FileOperationEffect.OpenImportPicker ->
                        importLauncher.launch(arrayOf("application/json", "*/*"))
                }
            }
    }
}
