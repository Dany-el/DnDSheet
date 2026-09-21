package com.yablonskyi.character.presentation.sheet.editor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yablonskyi.model.character.Ability
import com.yablonskyi.model.character.Attack
import com.yablonskyi.model.character.AttackType
import com.yablonskyi.model.character.DamageType
import com.yablonskyi.ui.R
import com.yablonskyi.ui.utils.EnumDropdown
import com.yablonskyi.ui.utils.IntTextField
import com.yablonskyi.ui.utils.NumbersTextField

@Composable
fun UpdateAttackSheet(
    attack: Attack,
    onDismiss: () -> Unit,
    onSave: (Attack) -> Unit,
    onDelete: (Attack) -> Unit,
) {
    val isCreateMode = attack.attackId == 0L

    var name by rememberSaveable(attack.attackId) { mutableStateOf(attack.name) }
    var attackType by rememberSaveable(attack.attackId) { mutableStateOf(attack.attackType) }
    var ability by rememberSaveable(attack.attackId) { mutableStateOf(attack.ability) }
    var isProficient by rememberSaveable(attack.attackId) { mutableStateOf(attack.isProficient) }

    var bonusToHit by rememberSaveable(attack.attackId) { mutableIntStateOf(attack.bonusToHit) }
    var bonusToDamage by rememberSaveable(attack.attackId) { mutableIntStateOf(attack.bonusToDamage) }

    var damageDice by rememberSaveable(attack.attackId) { mutableStateOf(attack.damageDice) }
    var damageType by rememberSaveable(attack.attackId) { mutableStateOf(attack.damageType) }
    var range by rememberSaveable(attack.attackId) { mutableStateOf(attack.range) }
    var notes by rememberSaveable(attack.attackId) { mutableStateOf(attack.notes) }

    val isNameValid = name.isNotBlank()
    val isRangeValid = range.isNotBlank()
    val isDamageDiceValid =
        damageDice.isNotBlank() && Regex("""^([1-9]\d*)?[dDкК](4|6|8|10|12|20|100)$""").matches(
            damageDice
        )

    val isFormValid = isNameValid && isRangeValid && isDamageDiceValid

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
                stringResource(R.string.msg_attack),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn{
            item {
                AttackFormContent(
                    name, { name = it },
                    attackType, { attackType = it },
                    ability, { ability = it },
                    isProficient, { isProficient = it },
                    bonusToHit, { bonusToHit = it },
                    bonusToDamage, { bonusToDamage = it },
                    damageDice, { damageDice = it },
                    damageType, { damageType = it },
                    range, { range = it },
                    notes, { notes = it }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = {
                            val finalAttack = attack.copy(
                                name = name.trim(),
                                attackType = attackType,
                                ability = ability,
                                isProficient = isProficient,
                                bonusToHit = bonusToHit,
                                bonusToDamage = bonusToDamage,
                                damageDice = damageDice,
                                damageType = damageType,
                                range = range,
                                notes = notes.trim()
                            )
                            onSave(finalAttack)
                            onDismiss()
                        },
                        enabled = isFormValid,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = (
                                    if (isCreateMode)
                                        stringResource(R.string.add)
                                    else
                                        stringResource(R.string.save)
                                    ).uppercase()
                        )
                    }
                    if (!isCreateMode) {
                        OutlinedButton(
                            onClick = {
                                onDelete(attack)
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error,
                                containerColor = Color.Transparent
                            )
                        ) {
                            Text(stringResource(R.string.delete).uppercase())
                        }
                    }
                }
            }
        }
    }

}

@Composable
fun AttackFormContent(
    name: String, onNameChange: (String) -> Unit,
    attackType: AttackType, onTypeChange: (AttackType) -> Unit,
    ability: Ability, onAbilityChange: (Ability) -> Unit,
    isProficient: Boolean, onProfChange: (Boolean) -> Unit,
    bonusToHit: Int, onBonusHitChange: (Int) -> Unit,
    bonusToDamage: Int, onBonusDamageChange: (Int) -> Unit,
    damageDice: String, onDamageDiceChange: (String) -> Unit,
    damageType: DamageType, onDamageTypeChange: (DamageType) -> Unit,
    range: String, onRangeChange: (String) -> Unit,
    notes: String, onNotesChange: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .padding(bottom = 16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = name,
                onValueChange = {
                    if (it.length <= 50) onNameChange(it)
                },
                label = { Text(stringResource(R.string.spell_name) + "*") },
                isError = name.isBlank(),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            IntTextField(
                value = range.run {
                    Regex("\\d+").find(this)?.value?.toInt() ?: 0
                },
                validate = { input ->
                    (input.toIntOrNull() ?: 0) <= 1000
                },
                onValueChange = {
                    onRangeChange(it.toString())
                },
                isError = range.isBlank() || range.run {
                    Regex("\\d+").find(this)?.value?.toInt() ?: 0
                } == 0,
                label = stringResource(R.string.msg_distance) + "*",
                modifier = Modifier.weight(0.6f),
            )
        }

        EnumDropdown(
            value = attackType,
            labelRes = R.string.attack_type,
            options = AttackType.entries,
            nameMapper = { stringResource(it.resId) },
            onSelected = onTypeChange,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = { onProfChange(!isProficient) })
            ) {
                Checkbox(checked = isProficient, onCheckedChange = onProfChange)
                Text(stringResource(R.string.is_proficient))
            }
            EnumDropdown(
                value = ability,
                labelRes = R.string.ability,
                options = Ability.entries,
                nameMapper = { stringResource(it.nameRes) },
                onSelected = onAbilityChange,
                modifier = Modifier
                    .padding(bottom = 4.dp)
                    .weight(1f)
            )
        }

        HorizontalDivider()

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {

            val damageDiceTypingRegex = Regex("""^([1-9]\d*)?([dDкК](1(00?|2)?|20?|4|6|8)?)?$""")

            val damageDiceValidRegex = Regex("""^([1-9]\d*)?[dDкК](4|6|8|10|12|20|100)$""")

            val isDamageDiceError =
                damageDice.isBlank() || !damageDiceValidRegex.matches(damageDice)

            OutlinedTextField(
                value = damageDice,
                onValueChange = {
                    if (it.isEmpty() || damageDiceTypingRegex.matches(it)) {
                        onDamageDiceChange(it)
                    }
                },
                label = { Text(stringResource(R.string.damage_dice) + "*") },
                supportingText = {
                    if (isDamageDiceError)
                        Text(
                            stringResource(
                                R.string.supporting_text_dice
                            )
                        )
                },
                isError = isDamageDiceError,
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            EnumDropdown(
                value = damageType,
                labelRes = R.string.damage_type,
                options = DamageType.entries,
                nameMapper = { stringResource(it.resId) },
                onSelected = onDamageTypeChange,
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            NumbersTextField(
                value = bonusToHit,
                label = stringResource(R.string.bonus_hit),
                validate = { input -> (input.toIntOrNull() ?: 0) in (-100..100) },
                onValueChange = {
                    onBonusHitChange(it)
                },
                modifier = Modifier.weight(1f)
            )
            NumbersTextField(
                value = bonusToDamage,
                label = stringResource(R.string.bonus_damage),
                validate = { input -> (input.toIntOrNull() ?: 0) in (-100..100) },
                onValueChange = {
                    onBonusDamageChange(it)
                },
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = { Text(stringResource(R.string.notes)) },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1
        )
    }
}
