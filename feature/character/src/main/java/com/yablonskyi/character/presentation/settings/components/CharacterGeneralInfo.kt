package com.yablonskyi.character.presentation.settings.components

import com.yablonskyi.domain.character.*
import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.NoPhotography
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
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.window.core.layout.WindowSizeClass
import coil.compose.AsyncImage
import com.yablonskyi.character.presentation.common.UiUtils
import com.yablonskyi.model.character.Ability
import com.yablonskyi.model.character.Character
import com.yablonskyi.model.character.SpellLevel
import com.yablonskyi.ui.R
import com.yablonskyi.ui.theme.DnDSheetTheme
import com.yablonskyi.ui.utils.EnumDropdown
import com.yablonskyi.ui.utils.IntTextField
import com.yablonskyi.ui.utils.NumbersTextField
import kotlinx.coroutines.launch
import com.yablonskyi.character.presentation.settings.*

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
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            CharacterProfilePicture(
                currentImagePath = character.imagePath,
                onImagePicker = onImagePicker,
            )
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
            IntTextField(
                value = character.level,
                validate = { input -> (input.toIntOrNull() ?: 1) < 20 },
                onValueChange = { newLevel ->
                    onUpdate(CharacterChange.Number(CharacterNumberField.LEVEL, newLevel))
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
