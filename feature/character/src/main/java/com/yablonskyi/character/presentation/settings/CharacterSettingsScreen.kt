package com.yablonskyi.character.presentation.settings

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.window.core.layout.WindowSizeClass
import com.yablonskyi.character.presentation.common.UiUtils
import com.yablonskyi.character.presentation.settings.components.CharacterGeneralInfo
import com.yablonskyi.character.presentation.settings.components.CharacterGeneralInfoExpanded
import com.yablonskyi.domain.character.CharacterChange
import com.yablonskyi.domain.character.CharacterNumberField
import com.yablonskyi.model.character.Ability
import com.yablonskyi.model.character.Character
import com.yablonskyi.model.character.SpellLevel
import com.yablonskyi.ui.R
import com.yablonskyi.ui.theme.DnDSheetTheme
import com.yablonskyi.ui.utils.EnumDropdown
import com.yablonskyi.ui.utils.IntTextField
import com.yablonskyi.ui.utils.NumbersTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterSettingsScreen(
    state: CharacterSettingsState,
    onIntent: (CharacterSettingsIntent) -> Unit,
) {
    val character = state.character ?: return
    val onUpdate: (CharacterChange) -> Unit = { onIntent(CharacterSettingsIntent.Change(it)) }
    val onSpellSlotsUpdate: (SpellLevel, Int) -> Unit = { level, max -> onUpdate(CharacterChange.SlotMaximum(level, max)) }
    val onImagePicker: () -> Unit = { onIntent(CharacterSettingsIntent.ImagePickerClicked) }
    val onNavigateBack: () -> Unit = { onIntent(CharacterSettingsIntent.BackClicked) }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val hasEnoughWidth =
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

    val isWideScreen = hasEnoughWidth && isLandscape

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Localized description",
                        )
                    }
                },
                title = {
                    Text(
                        text = stringResource(R.string.settings),
                        fontWeight = FontWeight.SemiBold,
                        textAlign = if (isWideScreen) TextAlign.Center else TextAlign.Left,
                        modifier = if (isWideScreen) Modifier.fillMaxWidth() else Modifier.fillMaxWidth(),
                    )
                }
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.widthIn(max = 840.dp)
            ) {
                item {
                    if (isWideScreen) {
                        CharacterGeneralInfoExpanded(
                            character = character,
                            onImagePicker = onImagePicker,
                            onUpdate = onUpdate
                        )
                    } else {
                        CharacterGeneralInfo(
                            character = character,
                            onImagePicker = onImagePicker,
                            onUpdate = onUpdate
                        )
                    }
                }
                item {
                    ClassSettings(
                        character = character,
                        onUpdate = onUpdate
                    )
                }
                item {
                    if (isWideScreen) {
                        SpellSettingsExpanded(
                            character = character,
                            onSpellSlotsUpdate = onSpellSlotsUpdate,
                            onUpdate = onUpdate
                        )
                    } else {
                        SpellSettings(
                            character = character,
                            onSpellSlotsUpdate = onSpellSlotsUpdate,
                            onUpdate = onUpdate
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ClassSettings(
    character: Character,
    onUpdate: (CharacterChange) -> Unit,
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
                    onClick = { onUpdate(CharacterChange.JackOfAllTrades(!character.hasJackOfAllTrades)) })
            ) {
                Text(
                    text = stringResource(R.string.jack_of_all_trades),
                )
                Checkbox(
                    checked = character.hasJackOfAllTrades,
                    onCheckedChange = {
                        onUpdate(CharacterChange.JackOfAllTrades(it))
                    },
                )
            }
        }
    }
}

@Composable
fun SpellSettings(
    character: Character,
    onUpdate: (CharacterChange) -> Unit,
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
                        onUpdate(CharacterChange.CastingAbility(it))
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    NumbersTextField(
                        value = character.spellSettings.dcMiscBonus,
                        validate = { input -> (input.toIntOrNull() ?: 0) in (-100..100) },
                        onValueChange = { newDcBonus ->
                            onUpdate(CharacterChange.Number(CharacterNumberField.DC_MISC_BONUS, newDcBonus))
                        },
                        label = stringResource(R.string.spell_saving_throw_bonus),
                        modifier = Modifier.weight(0.5f)
                    )
                    NumbersTextField(
                        value = character.spellSettings.attackMiscBonus,
                        validate = { input -> (input.toIntOrNull() ?: 0) in (-100..100) },
                        onValueChange = { newAttackBonus ->
                            onUpdate(CharacterChange.Number(CharacterNumberField.ATTACK_MISC_BONUS, newAttackBonus))
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

@Composable
fun SpellSettingsExpanded(
    character: Character,
    onUpdate: (CharacterChange) -> Unit,
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                            onUpdate(CharacterChange.CastingAbility(it))
                        },
                        modifier = Modifier.weight(1f)
                    )
                    NumbersTextField(
                        value = character.spellSettings.dcMiscBonus,
                        validate = { input -> (input.toIntOrNull() ?: 0) in (-100..100) },
                        onValueChange = { newDcBonus ->
                            onUpdate(CharacterChange.Number(CharacterNumberField.DC_MISC_BONUS, newDcBonus))
                        },
                        label = stringResource(R.string.spell_saving_throw_bonus),
                        modifier = Modifier.weight(0.5f)
                    )
                    NumbersTextField(
                        value = character.spellSettings.attackMiscBonus,
                        validate = { input -> (input.toIntOrNull() ?: 0) in (-100..100) },
                        onValueChange = { newAttackBonus ->
                            onUpdate(CharacterChange.Number(CharacterNumberField.ATTACK_MISC_BONUS, newAttackBonus))
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
        CharacterSettingsScreen(state = CharacterSettingsState(character = UiUtils.sampleCharacters.first()), onIntent = {})
    }
}

@Preview(
    showBackground = false, locale = "ru",
    device = "spec:width=1280dp,height=800dp,dpi=240,orientation=portrait"
)
@Composable
private fun CharacterSheetScreenPreview_Tablet() {
    DnDSheetTheme {
        CharacterSettingsScreen(state = CharacterSettingsState(character = UiUtils.sampleCharacters.first()), onIntent = {})
    }
}

@Preview(group = "Class Settings")
@Composable
private fun ClassSettingsPreview() {
    DnDSheetTheme {
        ClassSettings(
            character = UiUtils.sampleCharacters.first(),
            onUpdate = {},
        )
    }
}

@Preview(group = "Spell Settings")
@Composable
private fun SpellSettingsPreview() {
    DnDSheetTheme {
        SpellSettings(
            character = UiUtils.sampleCharacters.first(),
            onSpellSlotsUpdate = { _, _ -> },
            onUpdate = {},
        )
    }
}