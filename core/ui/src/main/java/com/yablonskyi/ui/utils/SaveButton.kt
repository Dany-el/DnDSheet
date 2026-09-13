package com.yablonskyi.ui.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SaveButton(
    isEnabled: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = { if (isEnabled) onClick() },
        enabled = isEnabled,
        modifier = modifier
    ) {
        Icon(Icons.Default.Save, contentDescription = contentDescription)
    }
}