package com.yablonskyi.dndsheet.ui.utils

import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun SlicedDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    items: List<SlicedMenuItem>,
    modifier: Modifier = Modifier,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier.widthIn(min = 180.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        items.forEach { item ->
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
    }
}

data class SlicedMenuItem(
    val text: String,
    val icon: ImageVector,
    val contentColor: Color = Color.Unspecified,
    val onClick: () -> Unit
)