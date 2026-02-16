package com.yablonskyi.dndsheet.ui.spell

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import com.yablonskyi.dndsheet.data.model.character.SpellRangeType
import com.yablonskyi.dndsheet.ui.theme.DnDSheetTheme
import com.yablonskyi.dndsheet.ui.utils.EnumDropdown
import com.yablonskyi.dndsheet.ui.utils.IntTextField
import com.yablonskyi.dndsheet.ui.utils.UiUtils
import kotlinx.coroutines.launch

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
    var rangeType by remember(spell.rangeType) { mutableStateOf(spell.rangeType) }
    var rangeValue by remember(spell.rangeValue) { mutableStateOf(spell.rangeValue) }

    var components by remember(spell.components) { mutableStateOf(spell.components) }
    var material by remember(spell.material) { mutableStateOf(spell.material ?: "") }

    var duration by remember(spell.duration) { mutableStateOf(spell.duration) }
    var isRitual by remember(spell.isRitual) { mutableStateOf(spell.isRitual) }
    var isConcentration by remember(spell.isConcentration) { mutableStateOf(spell.isConcentration) }

    var attackType by remember(spell.attackType) { mutableStateOf(spell.attackType) }
    var saveStat by remember(spell.saveStat) { mutableStateOf(spell.saveStat) }

    var damageType by remember(spell.damageType) { mutableStateOf(spell.damageType) }
    var damageDice by remember(spell.damageDice) { mutableStateOf(spell.damageDice) }

    var description by remember(spell.description) { mutableStateOf(spell.description) }
    var higherLevels by remember(spell.higherLevels) { mutableStateOf(spell.higherLevels ?: "") }

    // Validation
    val isNameValid = name.isNotBlank()
    val isDescriptionValid = description.isNotBlank()
    val isMaterialValid = if (components.contains(Component.MATERIAL)) {
        material.isNotBlank()
    } else {
        true
    }

    val damageDiceValidRegex = Regex("""^([1-9]\d*)?[dDкК](4|6|8|10|12|20|100)$""")

    val damageDiceError = damageType != null &&
            (damageDice.isNullOrBlank() || !damageDiceValidRegex.matches(damageDice ?: ""))

    val isAttackTypeValid = !(attackType == AttackType.SAVE && saveStat == null)

    val isDistanceValid = !(rangeType == SpellRangeType.DISTANCE && rangeValue == null)

    val isFormValid =
        isNameValid && isDescriptionValid && isMaterialValid && !damageDiceError && isAttackTypeValid && isDistanceValid

    val onSaveClick = {

        if (isFormValid) {
            val updatedSpell = spell.copy(
                name = name.trim(),
                level = level,
                school = school,
                castTime = castTime,
                rangeType = rangeType,
                rangeValue = rangeValue,
                components = components,
                material = if (components.contains(Component.MATERIAL)) material else null,
                duration = duration,
                isRitual = isRitual,
                isConcentration = isConcentration,
                attackType = attackType,
                saveStat = saveStat,
                damageType = damageType,
                damageDice = damageDice,
                description = description.trim(),
                higherLevels = higherLevels.trim()
            )

            Log.i("UpdatedSpell", "$updatedSpell")

            onUpdate(updatedSpell)
            onNavigateBack()
        }
    }

    val coroutineScope = rememberCoroutineScope()

    val descriptionRequester = remember { BringIntoViewRequester() }
    val higherLevelRequester = remember { BringIntoViewRequester() }
    val materialsRequester = remember { BringIntoViewRequester() }

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
                onClick = {
                    if (isFormValid) {
                        onSaveClick()
                    }
                },
                containerColor = if (isFormValid) FloatingActionButtonDefaults.containerColor else Color.Gray,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = "Save Spell")
            }
        },
    ) { padding ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding()
                .padding(16.dp),
        ) {
            // --- BASIC INFO ---
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.spell_name) + "*") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = !isNameValid,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )
            }

            item {
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
            }
            // --- CASTING ---
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EnumDropdown(
                        value = castTime,
                        labelRes = R.string.msg_casting_time,
                        options = SpellCastTime.entries,
                        nameMapper = { stringResource(it.resId) },
                        onSelected = { castTime = it },
                        modifier = Modifier.weight(1f)
                    )

                    EnumDropdown(
                        value = rangeType,
                        labelRes = R.string.range_distance,
                        options = SpellRangeType.entries,
                        nameMapper = { stringResource(it.resId) },
                        onSelected = { rangeType = it },
                        modifier = Modifier.weight(1f)
                    )
                    if (rangeType == SpellRangeType.DISTANCE) {
                        IntTextField(
                            value = rangeValue ?: 0,
                            label = "",
                            isError = !isDistanceValid,
                            validate = { input -> (input.toIntOrNull() ?: 0) < 1000 },
                            onValueChange = { rangeValue = it },
                            modifier = Modifier.weight(0.5f)
                        )
                    }
                }
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
            }

            // --- COMPONENTS (V, S, M) ---
            item {
                Text(
                    stringResource(R.string.msg_components),
                    style = MaterialTheme.typography.labelLarge
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(Component.entries) { component ->
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
                            label = {
                                Text(
                                    stringResource(component.resId),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                    }
                }
            }

            if (components.contains(Component.MATERIAL)) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .bringIntoViewRequester(materialsRequester)
                            .onFocusEvent { focusState ->
                                if (focusState.isFocused) {
                                    coroutineScope.launch {
                                        materialsRequester.bringIntoView()
                                    }
                                }
                            }
                    ) {
                        OutlinedTextField(
                            value = material,
                            onValueChange = { material = it },
                            isError = !isMaterialValid,
                            label = { Text(stringResource(R.string.spell_material_component) + "*") },
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 2
                        )
                    }
                }
            }

            // --- DURATION & FLAGS ---
            item {
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
            }

            item {
                val ritualInteractionSource = remember { MutableInteractionSource() }
                val concentrationInteractionSource = remember { MutableInteractionSource() }

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(
                            interactionSource = ritualInteractionSource,
                            indication = null,
                            onClick = {
                                isRitual = !isRitual
                            }
                        )
                    ) {
                        Checkbox(
                            checked = isRitual,
                            onCheckedChange = { isRitual = it }
                        )
                        Text(stringResource(R.string.ritual))
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(
                            interactionSource = concentrationInteractionSource,
                            indication = null,
                            onClick = {
                                isConcentration = !isConcentration
                            }
                        )
                    ) {
                        Checkbox(
                            checked = isConcentration,
                            onCheckedChange = { isConcentration = it }
                        )
                        Text(stringResource(R.string.concentration))
                    }
                }
            }

            item {
                HorizontalDivider()
            }

            // --- COMBAT STATS ---
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EnumDropdown(
                        value = attackType,
                        labelRes = R.string.spell_attack_type,
                        options = AttackType.entries,
                        nameMapper = { stringResource(it.resId) },
                        onSelected = {
                            attackType = it
                            if (it == AttackType.NONE) saveStat = null
                        },
                        modifier = Modifier.weight(1f)
                    )
                    if (attackType == AttackType.SAVE) {
                        EnumDropdown(
                            value = saveStat,
                            labelRes = R.string.spell_save_stat,
                            options = Ability.entries.filter { it != Ability.NONE },
                            nameMapper = {
                                it?.let { stringResource(it.nameRes) } ?: ""
                            },
                            onSelected = { saveStat = it },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EnumDropdown(
                        value = damageType,
                        labelRes = R.string.spell_damage_type,
                        options = listOf(null) + DamageType.entries,
                        nameMapper = {
                            it?.let { stringResource(it.resId) } ?: stringResource(R.string.none)
                        },
                        onSelected = {
                            damageType = it
                            if (it == null) damageDice = null
                        },
                        modifier = Modifier.weight(1f)
                    )
                    if (damageType != null) {

                        val damageDiceTypingRegex =
                            Regex("""^([1-9]\d*)?([dDкК](1(00?|2)?|20?|4|6|8)?)?$""")

                        OutlinedTextField(
                            value = damageDice ?: "",
                            onValueChange = {
                                if (it.isEmpty() || damageDiceTypingRegex.matches(it)) {
                                    damageDice = it
                                }
                            },
                            label = { Text(stringResource(R.string.spell_damage_dice)) },
                            placeholder = { Text((stringResource(R.string.placeholder_dice))) },
                            isError = damageDiceError,
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // --- DESCRIPTION ---
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(descriptionRequester)
                        .onFocusEvent { focusState ->
                            if (focusState.isFocused) {
                                coroutineScope.launch {
                                    descriptionRequester.bringIntoView()
                                }
                            }
                        }
                ) {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text(stringResource(R.string.spell_description) + "*") },
                        isError = !isDescriptionValid,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 3
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(higherLevelRequester)
                        .onFocusEvent { focusState ->
                            if (focusState.isFocused) {
                                coroutineScope.launch {
                                    higherLevelRequester.bringIntoView()
                                }
                            }
                        }
                ) {
                    OutlinedTextField(
                        value = higherLevels,
                        onValueChange = { higherLevels = it },
                        label = { Text(stringResource(R.string.spell_higher_levels)) },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences
                        ),
                        modifier = Modifier
                            .fillMaxWidth(),
                        minLines = 2,
                        maxLines = 2
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}

@Preview(locale = "uk")
@Composable
private fun SpellEditScreenPreview() {
    DnDSheetTheme {
        SpellEditScreen(
            spell = UiUtils.sampleSpells.filter { it.material != null }.map { it }.first(),
            onUpdate = {},
            onNavigateBack = {}
        )
    }
}