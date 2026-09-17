package com.yablonskyi.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@Composable
fun RoundedCheckBox(
    isChecked: Boolean,
    onCheckChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        targetValue = if (isChecked) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            Color.Transparent
        },
        animationSpec = tween(durationMillis = 180),
        label = "roundedCheckboxContainerColor",
    )
    val iconColor by animateColorAsState(
        targetValue = if (isChecked) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(durationMillis = 180),
        label = "roundedCheckboxIconColor",
    )

    Surface(
        checked = isChecked,
        onCheckedChange = onCheckChange,
        shape = CircleShape,
        color = containerColor,
        contentColor = iconColor,
        modifier = modifier.zIndex(1f),
    ) {
        AnimatedContent(
            targetState = isChecked,
            transitionSpec = {
                (fadeIn(tween(180)) + scaleIn(tween(180), initialScale = 0.6f))
                    .togetherWith(fadeOut(tween(120)) + scaleOut(tween(120), targetScale = 0.6f))
            },
            label = "roundedCheckboxIcon",
        ) { checked ->
            Icon(
                imageVector = if (checked) {
                    Icons.Default.CheckCircle
                } else {
                    Icons.Default.RadioButtonUnchecked
                },
                contentDescription = if (checked) {
                    "Deselect character"
                } else {
                    "Select character"
                },
                modifier = Modifier
                    .padding(1.dp)
                    .size(24.dp),
            )
        }
    }
}