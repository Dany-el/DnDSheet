package com.yablonskyi.character.presentation.settings

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yablonskyi.character.presentation.settings.components.CharacterGeneralInfo
import com.yablonskyi.character.presentation.settings.components.SettingsNumberInput
import com.yablonskyi.domain.character.CharacterChange
import com.yablonskyi.domain.character.CharacterNumberField
import com.yablonskyi.model.character.Ability
import com.yablonskyi.ui.R
import com.yablonskyi.ui.components.SettingsComponent
import com.yablonskyi.ui.components.SimpleTopAppBar
import com.yablonskyi.ui.utils.DnDSheetOutlinedTextField
import com.yablonskyi.ui.utils.EnumDropdown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterSettingsScreen(
    form: CharacterSettingsFormUiState,
    onIntent: (CharacterSettingsIntent) -> Unit,
    onFormIntent: (CharacterSettingsFormIntent) -> Unit
) {
    val character = form.character ?: return
    val configuration = LocalConfiguration.current
    val wide =
        configuration.orientation == Configuration.ORIENTATION_LANDSCAPE && LocalWindowInfo.current.containerSize.width >= 600

    Scaffold(
        topBar = {
            SimpleTopAppBar(
                title = stringResource(R.string.settings),
                onNavigateBack = { onIntent(CharacterSettingsIntent.BackClicked) })
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .imePadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                Modifier.widthIn(max = 1000.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    CharacterGeneralInfo(
                        form,
                        onFormIntent,
                        onImagePicker = { onIntent(CharacterSettingsIntent.ImagePickerClicked) },
                        wide = wide
                    )
                }
                item {
                    SettingsComponent(title = stringResource(R.string.class_settings)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.jack_of_all_trades))
                            Switch(
                                character.hasJackOfAllTrades,
                                {
                                    onFormIntent(
                                        CharacterSettingsFormIntent.Change(
                                            CharacterChange.JackOfAllTrades(it)
                                        )
                                    )
                                })
                        }
                    }
                }
                item {
                    SettingsComponent(title = stringResource(R.string.spell_settings)) {
                        EnumDropdown(
                            character.spellSettings.spellCastingAbility,
                            R.string.spell_ability,
                            Ability.entries,
                            nameMapper = { it?.let { stringResource(it.nameRes) } ?: "" },
                            onSelected = {
                                onFormIntent(
                                    CharacterSettingsFormIntent.Change(
                                        CharacterChange.CastingAbility(it)
                                    )
                                )
                            })
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SettingsNumberInput(
                                form,
                                CharacterNumberField.DC_MISC_BONUS,
                                R.string.spell_saving_throw_bonus,
                                onFormIntent,
                                Modifier.weight(1f)
                            )
                            SettingsNumberInput(
                                form,
                                CharacterNumberField.ATTACK_MISC_BONUS,
                                R.string.spell_attack_bonus,
                                onFormIntent,
                                Modifier.weight(1f)
                            )
                        }
                        Text(
                            stringResource(R.string.spell_slots),
                            style = MaterialTheme.typography.labelMedium
                        )
                        form.slots.entries.toList().chunked(if (wide) 5 else 3).forEach { entries ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                entries.forEach { (level, field) ->
                                    DnDSheetOutlinedTextField(
                                        field,
                                        {
                                            onFormIntent(
                                                CharacterSettingsFormIntent.Change(
                                                    CharacterChange.SlotMaximum(
                                                        level,
                                                        it
                                                    )
                                                )
                                            )
                                        },
                                        stringResource(level.resId),
                                        {
                                            onFormIntent(
                                                CharacterSettingsFormIntent.SlotFocus(
                                                    level,
                                                    it
                                                )
                                            )
                                        },
                                        Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}