package com.yablonskyi.dndsheet.ui.dice

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import com.yablonskyi.dndsheet.R
import com.yablonskyi.dndsheet.ui.theme.DnDSheetTheme
import com.yablonskyi.dndsheet.ui.utils.diceOptions

@Composable
fun DiceRollFloatingActionButton(
    onClick: (Map<Int, Int>) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val clickCounts = remember { mutableStateMapOf<Int, Int>() }

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val hasEnoughWidth =
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
    val hasEnoughHeight =
        windowSizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND)

    val isWideScreen = hasEnoughWidth && !hasEnoughHeight

    val isTabletScreen = hasEnoughWidth && hasEnoughHeight

    val diceItem = @Composable { flatIndex: Int ->
        val dice = diceOptions[flatIndex]
        val distanceMultiplier = diceOptions.size - flatIndex
        val count = clickCounts.getOrDefault(dice.sides, 0)

        val enterAnimation = if (isWideScreen || isTabletScreen) {
            slideInHorizontally(
                initialOffsetX = { it * distanceMultiplier },
                animationSpec = tween(150)
            ) + scaleIn(
                initialScale = 0.8f,
                animationSpec = tween(200)
            )
        } else {
            slideInVertically(
                initialOffsetY = { it * distanceMultiplier },
                animationSpec = tween(150)
            ) + scaleIn(
                initialScale = 0.8f,
                animationSpec = tween(200)
            )
        }

        val exitAnimation = if (isWideScreen || isTabletScreen) {
            slideOutHorizontally(
                targetOffsetX = { it * distanceMultiplier },
                animationSpec = tween(150)
            ) + fadeOut(
                animationSpec = tween(150)
            ) + scaleOut(
                targetScale = 0.8f,
                animationSpec = tween(150)
            )
        } else {
            slideOutVertically(
                targetOffsetY = { it * distanceMultiplier },
                animationSpec = tween(150)
            ) + fadeOut(
                animationSpec = tween(150)
            ) + scaleOut(
                targetScale = 0.8f,
                animationSpec = tween(150)
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = enterAnimation,
            exit = exitAnimation
        ) {
            Box {
                Button(
                    shape = FloatingActionButtonDefaults.shape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    onClick = {
                        clickCounts[dice.sides] = count + 1
                    },
                    contentPadding = PaddingValues(0.dp),
                    elevation = ButtonDefaults.buttonElevation(6.dp),
                    modifier = Modifier
                        .size(64.dp)
                        .align(Alignment.Center)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            painter = painterResource(id = dice.iconRes),
                            contentDescription = "Roll d${dice.sides}",
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "${stringResource(R.string.dice_first_letter)}${dice.sides}"
                        )
                    }
                }

                if (count > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = count.toString(),
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
    ) {
        if (isWideScreen || isTabletScreen) {
            val itemsPerRow = (diceOptions.size + 1) / 2
            val diceRows = diceOptions.chunked(itemsPerRow)

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                diceRows.forEachIndexed { rowIndex, rowItems ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        rowItems.forEachIndexed { colIndex, _ ->
                            val flatIndex = rowIndex * itemsPerRow + colIndex
                            diceItem(flatIndex)
                        }
                    }
                }
            }
        } else {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                diceOptions.indices.forEach { index ->
                    diceItem(index)
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        ExtendedFloatingActionButton(
            onClick = {
                if (expanded) {
                    onClick(clickCounts.toMap())
                }
                expanded = !expanded
                if (!expanded) {
                    clickCounts.clear()
                }
            },
            expanded = expanded,
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_dice_d20),
                    contentDescription = "Roll dice",
                    modifier = Modifier.size(32.dp)
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.roll),
                )
            },
            modifier = Modifier.defaultMinSize(80.dp, 80.dp)
        )
    }
}

@Preview
@Composable
private fun DiceRollFabPreview() {
    DnDSheetTheme {
        Scaffold(
            floatingActionButton = {
                DiceRollFloatingActionButton({})
            }
        ) {
            Box(
                Modifier.padding(it)
            ) {

            }
        }
    }
}