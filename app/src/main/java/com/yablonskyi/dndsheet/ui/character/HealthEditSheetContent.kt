package com.yablonskyi.dndsheet.ui.character

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yablonskyi.dndsheet.R

@Composable
fun HealthEditSheetContent(
    currentHp: Int,
    maxHp: Int,
    tempHp: Int,
    onDismiss: () -> Unit,
    onApply: (current: Int, max: Int, temp: Int) -> Unit
) {
    var localCurrent by remember { mutableIntStateOf(currentHp) }
    var localMax by remember { mutableIntStateOf(maxHp) }
    var localTemp by remember { mutableIntStateOf(tempHp) }

    var adjustmentValue by remember { mutableIntStateOf(0) }

    fun pushUpdate() {
        onApply(localCurrent, localMax, localTemp)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 48.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.health),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            IconButton(
                onClick = {
                    onDismiss()
                    pushUpdate()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IntTextField(
                value = localCurrent,
                label = stringResource(R.string.hp_current),
                onValueChange = {
                    localCurrent = it
                    pushUpdate()
                },
                modifier = Modifier.weight(1f)
            )

            IntTextField(
                value = localMax,
                label = stringResource(R.string.hp_max),
                onValueChange = {
                    localMax = it
                    pushUpdate()
                },
                modifier = Modifier.weight(1f)
            )

            // Temp HP
            IntTextField(
                value = localTemp,
                label = stringResource(R.string.hp_temp),
                onValueChange = {
                    localTemp = it
                    pushUpdate()
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Amount Input
            IntTextField(
                value = adjustmentValue,
                label = stringResource(R.string.amount),
                onValueChange = { adjustmentValue = it },
                modifier = Modifier.weight(1f).padding(bottom = 8.dp)
            )

            // Damage Button
            TextButton(
                onClick = {
                    if (adjustmentValue > 0) {
                        localCurrent = (localCurrent - adjustmentValue).coerceAtLeast(0)
                        adjustmentValue = 0
                        pushUpdate()
                    }
                },
                border = BorderStroke(2.dp, Color(0xffe34c1e)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color(0xffe34c1e)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(stringResource(R.string.damage))
            }

            // Heal Button
            TextButton(
                onClick = {
                    if (adjustmentValue > 0) {
                        localCurrent = (localCurrent + adjustmentValue).coerceAtMost(localMax)
                        adjustmentValue = 0
                        pushUpdate()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color(0xff529c64)
                ),
                border = BorderStroke(2.dp, Color(0xff529c64)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(stringResource(R.string.heal))
            }
        }
    }
}