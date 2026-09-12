package com.yablonskyi.ui.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.yablonskyi.ui.theme.DnDSheetTheme

object PreviewThemeWrapper {
    @Composable
    fun Preview(content: @Composable () -> Unit) {
        DnDSheetTheme {
            Surface(color = MaterialTheme.colorScheme.background) {
                content()
            }
        }
    }
}