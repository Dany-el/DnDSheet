package com.yablonskyi.dndsheet.ui.utils

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SlicedDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    items: List<SlicedMenuItem>,
    modifier: Modifier = Modifier,
    shadowElevation: Dp = 2.dp,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.Transparent,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        items.forEachIndexed { index, item ->
            val shape = when {
                items.size == 1 -> RoundedCornerShape(16.dp)
                index == 0 -> RoundedCornerShape(
                    topStart = 16.dp, topEnd = 16.dp,
                    bottomStart = 4.dp, bottomEnd = 4.dp
                )

                index == items.lastIndex -> RoundedCornerShape(
                    topStart = 4.dp, topEnd = 4.dp,
                    bottomStart = 16.dp, bottomEnd = 16.dp
                )

                else -> MaterialTheme.shapes.extraSmall
            }

            val shadowPadding = 8.dp

            Surface(
                shape = shape,
                shadowElevation = shadowElevation,
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.padding(
                    start = shadowPadding,
                    end = shadowPadding,
                    top = if (index == 0) shadowPadding else 0.dp,
                    bottom = if (index == items.lastIndex) shadowPadding else 0.dp
                )
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = item.text,
                            color = item.contentColor
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = when (item.contentColor) {
                                MaterialTheme.colorScheme.error -> item.contentColor
                                else -> LocalContentColor.current
                            }
                        )
                    },
                    onClick = {
                        onDismissRequest()
                        item.onClick()
                    }
                )
            }

            if (index < items.lastIndex) {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

data class SlicedMenuItem(
    val text: String,
    val icon: ImageVector,
    val contentColor: Color = Color.Unspecified, // Useful for the red "Delete" item,
    val onClick: () -> Unit
)