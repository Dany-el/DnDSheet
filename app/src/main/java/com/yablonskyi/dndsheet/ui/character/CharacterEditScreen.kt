package com.yablonskyi.dndsheet.ui.character

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import com.yablonskyi.dndsheet.R
import com.yablonskyi.dndsheet.data.model.character.Ability
import com.yablonskyi.dndsheet.data.model.character.Character
import com.yablonskyi.dndsheet.ui.theme.DnDSheetTheme
import com.yablonskyi.dndsheet.ui.utils.UiUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterEditScreen(
    character: Character,
    onUpdate: (Character) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                            contentDescription = "Localized description",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                title = {
                    Text(
                        text = stringResource(R.string.settings),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            )
        },
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item {
                CharacterGeneralInfo(
                    character = character,
                    onUpdate = onUpdate
                )
            }
            item {
                ClassSettings(
                    character = character,
                    onUpdate = onUpdate
                )
            }
            item {
                SpellSettings(
                    character = character,
                    onUpdate = onUpdate
                )
            }
        }
    }
}

@Composable
fun CharacterGeneralInfo(
    character: Character,
    onUpdate: (Character) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember(character.name) { mutableStateOf(character.name) }
    var race by remember(character.race) { mutableStateOf(character.race) }
    var charClass by remember(character.charClass) { mutableStateOf(character.charClass) }
    var subClass by remember(character.subClass) { mutableStateOf(character.subClass) }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { newName ->
                    name = newName
                    onUpdate(character.copy(name = newName))
                },
                singleLine = true,
                label = {
                    Text(
                        stringResource(R.string.char_name),
                    )
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = race,
                onValueChange = { newRace ->
                    race = newRace
                    onUpdate(character.copy(race = newRace))
                },
                singleLine = true,
                label = {
                    Text(
                        stringResource(R.string.char_race),
                    )
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = charClass,
                onValueChange = { newClass ->
                    charClass = newClass
                    onUpdate(character.copy(charClass = newClass))
                },
                singleLine = true,
                label = {
                    Text(
                        stringResource(R.string.char_class),
                    )
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = subClass,
                onValueChange = { newSubClass ->
                    subClass = newSubClass
                    onUpdate(character.copy(subClass = newSubClass))
                },
                singleLine = true,
                label = {
                    Text(
                        stringResource(R.string.char_subclass),
                    )
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.weight(1f)
            )
            IntTextField(
                value = character.level,
                onValueChange = { newLevel ->
                    if (newLevel in 1..100) {
                        onUpdate(character.copy(level = newLevel))
                    }
                },
                label = stringResource(R.string.spell_level),
                modifier = Modifier.weight(0.8f)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            IntTextField(
                value = character.armorClass,
                onValueChange = { newArmorClass ->
                    if (newArmorClass <= 100)
                        onUpdate(character.copy(armorClass = newArmorClass))
                },
                label = stringResource(R.string.char_ac),
                modifier = Modifier.weight(1f)
            )
            IntTextField(
                value = character.shield,
                onValueChange = { newShield ->
                    if (newShield <= 100)
                        onUpdate(character.copy(shield = newShield))
                },
                label = stringResource(R.string.char_shield),
                modifier = Modifier.weight(1f)
            )
            IntTextField(
                value = character.speed,
                onValueChange = { newSpeed ->
                    if (newSpeed <= 100)
                        onUpdate(character.copy(speed = newSpeed))
                },
                label = stringResource(R.string.char_speed),
                modifier = Modifier.weight(1f)
            )
            IntTextField(
                value = character.initiativeMiscBonus,
                onValueChange = { newInitBonus ->
                    if (newInitBonus <= 100)
                        onUpdate(character.copy(initiativeMiscBonus = newInitBonus))
                },
                label = stringResource(R.string.char_initiative_bonus),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ClassSettings(
    character: Character,
    onUpdate: (Character) -> Unit,
    isExpanded: Boolean = false
) {
    var expanded by remember { mutableStateOf(isExpanded) }

    Surface(
        color = OutlinedTextFieldDefaults.colors().unfocusedContainerColor,
        shape = MaterialTheme.shapes.extraSmall,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                expanded = !expanded
            }
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.class_settings),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                )
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.jack_of_all_trades),
                    )
                    Checkbox(
                        checked = character.hasJackOfAllTrades,
                        onCheckedChange = {
                            onUpdate(character.copy(hasJackOfAllTrades = it))
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun SpellSettings(
    character: Character,
    onUpdate: (Character) -> Unit,
    isExpanded: Boolean = false
) {
    var cardExpanded by remember { mutableStateOf(isExpanded) }
    var menuExpanded by remember { mutableStateOf(false) }

    val density = LocalDensity.current
    var textFieldSize: Size by remember { mutableStateOf(Size.Zero) }

    Surface(
        color = OutlinedTextFieldDefaults.colors().unfocusedContainerColor,
        shape = MaterialTheme.shapes.extraSmall,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                cardExpanded = !cardExpanded
            }
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.spell_settings),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (cardExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                )
            }
            if (cardExpanded) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .onGloballyPositioned { coordinates ->
                                textFieldSize = coordinates.size.toSize()
                            }
                    ) {
                        OutlinedTextField(
                            value = stringResource(
                                character.spellSettings.spellCastingAbility?.nameRes
                                    ?: R.string.ability_none
                            ),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.spell_ability)) },
                            trailingIcon = {
                                Icon(Icons.Default.ArrowDropDown, "Select Ability")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { menuExpanded = true },
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier.width(with(density) { textFieldSize.width.toDp() })
                        ) {
                            Ability.entries.forEach { ability ->
                                DropdownMenuItem(
                                    text = { Text(stringResource(ability.nameRes)) },
                                    onClick = {
                                        menuExpanded = false
                                        onUpdate(
                                            character.copy(
                                                spellSettings = character.spellSettings.copy(
                                                    spellCastingAbility = ability
                                                )
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }
                    IntTextField(
                        value = character.spellSettings.dcMiscBonus,
                        onValueChange = { newDcBonus ->
                            if (newDcBonus <= 100)
                                onUpdate(
                                    character.copy(
                                        spellSettings = character.spellSettings.copy(
                                            dcMiscBonus = newDcBonus
                                        )
                                    )
                                )
                        },
                        label = stringResource(R.string.spell_saving_throw_bonus),
                        modifier = Modifier.weight(0.5f)
                    )
                    IntTextField(
                        value = character.spellSettings.attackMiscBonus,
                        onValueChange = { newAttackBonus ->
                            if (newAttackBonus <= 100)
                                onUpdate(
                                    character.copy(
                                        spellSettings = character.spellSettings.copy(
                                            attackMiscBonus = newAttackBonus
                                        )
                                    )
                                )
                        },
                        label = stringResource(R.string.spell_attack_bonus),
                        modifier = Modifier.weight(0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun IntTextField(
    value: Int,
    label: String,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember(value) { mutableStateOf(value.toString()) }

    OutlinedTextField(
        value = text,
        onValueChange = { newText ->
            if (newText.all { it.isDigit() }) {
                text = newText
                val intValue = newText.toIntOrNull() ?: value // if null, pass old value
                onValueChange(intValue)
            }
        },
        label = {
            Text(
                label,
                maxLines = 2,
                overflow = TextOverflow.MiddleEllipsis
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next
        ),
        singleLine = true,
        modifier = modifier
    )
}

@Preview(showBackground = false, locale = "ru")
@Composable
private fun CharacterSheetScreenPreview() {
    DnDSheetTheme {
        CharacterEditScreen(
            character = UiUtils.sampleCharacters.first(),
            onUpdate = {},
            onNavigateBack = {},
        )
    }
}

@Preview(group = "Class Settings")
@Composable
private fun ClassSettingsPreview_expanded() {
    DnDSheetTheme {
        ClassSettings(
            character = UiUtils.sampleCharacters.first(),
            onUpdate = {},
            isExpanded = true
        )
    }
}

@Preview(group = "Class Settings")
@Composable
private fun ClassSettingsPreview_unexpended() {
    DnDSheetTheme {
        ClassSettings(
            character = UiUtils.sampleCharacters.first(),
            onUpdate = {},
        )
    }
}

@Preview(group = "Spell Settings")
@Composable
private fun SpellSettingsPreview_expanded() {
    DnDSheetTheme {
        SpellSettings(
            character = UiUtils.sampleCharacters.first(),
            onUpdate = {},
            isExpanded = true
        )
    }
}

@Preview(group = "Spell Settings")
@Composable
private fun SpellSettingsPreview_unexpended() {
    DnDSheetTheme {
        SpellSettings(
            character = UiUtils.sampleCharacters.first(),
            onUpdate = {},
        )
    }
}