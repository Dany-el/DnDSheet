package com.yablonskyi.dndsheet.ui.spell

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.yablonskyi.dndsheet.R
import com.yablonskyi.dndsheet.data.model.character.Ability
import com.yablonskyi.dndsheet.data.model.character.AttackType
import com.yablonskyi.dndsheet.data.model.character.Component
import com.yablonskyi.dndsheet.data.model.character.DamageType
import com.yablonskyi.dndsheet.data.model.character.MagicSchool
import com.yablonskyi.dndsheet.data.model.character.Spell
import com.yablonskyi.dndsheet.data.model.character.SpellCastTime
import com.yablonskyi.dndsheet.data.model.character.SpellDuration
import com.yablonskyi.dndsheet.data.model.character.SpellLevel
import com.yablonskyi.dndsheet.ui.theme.DnDSheetTheme
import com.yablonskyi.dndsheet.ui.utils.UiUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpellEditScreen(
    spell: Spell,
    onUpdate: (Spell) -> Unit,
    onNavigateBack: () -> Unit
) {
    // --- Local State Holders ---
    var name by remember(spell.name) { mutableStateOf(spell.name) }
    var level by remember(spell.level) { mutableStateOf(spell.level) }
    var school by remember(spell.school) { mutableStateOf(spell.school) }

    var castTime by remember(spell.castTime) { mutableStateOf(spell.castTime) }
    var range by remember(spell.range) { mutableStateOf(spell.range) }

    var components by remember(spell.components) { mutableStateOf(spell.components) }
    var material by remember(spell.material) { mutableStateOf(spell.material ?: "") }

    var duration by remember(spell.duration) { mutableStateOf(spell.duration) }
    var isRitual by remember(spell.isRitual) { mutableStateOf(spell.isRitual) }
    var isConcentration by remember(spell.isConcentration) { mutableStateOf(spell.isConcentration) }

    var attackType by remember(spell.attackType) { mutableStateOf(spell.attackType) }
    var saveStat by remember(spell.saveStat) { mutableStateOf(spell.saveStat) }

    var damageType by remember(spell.damageType) { mutableStateOf(spell.damageType) }
    var damageDice by remember(spell.damageDice) { mutableStateOf(spell.damageDice ?: "") }

    var description by remember(spell.description) { mutableStateOf(spell.description) }
    var higherLevels by remember(spell.higherLevels) { mutableStateOf(spell.higherLevels ?: "") }

    val onSaveClick = {
        val updatedSpell = spell.copy(
            name = name,
            level = level,
            school = school,
            castTime = castTime,
            range = range,
            components = components,
            material = if (components.contains(Component.MATERIAL)) material else null,
            duration = duration,
            isRitual = isRitual,
            isConcentration = isConcentration,
            attackType = attackType,
            saveStat = saveStat,
            damageType = damageType,
            damageDice = damageDice,
            description = description,
            higherLevels = higherLevels
        )
        onUpdate(updatedSpell)
        onNavigateBack()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                title = {
                    Text(
                        text = stringResource(if (spell.spellId == 0L) R.string.add_spell else R.string.edit_spell),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onSaveClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = "Save Spell")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- BASIC INFO ---
            OutlinedTextField(
                value = name,
                onValueChange = { name = it }, // Logic removed, just state update
                label = { Text(stringResource(R.string.spell_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EnumDropdown(
                    value = level,
                    labelRes = R.string.spell_level,
                    options = SpellLevel.entries,
                    nameMapper = { stringResource(it.resId) },
                    onSelected = { level = it },
                    modifier = Modifier.weight(1f)
                )

                EnumDropdown(
                    value = school,
                    labelRes = R.string.msg_school,
                    options = MagicSchool.entries,
                    nameMapper = { stringResource(it.resId) },
                    onSelected = { school = it },
                    modifier = Modifier.weight(1f)
                )
            }

            // --- CASTING ---
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EnumDropdown(
                    value = castTime,
                    labelRes = R.string.msg_casting_time,
                    options = SpellCastTime.entries,
                    nameMapper = { stringResource(it.resId) },
                    onSelected = { castTime = it },
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = range,
                    onValueChange = { range = it },
                    label = { Text(stringResource(R.string.msg_distance)) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            // --- COMPONENTS (V, S, M) ---
            Text(
                stringResource(R.string.msg_components),
                style = MaterialTheme.typography.labelLarge
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Component.entries.forEach { component ->
                    FilterChip(
                        selected = components.contains(component),
                        onClick = {
                            val newComponents = if (components.contains(component)) {
                                components - component
                            } else {
                                components + component
                            }.sorted()

                            components = newComponents
                        },
                        label = { Text(stringResource(component.resId), maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    )
                }
            }

            if (components.contains(Component.MATERIAL)) {
                OutlinedTextField(
                    value = material,
                    onValueChange = { material = it },
                    label = { Text(stringResource(R.string.spell_material_component)) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }

            // --- DURATION & FLAGS ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                EnumDropdown(
                    value = duration,
                    labelRes = R.string.msg_duration,
                    options = SpellDuration.entries,
                    nameMapper = { stringResource(it.resId) },
                    onSelected = { duration = it },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isRitual,
                        onCheckedChange = { isRitual = it }
                    )
                    Text(stringResource(R.string.ritual))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isConcentration,
                        onCheckedChange = { isConcentration = it }
                    )
                    Text(stringResource(R.string.concentration))
                }
            }

            HorizontalDivider()

            // --- COMBAT STATS ---
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EnumDropdown(
                    value = attackType,
                    labelRes = R.string.spell_attack_type,
                    options = AttackType.entries,
                    nameMapper = { stringResource(it.resId) },
                    onSelected = { attackType = it },
                    modifier = Modifier.weight(1f)
                )

                EnumDropdown(
                    value = saveStat,
                    labelRes = R.string.spell_save_stat,
                    options = Ability.entries,
                    nameMapper = {
                        it?.let { stringResource(it.nameRes) } ?: ""
                    },
                    onSelected = { saveStat = it },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EnumDropdown(
                    value = damageType,
                    labelRes = R.string.spell_damage_type,
                    options = listOf(null) + DamageType.entries,
                    nameMapper = {
                        it?.let { stringResource(it.resId) } ?: stringResource(R.string.none)
                    },
                    onSelected = { damageType = it },
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = damageDice,
                    onValueChange = { damageDice = it },
                    label = { Text(stringResource(R.string.spell_damage_dice)) },
                    placeholder = { Text(stringResource(R.string.supporting_text_dice)) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            // --- DESCRIPTION ---
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.spell_description)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                minLines = 3
            )

            OutlinedTextField(
                value = higherLevels,
                onValueChange = { higherLevels = it },
                label = { Text(stringResource(R.string.spell_higher_levels)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

// --- REUSABLE DROPDOWN COMPONENT ---
@Composable
fun <T> EnumDropdown(
    value: T,
    labelRes: Int,
    options: List<T>,
    nameMapper: @Composable (T) -> String,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var textFieldSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
    val density = LocalDensity.current

    Box(modifier = modifier) {
        OutlinedTextField(
            value = nameMapper(value),
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(stringResource(labelRes), maxLines = 1, overflow = TextOverflow.Ellipsis) },
            trailingIcon = {
                Icon(
                    imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = null
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    textFieldSize = coordinates.size.toSize()
                }
                .clickable { expanded = true },
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(with(density) { textFieldSize.width.toDp() })
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(nameMapper(option), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    onClick = {
                        expanded = false
                        onSelected(option)
                    }
                )
            }
        }
    }
}

@Preview
@Composable
private fun SpellEditScreenPreview() {
    DnDSheetTheme {
        SpellEditScreen(
            spell = UiUtils.sampleSpells[3],
            onUpdate = {},
            onNavigateBack = {}
        )
    }
}