package com.yablonskyi.dndsheet.ui.attack

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yablonskyi.dndsheet.R
import com.yablonskyi.dndsheet.data.model.character.Ability
import com.yablonskyi.dndsheet.data.model.character.Attack
import com.yablonskyi.dndsheet.data.model.character.AttackType
import com.yablonskyi.dndsheet.data.model.character.DamageType
import com.yablonskyi.dndsheet.ui.character.IntTextField
import com.yablonskyi.dndsheet.ui.spell.EnumDropdown

@Composable
fun UpdateAttackSheet(
    attack: Attack,
    onDismiss: () -> Unit,
    onSave: (Attack) -> Unit,
    onDelete: (Attack) -> Unit,
) {
    val isCreateMode = attack.attackId == 0L

    var name by remember(attack) { mutableStateOf(attack.name) }
    var attackType by remember(attack) { mutableStateOf(attack.attackType) }
    var ability by remember(attack) { mutableStateOf(attack.ability) }
    var isProficient by remember(attack) { mutableStateOf(attack.isProficient) }

    var bonusToHit by remember(attack) { mutableIntStateOf(attack.bonusToHit) }
    var bonusToDamage by remember(attack) { mutableIntStateOf(attack.bonusToDamage) }

    var damageDice by remember(attack) { mutableStateOf(attack.damageDice) }
    var damageType by remember(attack) { mutableStateOf(attack.damageType) }
    var range by remember(attack) { mutableStateOf(attack.range) }
    var notes by remember(attack) { mutableStateOf(attack.notes) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 48.dp)
    ) {
        // Header
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
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = {
                    val finalAttack = attack.copy(
                        name = name,
                        attackType = attackType,
                        ability = ability,
                        isProficient = isProficient,
                        bonusToHit = bonusToHit,
                        bonusToDamage = bonusToDamage,
                        damageDice = damageDice,
                        damageType = damageType,
                        range = range,
                        notes = notes
                    )
                    onSave(finalAttack)
                    onDismiss()
                },
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
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text(stringResource(R.string.char_name)) },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = range,
                onValueChange = onRangeChange,
                label = { Text(stringResource(R.string.msg_distance)) },
                modifier = Modifier.weight(0.6f),
                singleLine = true
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
                modifier = Modifier.weight(1f)
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
            OutlinedTextField(
                value = damageDice,
                onValueChange = onDamageDiceChange,
                label = { Text(stringResource(R.string.damage_dice)) },
                supportingText = { Text(stringResource(R.string.supporting_text_dice)) },
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
            IntTextField(
                value = bonusToHit,
                label = stringResource(R.string.bonus_hit),
                onValueChange = onBonusHitChange,
                modifier = Modifier.weight(1f)
            )
            IntTextField(
                value = bonusToDamage,
                label = stringResource(R.string.bonus_damage),
                onValueChange = onBonusDamageChange,
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = { Text(stringResource(R.string.notes)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )
    }
}