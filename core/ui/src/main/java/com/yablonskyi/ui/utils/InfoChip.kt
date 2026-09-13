package com.yablonskyi.ui.utils

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.yablonskyi.ui.theme.Dimens

@Composable
fun InfoChip(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.labelLarge
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = modifier
    ) {
        Text(
            text = text,
            style = style,
            modifier = Modifier.padding(
                horizontal = Dimens.Spacing.Medium,
                vertical = Dimens.Spacing.Small
            )
        )
    }
}

@Composable
fun OutlinedInfoChip(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.labelLarge
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = Color.Transparent,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
    ) {
        Text(
            text = text,
            style = style,
            modifier = Modifier.padding(
                horizontal = Dimens.Spacing.Medium,
                vertical = Dimens.Spacing.Small
            )
        )
    }
}