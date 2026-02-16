package com.yablonskyi.dndsheet.ui.character

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.yablonskyi.dndsheet.R
import com.yablonskyi.dndsheet.data.model.character.Ability
import com.yablonskyi.dndsheet.data.model.character.Character
import com.yablonskyi.dndsheet.data.model.character.SpellLevel
import com.yablonskyi.dndsheet.ui.theme.DnDSheetTheme
import com.yablonskyi.dndsheet.ui.utils.EnumDropdown
import com.yablonskyi.dndsheet.ui.utils.IntTextField
import com.yablonskyi.dndsheet.ui.utils.UiUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterEditScreen(
    character: Character,
    onUpdate: (Character) -> Unit,
    onSpellSlotsUpdate: (SpellLevel, Int) -> Unit,
    onImageUpdated: (String) -> Unit,
    onNavigateBack: () -> Unit,
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
                .imePadding()
                .padding(innerPadding)
        ) {
            item {
                CharacterGeneralInfo(
                    character = character,
                    onImageUpdated = onImageUpdated,
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
                    onSpellSlotsUpdate = onSpellSlotsUpdate,
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
    onImageUpdated: (String) -> Unit,
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
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CharacterProfilePicture(
                currentImagePath = character.imagePath,
                onImageUpdated = onImageUpdated
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
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
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                        capitalization = KeyboardCapitalization.Sentences
                    ),
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
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                        capitalization = KeyboardCapitalization.Sentences
                    ),
//                    modifier = Modifier.weight(1f)
                )
            }
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
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    capitalization = KeyboardCapitalization.Sentences
                ),
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
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    capitalization = KeyboardCapitalization.Sentences
                ),
                modifier = Modifier.weight(1f)
            )
            IntTextField(
                value = character.level,
                validate = { input -> (input.toIntOrNull() ?: 1) < 20 },
                onValueChange = { newLevel ->
                    onUpdate(character.copy(level = newLevel))
                },
                label = stringResource(R.string.spell_level),
                modifier = Modifier.weight(0.6f)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            IntTextField(
                value = character.armorClass,
                validate = { input -> (input.toIntOrNull() ?: 0) < 100 },
                onValueChange = { newArmorClass ->
                    onUpdate(character.copy(armorClass = newArmorClass))
                },
                label = stringResource(R.string.char_ac),
                modifier = Modifier.weight(1f)
            )
            IntTextField(
                value = character.shield,
                validate = { input -> (input.toIntOrNull() ?: 0) < 100 },
                onValueChange = { newShield ->
                    onUpdate(character.copy(shield = newShield))
                },
                label = stringResource(R.string.char_shield),
                modifier = Modifier.weight(1f)
            )
            IntTextField(
                value = character.speed,
                validate = { input -> (input.toIntOrNull() ?: 0) < 100 },
                onValueChange = { newSpeed ->
                    onUpdate(character.copy(speed = newSpeed))
                },
                label = stringResource(R.string.char_speed),
                modifier = Modifier.weight(1f)
            )
            IntTextField(
                value = character.initiativeMiscBonus,
                onValueChange = { newInitBonus ->
                    if (newInitBonus < 100)
                        onUpdate(character.copy(initiativeMiscBonus = newInitBonus))
                },
                label = stringResource(R.string.char_initiative_bonus),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun CharacterProfilePicture(
    currentImagePath: String?,
    onImageUpdated: (String) -> Unit,
    modifier: Modifier = Modifier,
    enableImagePicker: Boolean = true,
    shape: Shape = CircleShape,
    height: Dp = 140.dp,
    width: Dp = 140.dp,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                val savedPath = saveImageToInternalStorage(context, uri, currentImagePath)
                if (savedPath != null) {
                    onImageUpdated(savedPath)
                }
            }
        }
    }

    Surface(
        modifier = modifier
            .height(height)
            .width(width)
            .clip(shape)
            .clickable(
                enabled = enableImagePicker
            ) {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            },
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        if (currentImagePath != null) {
            AsyncImage(
                model = currentImagePath,
                contentDescription = "Character Profile",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Add Photo",
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxSize()
            )
        }
    }
}

