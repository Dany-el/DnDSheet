package com.yablonskyi.dndsheet.ui.utils

import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember

@Composable
fun rememberDebouncedClick(
    cooldownMs: Long = 700L,
    onClick: () -> Unit
): () -> Unit {
    val lastClickTime = remember { mutableLongStateOf(0L) }

    return {
        val currentTime = SystemClock.elapsedRealtime()
        if (currentTime - lastClickTime.longValue > cooldownMs) {
            lastClickTime.longValue = currentTime
            onClick()
        }
    }
}