package com.yablonskyi.ui.animation.utils

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp

@Composable
fun StartupSlide(
    fromY: Dp,
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
    content: @Composable () -> Unit,
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 350,
                delayMillis = delayMillis,
                easing = FastOutSlowInEasing,
            ),
        )
    }

    Box(
        modifier = modifier.graphicsLayer {
            alpha = progress.value
            translationY = fromY.toPx() * (1f - progress.value)
        },
    ) {
        content()
    }
}