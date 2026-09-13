package com.yablonskyi.character.platform.files

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

@Composable
internal fun rememberCharacterImportLauncher(onResult: (String?) -> Unit): ManagedActivityResultLauncher<Array<String>, Uri?> =
    rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { onResult(it?.toString()) }

@Composable
internal fun rememberCharacterExportLauncher(onResult: (String?) -> Unit): ManagedActivityResultLauncher<String, Uri?> =
    rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { onResult(it?.toString()) }
