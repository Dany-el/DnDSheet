package com.yablonskyi.character.presentation.settings.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yablonskyi.character.presentation.settings.CharacterSettingsFormIntent
import com.yablonskyi.character.presentation.settings.CharacterSettingsFormUiState
import com.yablonskyi.domain.character.CharacterChange
import com.yablonskyi.domain.character.CharacterNumberField
import com.yablonskyi.domain.character.CharacterTextField
import com.yablonskyi.ui.R
import com.yablonskyi.ui.utils.DnDSheetOutlinedTextField

@Composable
fun CharacterGeneralInfo(
    state: CharacterSettingsFormUiState, onIntent: (CharacterSettingsFormIntent) -> Unit,
    onImagePicker: () -> Unit, modifier: Modifier = Modifier, wide: Boolean = false
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CharacterProfilePicturePicker(state.character?.imagePath, onImagePicker)
        listOf(
            CharacterTextField.NAME to R.string.char_name,
            CharacterTextField.RACE to R.string.char_race,
            CharacterTextField.CHAR_CLASS to R.string.char_class,
            CharacterTextField.SUB_CLASS to R.string.char_subclass
        )
            .chunked(if (wide) 4 else 2).forEach { fields ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    fields.forEach { (field, label) ->
                        DnDSheetOutlinedTextField(
                            state.texts.getValue(field),
                            {
                                onIntent(
                                    CharacterSettingsFormIntent.Change(
                                        CharacterChange.Text(
                                            field,
                                            it
                                        )
                                    )
                                )
                            },
                            stringResource(label),
                            { onIntent(CharacterSettingsFormIntent.TextFocus(field, it)) },
                            Modifier.weight(1f)
                        )
                    }
                }
            }
        LevelControl(state, onIntent)
        listOf(
            CharacterNumberField.ARMOR_CLASS to R.string.char_ac,
            CharacterNumberField.SHIELD to R.string.char_shield,
            CharacterNumberField.SPEED to R.string.char_speed,
            CharacterNumberField.INITIATIVE_MISC_BONUS to R.string.char_initiative_bonus
        )
            .chunked(if (wide) 4 else 2)
            .forEach { fields ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    fields.forEach { (field, label) ->
                        SettingsNumberInput(
                            state,
                            field,
                            label,
                            onIntent,
                            Modifier.weight(1f)
                        )
                    }
                }
            }
    }
}

@Composable
fun SettingsNumberInput(
    state: CharacterSettingsFormUiState, field: CharacterNumberField, label: Int,
    onIntent: (CharacterSettingsFormIntent) -> Unit, modifier: Modifier = Modifier
) {
    val value = state.numbers.getValue(field)
    DnDSheetOutlinedTextField(
        value,
        { onIntent(CharacterSettingsFormIntent.Change(CharacterChange.Number(field, it))) },
        stringResource(label),
        { onIntent(CharacterSettingsFormIntent.NumberFocus(field, it)) },
        modifier,
        allowSigned = value.minValue < 0,
        showMaximum = field == CharacterNumberField.LEVEL
    )
}

@Composable
fun LevelControl(
    state: CharacterSettingsFormUiState,
    onIntent: (CharacterSettingsFormIntent) -> Unit
) {
    val level = state.numbers.getValue(CharacterNumberField.LEVEL)
    val focus = LocalFocusManager.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SettingsNumberInput(
            state,
            CharacterNumberField.LEVEL,
            R.string.spell_level,
            onIntent,
            Modifier.weight(1f)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            IconButton(
                onClick = { focus.clearFocus(); onIntent(CharacterSettingsFormIntent.IncreaseLevel) },
                enabled = level.isValid && level.value < 20,
                shape = CircleShape,
                modifier = Modifier
                    .sizeIn(48.dp)
                    .border(
                        BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
                        CircleShape
                    )
            ) {
                Icon(Icons.Default.Add, stringResource(R.string.level_increase))
            }
            IconButton(
                onClick = { focus.clearFocus(); onIntent(CharacterSettingsFormIntent.DecreaseLevel) },
                enabled = level.isValid && level.value > 1,
                shape = CircleShape,
                modifier = Modifier
                    .sizeIn(48.dp)
                    .border(
                        BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
                        CircleShape
                    )
            ) {
                Icon(Icons.Default.Remove, stringResource(R.string.level_decrease))
            }
        }
    }
}