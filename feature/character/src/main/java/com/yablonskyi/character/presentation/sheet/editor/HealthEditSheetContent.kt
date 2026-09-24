package com.yablonskyi.character.presentation.sheet.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yablonskyi.ui.R
import com.yablonskyi.ui.utils.DnDSheetOutlinedTextField

val damageColor = Color(0xffe34c1e)
val healingColor = Color(0xff529c64)

@Composable
fun HealthEditSheetContent(state: HealthFormUiState, onIntent: (HealthFormIntent) -> Unit) {
    val focus = LocalFocusManager.current
    fun action(intent: HealthFormIntent) {
        focus.clearFocus(); onIntent(intent)
    }
    LazyColumn(
        Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    stringResource(R.string.health),
                    style = MaterialTheme.typography.headlineSmall
                )
                IconButton(
                    onClick = { onIntent(HealthFormIntent.Dismiss) }
                ) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close))
                }
            }
        }
        item {
            listOf(
                HealthField.CURRENT to R.string.hp_current,
                HealthField.MAXIMUM to R.string.hp_max,
                HealthField.TEMPORARY to R.string.hp_temp
            ).forEach { (field, label) ->
                HealthInput(state, field, label, onIntent)
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = { action(HealthFormIntent.FullHeal) },
                    enabled = state.maximum.isValid,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = healingColor,
                    ),
                    modifier = Modifier.weight(1f)
                ) { Text(stringResource(R.string.btn_full_heal)) }
                OutlinedButton(
                    onClick = { action(HealthFormIntent.MarkDead) },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = damageColor,
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        stringResource(R.string.btn_you_are_dead)
                    )
                }
            }
        }
        item {
            HorizontalDivider()
        }
        item {
            HealthInput(state, HealthField.AMOUNT, R.string.amount, onIntent)

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = { action(HealthFormIntent.Heal) },
                    enabled = state.isValid && state.amount.isValid,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = healingColor,
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_vital_signs),
                        contentDescription = stringResource(R.string.heal),
                        tint = healingColor
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.heal))
                }
                OutlinedButton(
                    onClick = { action(HealthFormIntent.Damage) },
                    enabled = state.isValid && state.amount.isValid,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = damageColor,
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_skull),
                        contentDescription = stringResource(R.string.damage),
                        tint = damageColor
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.damage))
                }
            }
        }
    }
}

@Composable
private fun HealthInput(
    state: HealthFormUiState,
    field: HealthField,
    label: Int,
    onIntent: (HealthFormIntent) -> Unit
) {
    DnDSheetOutlinedTextField(
        state.field(field),
        { onIntent(HealthFormIntent.Changed(field, it)) },
        stringResource(label),
        { onIntent(HealthFormIntent.FocusChanged(field, it)) },
        Modifier.fillMaxWidth(),
        showMaximum = field == HealthField.CURRENT
    )
}