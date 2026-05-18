package com.yablonskyi.dndsheet.ui.dice

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yablonskyi.dndsheet.R
import com.yablonskyi.dndsheet.ui.theme.DnDSheetTheme
import kotlin.math.abs

@Composable
fun DiceRollResultBox(
    numbers: List<Int>,
    strings: List<String>,
    diceMod: Int?,
    result: Int,
    modifier: Modifier = Modifier,
    hasRegularDice: Boolean = false,
    isPinned: Boolean,
    onPinClick: () -> Unit,
    diceChar: String = stringResource(R.string.dice_first_letter),
) {
    val color = if (numbers.any { it == 20 } && hasRegularDice) Color.Green
    else if (numbers.any { it == 1 } && hasRegularDice) Color.Red
    else null

    val fallbackSurface = MaterialTheme.colorScheme.surfaceContainer

    val solidDimColor = color?.let {
        lerp(start = it, stop = fallbackSurface, fraction = 0.7f)
    } ?: fallbackSurface

    Card(
        onClick = onPinClick,
        colors = CardDefaults.cardColors().copy(
            containerColor = solidDimColor,
        ),
        border = color?.let { BorderStroke(1.dp, it) },
        elevation = CardDefaults.elevatedCardElevation(6.dp),
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 16.dp)
            .width(250.dp)
            .height(120.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                ) {
                    Text(
                        text = numbers.run {
                            if (diceMod == null)
                                this.joinToString(" + ")
                            else {
                                val sign = if (diceMod >= 0) "+" else "-"

                                "${this.joinToString(" + ")} $sign ${abs(diceMod)}"
                            }
                        },
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.MiddleEllipsis,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                    Text(
                        text = strings.joinToString(", ") { it.replace("d", diceChar) },
                        maxLines = 2,
                        style = MaterialTheme.typography.labelMedium,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.align(Alignment.BottomStart)
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    VerticalDivider(
                        thickness = 2.dp,
                        color = Color.Gray,
                        modifier = Modifier.weight(0.3f)
                    )
                    Text("=", fontSize = 24.sp)
                    VerticalDivider(
                        thickness = 2.dp,
                        color = Color.Gray,
                        modifier = Modifier.weight(0.3f)
                    )
                }
                Text(
                    text = result.toString(),
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(0.5f)
                )
            }
            if (isPinned) {
                Icon(
                    imageVector = Icons.Default.PushPin,
                    contentDescription = "Result pinned",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(16.dp)
                        .graphicsLayer { rotationZ = 45f }
                )
            }
        }
    }
}

@Preview
@Composable
private fun ResultBoxPreview() {
    DnDSheetTheme {
        DiceRollResultBox(
            numbers = listOf(
                17, 20, 5
            ),
            diceMod = 2,
            hasRegularDice = true,
            result = 1000,
            strings = listOf(
                "3d20"
            ),
            isPinned = true,
            onPinClick = {}
        )
    }
}