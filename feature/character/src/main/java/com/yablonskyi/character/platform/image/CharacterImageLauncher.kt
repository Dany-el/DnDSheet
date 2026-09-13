package com.yablonskyi.character.platform.image

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

@Composable
internal fun rememberCharacterImageLauncher(onResult: (String?) -> Unit): ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?> =
    rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { onResult(it?.toString()) }
