package com.yablonskyi.compendium.spells.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.window.core.layout.WindowSizeClass
import com.yablonskyi.compendium.spells.utils.PreviewUtils
import com.yablonskyi.compendium.spells.viewmodel.SpellFormIntent
import com.yablonskyi.compendium.spells.viewmodel.SpellFormUiState
import com.yablonskyi.model.character.Ability
import com.yablonskyi.model.character.AttackType
import com.yablonskyi.model.character.Component
import com.yablonskyi.model.character.DamageType
import com.yablonskyi.model.character.MagicSchool
import com.yablonskyi.model.character.SpellCastTime
import com.yablonskyi.model.character.SpellDuration
import com.yablonskyi.model.character.SpellLevel
import com.yablonskyi.model.character.SpellRangeType
import com.yablonskyi.ui.R
import com.yablonskyi.ui.theme.Dimens
import com.yablonskyi.ui.utils.DnDSheetOutlinedTextField
import com.yablonskyi.ui.utils.EnumDropdown
import com.yablonskyi.ui.utils.PreviewThemeWrapper
import com.yablonskyi.ui.utils.SaveButton
import com.yablonskyi.ui.utils.bringIntoViewOnFocus
import com.yablonskyi.ui.validation.FieldState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SpellCreateScreen(
    uiState: SpellFormUiState,
    onIntent: (SpellFormIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val isWideScreen =
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                ),
                navigationIcon = {
                    IconButton(onClick = { onIntent(SpellFormIntent.NavigateBack) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                title = {
                    Text(
                        text = stringResource(if (uiState.id == 0L) R.string.add_spell else R.string.edit_spell),
                        fontWeight = FontWeight.SemiBold,
                        textAlign = if (isWideScreen) null else TextAlign.Left,
                        modifier = if (isWideScreen) Modifier else Modifier.fillMaxWidth(),
                    )
                },
                actions = {
                    SaveButton(
                        isEnabled = uiState.isFormValid,
                        contentDescription = stringResource(R.string.save_spell),
                        onClick = { onIntent(SpellFormIntent.Submit) }
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
                contentPadding = PaddingValues(Dimens.Spacing.Small),
                verticalArrangement = Arrangement.spacedBy(Dimens.Spacing.Large),
                modifier = Modifier.widthIn(max = Dimens.Content.MaxWidth),
            ) {
                item {
                    OutlinedCard {
                        Column(
                            Modifier.padding(Dimens.Spacing.Small)
                        ) {
                            Spacer(Modifier.height(Dimens.Spacing.Small))

                            DnDSheetOutlinedTextField(
                                fieldState = uiState.name,
                                onValueChange = { onIntent(SpellFormIntent.NameChanged(it)) },
                                label = stringResource(R.string.spell_name),
                                onFocusChanged = { onIntent(SpellFormIntent.NameFocusChanged(it)) },
                                isRequired = true
                            )

                            // Basic info
                            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing.Small)) {
                                EnumDropdown(
                                    value = uiState.level,
                                    labelRes = R.string.spell_level,
                                    options = SpellLevel.entries,
                                    nameMapper = { stringResource(it.resId) },
                                    onSelected = { onIntent(SpellFormIntent.LevelChanged(it)) },
                                    modifier = Modifier.weight(1f)
                                )

                                EnumDropdown(
                                    value = uiState.school,
                                    labelRes = R.string.msg_school,
                                    options = MagicSchool.entries,
                                    nameMapper = { stringResource(it.resId) },
                                    onSelected = { onIntent(SpellFormIntent.SchoolChanged(it)) },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Casting
                            EnumDropdown(
                                value = uiState.castTime,
                                labelRes = R.string.msg_casting_time,
                                options = SpellCastTime.entries,
                                nameMapper = { stringResource(it.resId) },
                                onSelected = { onIntent(SpellFormIntent.CastTimeChanged(it)) },
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing.Small)) {
                                EnumDropdown(
                                    value = uiState.rangeType,
                                    labelRes = R.string.range_distance,
                                    options = SpellRangeType.entries,
                                    nameMapper = { stringResource(it.resId) },
                                    onSelected = { onIntent(SpellFormIntent.RangeTypeChanged(it)) },
                                    modifier = Modifier.weight(1f)
                                )
                                if (uiState.rangeType == SpellRangeType.DISTANCE) {
                                    DnDSheetOutlinedTextField(
                                        fieldState = uiState.rangeValueField,
                                        onValueChange = {
                                            onIntent(
                                                SpellFormIntent.RangeValueChanged(
                                                    it
                                                )
                                            )
                                        },
                                        errorText = uiState.rangeValueField.error?.let {
                                            stringResource(
                                                it,
                                                uiState.rangeValueField.maxValue,
                                                uiState.rangeValueField.minValue
                                            )
                                        },
                                        label = stringResource(R.string.feets),
                                        onFocusChanged = {
                                            onIntent(SpellFormIntent.RangeValueFocusChanged(it))
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    OutlinedCard {
                        Column(
                            Modifier.padding(Dimens.Spacing.Small)
                        ) {
                            Spacer(Modifier.height(Dimens.Spacing.Small))

                            ComponentsSection(
                                selected = uiState.components,
                                onToggle = { onIntent(SpellFormIntent.ComponentToggled(it)) }
                            )

                            if (Component.MATERIAL in uiState.components) {
                                DnDSheetOutlinedTextField(
                                    fieldState = uiState.material,
                                    onValueChange = { onIntent(SpellFormIntent.MaterialChanged(it)) },
                                    label = stringResource(R.string.spell_material_component),
                                    onFocusChanged = {
                                        onIntent(
                                            SpellFormIntent.MaterialFocusChanged(
                                                it
                                            )
                                        )
                                    },
                                    isRequired = true,
                                    minLines = 2,
                                    maxLines = 6,
                                    keyboardOptions = KeyboardOptions(
                                        capitalization = KeyboardCapitalization.Sentences,
                                        imeAction = ImeAction.Default
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .bringIntoViewOnFocus()
                                )
                            }

                            DurationAndFlagsSection(
                                duration = uiState.duration,
                                isRitual = uiState.isRitual,
                                isConcentration = uiState.isConcentration,
                                onDurationChanged = { onIntent(SpellFormIntent.DurationChanged(it)) },
                                onRitualToggle = { onIntent(SpellFormIntent.RitualToggled) },
                                onConcentrationToggle = { onIntent(SpellFormIntent.ConcentrationToggled) }
                            )
                        }
                    }
                }

                item {
                    OutlinedCard {
                        Column(
                            Modifier.padding(Dimens.Spacing.Small)
                        ) {
                            Spacer(Modifier.height(Dimens.Spacing.Small))

                            CombatStatsSection(
                                attackType = uiState.attackType,
                                saveStat = uiState.saveStat,
                                damageType = uiState.damageType,
                                damageDiceField = uiState.damageDice,
                                onAttackTypeChanged = {
                                    onIntent(
                                        SpellFormIntent.AttackTypeChanged(
                                            it
                                        )
                                    )
                                },
                                onSaveStatChanged = { onIntent(SpellFormIntent.SaveStatChanged(it)) },
                                onDamageTypeChanged = {
                                    onIntent(
                                        SpellFormIntent.DamageTypeChanged(
                                            it
                                        )
                                    )
                                },
                                onDamageDiceChanged = {
                                    onIntent(
                                        SpellFormIntent.DamageDiceChanged(
                                            it
                                        )
                                    )
                                }
                            )
                        }
                    }
                }

                item {
                    OutlinedCard {
                        Column(
                            Modifier.padding(Dimens.Spacing.Small)
                        ) {
                            Spacer(Modifier.height(Dimens.Spacing.Small))

                            DnDSheetOutlinedTextField(
                                fieldState = uiState.description,
                                onValueChange = { onIntent(SpellFormIntent.DescriptionChanged(it)) },
                                label = stringResource(R.string.spell_description),
                                onFocusChanged = {
                                    onIntent(
                                        SpellFormIntent.DescriptionFocusChanged(
                                            it
                                        )
                                    )
                                },
                                isRequired = true,
                                minLines = 3,
                                maxLines = 6,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Sentences,
                                    imeAction = ImeAction.Default
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .bringIntoViewOnFocus()
                            )

                            DnDSheetOutlinedTextField(
                                fieldState = uiState.higherLevels,
                                onValueChange = { onIntent(SpellFormIntent.HigherLevelsChanged(it)) },
                                label = stringResource(R.string.spell_higher_levels),
                                onFocusChanged = {
                                    onIntent(SpellFormIntent.HigherLevelsFocusChanged(it))
                                },
                                minLines = 2,
                                maxLines = 4,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Sentences,
                                    imeAction = ImeAction.Default
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .bringIntoViewOnFocus()
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(Dimens.Fab.Clearance))
                }
            }
        }
    }
}

@Composable
private fun ComponentsSection(
    selected: Set<Component>,
    onToggle: (Component) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.msg_components),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = Dimens.Spacing.XSmall)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing.Small),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(Component.entries) { component ->
                FilterChip(
                    selected = component in selected,
                    onClick = { onToggle(component) },
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
}

@Composable
private fun DurationAndFlagsSection(
    duration: SpellDuration,
    isRitual: Boolean,
    isConcentration: Boolean,
    onDurationChanged: (SpellDuration) -> Unit,
    onRitualToggle: () -> Unit,
    onConcentrationToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
//        verticalArrangement = Arrangement.spacedBy(Dimens.Spacing.Small)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            EnumDropdown(
                value = duration,
                labelRes = R.string.msg_duration,
                options = SpellDuration.entries,
                nameMapper = { stringResource(it.resId) },
                onSelected = onDurationChanged,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing.Large)) {
            FlagCheckbox(
                labelRes = R.string.ritual,
                checked = isRitual,
                onToggle = onRitualToggle
            )
            FlagCheckbox(
                labelRes = R.string.concentration,
                checked = isConcentration,
                onToggle = onConcentrationToggle
            )
        }
    }
}

@Composable
private fun FlagCheckbox(
    labelRes: Int,
    checked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onToggle
        )
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { onToggle() }
        )
        Text(stringResource(labelRes))
    }
}

@Composable
private fun CombatStatsSection(
    attackType: AttackType,
    saveStat: Ability?,
    damageType: DamageType?,
    damageDiceField: FieldState,
    onAttackTypeChanged: (AttackType) -> Unit,
    onSaveStatChanged: (Ability?) -> Unit,
    onDamageTypeChanged: (DamageType?) -> Unit,
    onDamageDiceChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
//        verticalArrangement = Arrangement.spacedBy(Dimens.Spacing.Small)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing.Small)) {
            EnumDropdown(
                value = attackType,
                labelRes = R.string.spell_attack_type,
                options = AttackType.entries,
                nameMapper = { stringResource(it.resId) },
                onSelected = onAttackTypeChanged,
                modifier = Modifier.weight(1f)
            )
            if (attackType == AttackType.SAVE) {
                EnumDropdown(
                    value = saveStat,
                    labelRes = R.string.spell_save_stat,
                    options = Ability.playableAbilities,
                    nameMapper = {
                        it?.let { stringResource(it.nameRes) } ?: ""
                    },
                    isRequired = true,
                    onSelected = onSaveStatChanged,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing.Small)) {
            EnumDropdown(
                value = damageType,
                labelRes = R.string.spell_damage_type,
                options = DamageType.entries + null,
                nameMapper = {
                    it?.let { stringResource(it.resId) }
                        ?: stringResource(R.string.none)
                },
                onSelected = onDamageTypeChanged,
                modifier = Modifier.weight(1f)
            )
            if (damageType != null) {
                OutlinedTextField(
                    value = damageDiceField.text,
                    onValueChange = onDamageDiceChanged,
                    label = { Text(stringResource(R.string.spell_damage_dice)) },
                    placeholder = { Text((stringResource(R.string.placeholder_dice))) },
                    isError = damageDiceField.error != null,
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Preview(locale = "uk")
@Composable
private fun SpellCreateScreenPreview() {
    PreviewThemeWrapper.Preview {
        SpellCreateScreen(
            uiState = PreviewUtils.spellFormState,
            onIntent = {}
        )
    }
}

@Preview(locale = "uk", device = "spec:width=1280dp,height=800dp,dpi=240")
@Composable
private fun SpellCreateScreenPreview_Tablet() {
    PreviewThemeWrapper.Preview {
        SpellCreateScreen(
            uiState = PreviewUtils.spellFormState,
            onIntent = {}
        )
    }
}