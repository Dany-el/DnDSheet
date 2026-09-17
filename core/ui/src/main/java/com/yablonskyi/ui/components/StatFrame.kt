package com.yablonskyi.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntSize
import com.yablonskyi.ui.utils.PreviewThemeWrapper

/**
 * A character-sheet frame with a title interrupting its top edge and an overlapping score badge.
 * Values are supplied by the caller; this component does not calculate ability modifiers.
 */
@Composable
fun StatFrame(
    title: String,
    value: String,
    score: String,
    onValueClick: () -> Unit,
    onScoreClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Transparent,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    val titleStyle = MaterialTheme.typography.titleMedium
    val scoreStyle = MaterialTheme.typography.titleLarge
    val density = LocalDensity.current
    var titleSize by remember { mutableStateOf(IntSize.Zero) }
    var scoreSize by remember { mutableStateOf(IntSize.Zero) }
    val titleHalfHeight = if (titleSize.height > 0) titleSize.height / 2f
    else with(density) { titleStyle.lineHeight.toPx() / 2 }
    val scoreHalfHeight = if (scoreSize.height > 0) scoreSize.height / 2f
    else with(density) { scoreStyle.lineHeight.toPx() / 2 + 4.dp.toPx() }
    val clickableAreaColor = MaterialTheme.colorScheme.tertiary

    Column(
        modifier = modifier
            .width(160.dp)
            .background(containerColor)
            .drawBehind {
                val strokeWidth = 1.5.dp.toPx()
                val inset = strokeWidth / 2
                // Leave actual gaps in the frame instead of hiding it with opaque backgrounds.
                clipRect(
                    left = (size.width - titleSize.width) / 2f,
                    top = 0f,
                    right = (size.width + titleSize.width) / 2f,
                    bottom = titleSize.height.toFloat(),
                    clipOp = ClipOp.Difference,
                ) {
                    clipRect(
                        left = (size.width - scoreSize.width) / 2f,
                        top = size.height - scoreSize.height,
                        right = (size.width + scoreSize.width) / 2f,
                        bottom = size.height,
                        clipOp = ClipOp.Difference,
                    ) {
                        drawRoundRect(
                            color = contentColor,
                            topLeft = Offset(inset, titleHalfHeight),
                            size = Size(
                                width = (size.width - strokeWidth).coerceAtLeast(0f),
                                height = (size.height - titleHalfHeight - scoreHalfHeight)
                                    .coerceAtLeast(0f),
                            ),
                            cornerRadius = CornerRadius(16.dp.toPx()),
                            style = Stroke(strokeWidth),
                        )
                    }
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .onSizeChanged { titleSize = it }
                .padding(horizontal = 8.dp),
            color = contentColor,
            style = titleStyle,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Box(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .fillMaxWidth()
                .border(
                    width = 2.dp,
                    color = clickableAreaColor,
                    shape = RoundedCornerShape(16.dp),
                )
                .clip(RoundedCornerShape(16.dp))
                .clickable(role = Role.Button, onClick = onValueClick)
                .padding(horizontal = 8.dp, vertical = 18.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = value,
                color = clickableAreaColor,
                style = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold
            )
        }
        Text(
            text = score,
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .onSizeChanged { scoreSize = it }
                .border(2.dp, clickableAreaColor, RoundedCornerShape(50))
                .clip(RoundedCornerShape(50))
                .clickable(role = Role.Button, onClick = onScoreClick)
                .padding(horizontal = 20.dp, vertical = 4.dp),
            color = clickableAreaColor,
            fontWeight = FontWeight.SemiBold,
            style = scoreStyle,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Large text", showBackground = true, fontScale = 1.5f)
@Composable
private fun StatFramePreview() {
    PreviewThemeWrapper.Preview {
        StatFrame(
            title = "TEXT",
            value = "+2",
            score = "16",
            onValueClick = {},
            onScoreClick = {},
            modifier = Modifier.padding(24.dp),
        )
    }
}