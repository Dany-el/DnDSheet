package com.yablonskyi.character.presentation.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.yablonskyi.domain.character.CharacterChange
import com.yablonskyi.domain.character.CharacterNumberField
import com.yablonskyi.domain.character.CharacterTextField
import com.yablonskyi.model.character.Character
import com.yablonskyi.ui.R
import com.yablonskyi.ui.utils.DnDSheetOutlinedTextField
import com.yablonskyi.ui.utils.IntTextField

@Composable
fun CharacterGeneralInfo(
    character: Character,
    onUpdate: (CharacterChange) -> Unit,
    onImagePicker: () -> Unit,
    modifier: Modifier = Modifier
) {

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        CharacterProfilePicture(
            currentImagePath = character.imagePath,
            onImagePicker = onImagePicker,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = character.name,
                    onValueChange = { newName ->
                        onUpdate(CharacterChange.Text(CharacterTextField.NAME, newName))
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
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = character.race,
                    onValueChange = { newRace ->

                        onUpdate(CharacterChange.Text(CharacterTextField.RACE, newRace))
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
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = character.charClass,
                onValueChange = { newClass ->

                    onUpdate(CharacterChange.Text(CharacterTextField.CHAR_CLASS, newClass))
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
                value = character.subClass,
                onValueChange = { newSubClass ->

                    onUpdate(CharacterChange.Text(CharacterTextField.SUB_CLASS, newSubClass))
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
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            IntTextField(
                value = character.level,
                validate = { input -> (input.toIntOrNull() ?: 1) < 20 },
                onValueChange = { newLevel ->
                    onUpdate(CharacterChange.Number(CharacterNumberField.LEVEL, newLevel))
                },
                label = stringResource(R.string.spell_level),
                modifier = Modifier.weight(1f)
            )
            IntTextField(
                value = character.armorClass,
                validate = { input -> (input.toIntOrNull() ?: 0) < 100 },
                onValueChange = { newArmorClass ->
                    onUpdate(CharacterChange.Number(CharacterNumberField.ARMOR_CLASS, newArmorClass))
                },
                label = stringResource(R.string.char_ac),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ){
            IntTextField(
                value = character.speed,
                validate = { input -> (input.toIntOrNull() ?: 0) < 100 },
                onValueChange = { newSpeed ->
                    onUpdate(CharacterChange.Number(CharacterNumberField.SPEED, newSpeed))
                },
                label = stringResource(R.string.char_speed),
                modifier = Modifier.weight(1f)
            )
            IntTextField(
                value = character.initiativeMiscBonus,
                onValueChange = { newInitBonus ->
                    if (newInitBonus < 100)
                        onUpdate(CharacterChange.Number(CharacterNumberField.INITIATIVE_MISC_BONUS, newInitBonus))
                },
                label = stringResource(R.string.char_initiative_bonus),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun CharacterGeneralInfoExpanded(
    character: Character,
    onUpdate: (CharacterChange) -> Unit,
    onImagePicker: () -> Unit,
    modifier: Modifier = Modifier
) {

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CharacterProfilePicture(
                currentImagePath = character.imagePath,
                onImagePicker = onImagePicker
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = character.name,
                        onValueChange = { newName ->

                            onUpdate(CharacterChange.Text(CharacterTextField.NAME, newName))
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
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = character.charClass,
                        onValueChange = { newClass ->

                            onUpdate(CharacterChange.Text(CharacterTextField.CHAR_CLASS, newClass))
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
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = character.race,
                        onValueChange = { newRace ->

                            onUpdate(CharacterChange.Text(CharacterTextField.RACE, newRace))
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
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = character.subClass,
                        onValueChange = { newSubClass ->

                            onUpdate(CharacterChange.Text(CharacterTextField.SUB_CLASS, newSubClass))
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
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            IntTextField(
                value = character.level,
                validate = { input -> (input.toIntOrNull() ?: 1) < 20 },
                onValueChange = { newLevel ->
                    onUpdate(CharacterChange.Number(CharacterNumberField.LEVEL, newLevel))
                },
                label = stringResource(R.string.spell_level),
                modifier = Modifier.weight(1f)
            )
            IntTextField(
                value = character.armorClass,
                validate = { input -> (input.toIntOrNull() ?: 0) < 100 },
                onValueChange = { newArmorClass ->
                    onUpdate(CharacterChange.Number(CharacterNumberField.ARMOR_CLASS, newArmorClass))
                },
                label = stringResource(R.string.char_ac),
                modifier = Modifier.weight(1f)
            )
            IntTextField(
                value = character.shield,
                validate = { input -> (input.toIntOrNull() ?: 0) < 100 },
                onValueChange = { newShield ->
                    onUpdate(CharacterChange.Number(CharacterNumberField.SHIELD, newShield))
                },
                label = stringResource(R.string.char_shield),
                modifier = Modifier.weight(1f)
            )
            IntTextField(
                value = character.speed,
                validate = { input -> (input.toIntOrNull() ?: 0) < 100 },
                onValueChange = { newSpeed ->
                    onUpdate(CharacterChange.Number(CharacterNumberField.SPEED, newSpeed))
                },
                label = stringResource(R.string.char_speed),
                modifier = Modifier.weight(1f)
            )
            IntTextField(
                value = character.initiativeMiscBonus,
                onValueChange = { newInitBonus ->
                    if (newInitBonus < 100)
                        onUpdate(CharacterChange.Number(CharacterNumberField.INITIATIVE_MISC_BONUS, newInitBonus))
                },
                label = stringResource(R.string.char_initiative_bonus),
                modifier = Modifier.weight(1f)
            )
        }
    }
}