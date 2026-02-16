package com.yablonskyi.dndsheet.ui.dice

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yablonskyi.dndsheet.R
import com.yablonskyi.dndsheet.ui.utils.diceOptions

@Composable
fun MultiFloatingActionButton(
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
        AnimatedVisibility(
            visible = expanded,
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                diceOptions.forEach { dice ->
                    val count = clickCounts.getOrDefault(dice.sides, 0)
                    Box {
                        FloatingActionButton(
                            onClick = {
                                clickCounts[dice.sides] = count + 1
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .align(Alignment.Center),
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = dice.iconRes),
                                    contentDescription = "Roll d${dice.sides}",
                                    modifier = Modifier.size(24.dp)
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

        Spacer(Modifier.height(4.dp))

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