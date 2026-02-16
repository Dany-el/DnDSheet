package com.yablonskyi.dndsheet.ui.dice

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import com.yablonskyi.dndsheet.R
import com.yablonskyi.dndsheet.ui.utils.diceOptions

@Composable
fun DiceRollFloatingActionButton(
    onClick: (Map<Int, Int>) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val clickCounts = remember { mutableStateMapOf<Int, Int>() }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            diceOptions.forEachIndexed { index, dice ->

                val distanceMultiplier = diceOptions.size - index

                AnimatedVisibility(
                    visible = expanded,
                    enter = slideInVertically(
                        initialOffsetY = { it * distanceMultiplier },
                        animationSpec = tween(150)
                    ) + scaleIn(
                        initialScale = 0.8f,
                        animationSpec = tween(200)
                    ),
                    exit = slideOutVertically(
                        targetOffsetY = { it * distanceMultiplier },
                        animationSpec = tween(150)
                    )
                ) {
                    val count = clickCounts.getOrDefault(dice.sides, 0)

                    Box {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            ),
//                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
                            modifier = Modifier
                                .size(64.dp)
                                .align(Alignment.Center)
                                .clickable(
                                    onClick = {
                                        clickCounts[dice.sides] = count + 1
                                    }
                                ),
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
        }

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
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            modifier = Modifier.defaultMinSize(80.dp, 80.dp)
        )
    }
}