@Composable
fun ClassSettings(
    character: Character,
    onUpdate: (Character) -> Unit,
) {
    Surface(
        color = OutlinedTextFieldDefaults.colors().unfocusedContainerColor,
        shape = MaterialTheme.shapes.extraSmall,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier
            .fillMaxWidth()
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
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onUpdate(character.copy(hasJackOfAllTrades = !character.hasJackOfAllTrades)) })
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

@Composable
fun SpellSettings(
    character: Character,
    onUpdate: (Character) -> Unit,
    onSpellSlotsUpdate: (SpellLevel, Int) -> Unit,
) {
    val focusManager = LocalFocusManager.current

    Surface(
        color = OutlinedTextFieldDefaults.colors().unfocusedContainerColor,
        shape = MaterialTheme.shapes.extraSmall,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
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
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                EnumDropdown(
                    value = character.spellSettings.spellCastingAbility,
                    labelRes = R.string.spell_ability,
                    options = Ability.entries,
                    nameMapper = {
                        it?.let {
                            stringResource(it.nameRes)
                        } ?: ""
                    },
                    onSelected = {
                        onUpdate(
                            character.copy(
                                spellSettings = character.spellSettings.copy(
                                    spellCastingAbility = it
                                )
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    IntTextField(
                        value = character.spellSettings.dcMiscBonus,
                        validate = { input -> (input.toIntOrNull() ?: 0) < 100 },
                        onValueChange = { newDcBonus ->
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
                        validate = { input -> (input.toIntOrNull() ?: 0) < 100 },
                        onValueChange = { newAttackBonus ->
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

                Spacer(Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.spell_slots),
                    fontWeight = FontWeight.SemiBold,
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SpellLevel.entries.subList(1, 4).forEach { level ->
                        IntTextField(
                            value = character.spellSettings.spellSlots[level]?.max ?: 0,
                            validate = { input -> (input.toIntOrNull() ?: 0) < 10 },
                            onValueChange = { newMaxValue ->
                                onSpellSlotsUpdate(level, newMaxValue)
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                }
                            ),
                            label = stringResource(level.resId),
                            modifier = Modifier.weight(0.2f)
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SpellLevel.entries.subList(4, 7).forEach { level ->
                        IntTextField(
                            value = character.spellSettings.spellSlots[level]?.max ?: 0,
                            validate = { input -> (input.toIntOrNull() ?: 0) < 10 },
                            onValueChange = { newMaxValue ->
                                onSpellSlotsUpdate(level, newMaxValue)
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                }
                            ),
                            label = stringResource(level.resId),
                            modifier = Modifier.weight(0.2f)
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SpellLevel.entries.subList(7, 10).forEach { level ->
                        IntTextField(
                            value = character.spellSettings.spellSlots[level]?.max ?: 0,
                            validate = { input -> (input.toIntOrNull() ?: 0) < 10 },
                            onValueChange = { newMaxValue ->
                                onSpellSlotsUpdate(level, newMaxValue)
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                }
                            ),
                            label = stringResource(level.resId),
                            modifier = Modifier.weight(0.2f)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = false, locale = "ru")
@Composable
private fun CharacterSheetScreenPreview() {
    DnDSheetTheme {
        CharacterEditScreen(
            character = UiUtils.sampleCharacters.first(),
            onUpdate = {},
            onSpellSlotsUpdate = { _, _ -> },
            onNavigateBack = {},
            onImageUpdated = {}
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
            onSpellSlotsUpdate = { _, _ -> },
            onUpdate = {},
        )
    }
}

@Preview(group = "Spell Settings")
@Composable
private fun SpellSettingsPreview_unexpended() {
    DnDSheetTheme {
        SpellSettings(
            character = UiUtils.sampleCharacters.first(),
            onSpellSlotsUpdate = { _, _ -> },
            onUpdate = {},
        )
    }
}