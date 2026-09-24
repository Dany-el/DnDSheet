package com.yablonskyi.character.presentation.sheet.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yablonskyi.model.character.Ability
import com.yablonskyi.model.character.AttackType
import com.yablonskyi.model.character.AttackUsage
import com.yablonskyi.model.character.DamageAbilityModifier
import com.yablonskyi.model.character.DamageMode
import com.yablonskyi.model.character.DamageType
import com.yablonskyi.model.dice.DiceRoles
import com.yablonskyi.ui.R
import com.yablonskyi.ui.utils.DnDSheetOutlinedTextField
import com.yablonskyi.ui.utils.EnumDropdown

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UpdateAttackSheet(state: AttackFormUiState, onIntent: (AttackFormIntent) -> Unit) {
    val focus = LocalFocusManager.current
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    stringResource(R.string.msg_attack),
                    style = MaterialTheme.typography.headlineSmall
                )
                IconButton(
                    onClick = { onIntent(AttackFormIntent.Dismiss) }
                ) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close))
                }
            }
        }
        item { AttackFormContent(state, onIntent) }
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { focus.clearFocus(); onIntent(AttackFormIntent.Submit) },
                    enabled = state.isValid && !state.submitted,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(if (state.original.attackId == 0L) R.string.add else R.string.save))
                }
                if (state.original.attackId != 0L) {
                    OutlinedButton(
                        onClick = { onIntent(AttackFormIntent.Delete) },
                        enabled = !state.submitted,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.delete))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AttackFormContent(state: AttackFormUiState, onIntent: (AttackFormIntent) -> Unit) {
    DnDSheetOutlinedTextField(
        state.name,
        { onIntent(AttackFormIntent.TextChanged(AttackTextField.NAME, it)) },
        stringResource(R.string.spell_name),
        { onIntent(AttackFormIntent.TextFocusChanged(AttackTextField.NAME, it)) },
        isRequired = true
    )
    AttackNumberInput(state, AttackNumberField.RANGE, R.string.msg_distance, onIntent)
    EnumDropdown(
        state.attackType,
        R.string.attack_type,
        AttackType.entries,
        nameMapper = { stringResource(it.resId) },
        onSelected = { onIntent(AttackFormIntent.TypeChanged(it)) })
    EnumDropdown(
        state.ability,
        R.string.ability,
        Ability.entries,
        nameMapper = { stringResource(it.nameRes) },
        onSelected = { onIntent(AttackFormIntent.AbilityChanged(it)) })
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(R.string.is_proficient))
        Switch(state.proficient, { onIntent(AttackFormIntent.ProficiencyChanged(it)) })
    }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AttackUsage.entries.forEach { usage ->
            FilterChip(
                selected = usage in state.usages,
                onClick = { onIntent(AttackFormIntent.UsageChanged(usage)) },
                label = { Text(stringResource(usage.labelRes())) })
        }
    }
    if (state.usages.isEmpty()) Text(
        stringResource(R.string.attack_usage_required),
        color = MaterialTheme.colorScheme.error
    )
    EnumDropdown(
        state.damageMode, R.string.attack_damage_mode, DamageMode.entries,
        nameMapper = { stringResource(if (it == DamageMode.DICE) R.string.attack_dice else R.string.attack_fixed) },
        onSelected = { onIntent(AttackFormIntent.ModeChanged(it)) })
    if (state.damageMode == DamageMode.DICE) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AttackNumberInput(
                state,
                AttackNumberField.DICE_COUNT,
                R.string.attack_dice_count,
                onIntent,
                Modifier.weight(1f)
            )
            EnumDropdown(
                state.die,
                R.string.attack_die,
                DiceRoles.entries,
                Modifier.weight(1f),
                nameMapper = { it.name.lowercase() },
                onSelected = { onIntent(AttackFormIntent.DieChanged(it)) })
        }
        if (state.diceParseError) Text(
            stringResource(R.string.form_invalid_dice),
            color = MaterialTheme.colorScheme.error
        )
    } else AttackNumberInput(
        state,
        AttackNumberField.FIXED_DAMAGE,
        R.string.attack_fixed_damage,
        onIntent
    )
    EnumDropdown(
        state.damageType,
        R.string.damage_type,
        DamageType.entries,
        nameMapper = { stringResource(it.resId) },
        onSelected = { onIntent(AttackFormIntent.DamageTypeChanged(it)) })
    EnumDropdown(
        state.damageAbilityModifier,
        R.string.attack_damage_modifier,
        DamageAbilityModifier.entries,
        nameMapper = {
            stringResource(
                when (it) {
                    DamageAbilityModifier.FULL -> R.string.attack_modifier_full
                    DamageAbilityModifier.NONE -> R.string.attack_modifier_none
                    DamageAbilityModifier.NEGATIVE_ONLY -> R.string.attack_modifier_negative
                }
            )
        },
        onSelected = { onIntent(AttackFormIntent.ModifierChanged(it)) })
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        AttackNumberInput(
            state,
            AttackNumberField.HIT_BONUS,
            R.string.bonus_hit,
            onIntent,
            Modifier.weight(1f)
        )
        AttackNumberInput(
            state,
            AttackNumberField.DAMAGE_BONUS,
            R.string.bonus_damage,
            onIntent,
            Modifier.weight(1f)
        )
    }
    DnDSheetOutlinedTextField(
        state.notes,
        { onIntent(AttackFormIntent.TextChanged(AttackTextField.NOTES, it)) },
        stringResource(R.string.notes),
        { onIntent(AttackFormIntent.TextFocusChanged(AttackTextField.NOTES, it)) },
        maxLines = 8
    )
}

@Composable
private fun AttackNumberInput(
    state: AttackFormUiState, field: AttackNumberField, label: Int,
    onIntent: (AttackFormIntent) -> Unit, modifier: Modifier = Modifier.fillMaxWidth()
) {
    val number = state.number(field)
    DnDSheetOutlinedTextField(
        number, { onIntent(AttackFormIntent.NumberChanged(field, it)) }, stringResource(label),
        { onIntent(AttackFormIntent.NumberFocusChanged(field, it)) }, modifier,
        allowSigned = number.minValue < 0, showMaximum = field == AttackNumberField.DICE_COUNT
    )
}

fun AttackUsage.labelRes(): Int = when (this) {
    AttackUsage.ACTION -> R.string.attack_action
    AttackUsage.BONUS_ACTION -> R.string.attack_bonus_action
    AttackUsage.REACTION -> R.string.attack_reaction
}