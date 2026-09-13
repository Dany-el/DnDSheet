package com.yablonskyi.compendium.classes.ui

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.window.core.layout.WindowSizeClass
import com.yablonskyi.compendium.classes.utils.PreviewUtils
import com.yablonskyi.compendium.classes.viewmodel.ClassFormIntent
import com.yablonskyi.compendium.classes.viewmodel.ClassFormUiState
import com.yablonskyi.model.character.Ability
import com.yablonskyi.model.character.Skill
import com.yablonskyi.model.dice.DiceRoles
import com.yablonskyi.ui.R
import com.yablonskyi.ui.theme.Dimens
import com.yablonskyi.ui.utils.DnDSheetOutlinedTextField
import com.yablonskyi.ui.utils.EnumDropdown
import com.yablonskyi.ui.utils.PreviewThemeWrapper
import com.yablonskyi.ui.utils.SaveButton
import com.yablonskyi.ui.utils.bringIntoViewOnFocus

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ClassCreateScreen(
    uiState: ClassFormUiState,
    onIntent: (ClassFormIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val isWideScreen =
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.id.isBlank()) stringResource(R.string.creating)
                        else stringResource(R.string.edit_class),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        textAlign = if (isWideScreen) TextAlign.Center else TextAlign.Left,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onIntent(ClassFormIntent.NavigateBack) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                        Dimens.TopBar.Elevation
                    ),
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    SaveButton(
                        isEnabled = uiState.isFormValid,
                        contentDescription = stringResource(R.string.save),
                        onClick = { onIntent(ClassFormIntent.Submit) }
                    )
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = Dimens.Spacing.Small,
                    end = Dimens.Spacing.Small,
                    top = Dimens.Spacing.Medium,
                    bottom = Dimens.Spacing.Large
                ),
                modifier = Modifier.widthIn(max = Dimens.Content.MaxWidth)
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .imePadding()
            ) {
                // Name
                item {
                    DnDSheetOutlinedTextField(
                        fieldState = uiState.name,
                        onValueChange = { onIntent(ClassFormIntent.NameChanged(it)) },
                        label = stringResource(R.string.class_name),
                        onFocusChanged = { onIntent(ClassFormIntent.NameFocusChanged(it)) },
                        isRequired = true
                    )
                }

                // Hit Dice / Skill Choice Count
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing.Small),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        HitDiceDropdown(
                            label = stringResource(R.string.hit_dice),
                            selected = uiState.hitDice,
                            onSelected = { onIntent(ClassFormIntent.HitDiceChanged(it)) },
                            choices = DiceRoles.hitDices,
                            modifier = Modifier.weight(1f)
                        )
                        DnDSheetOutlinedTextField(
                            fieldState = uiState.skillChoiceCountField,
                            onValueChange = { onIntent(ClassFormIntent.SkillChoiceCountChanged(it)) },
                            label = stringResource(R.string.skill_choices),
                            errorText = uiState.skillChoiceCountField.error?.let {
                                stringResource(
                                    it,
                                    uiState.skillChoiceCountField.maxValue,
                                    uiState.skillChoiceCountField.minValue
                                )
                            },
                            onFocusChanged = { onIntent(ClassFormIntent.SkillChoiceCountFocusChanged(it)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Primary / Spellcasting Ability
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing.Small),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = Dimens.Spacing.Large)
                    ) {
                        AttackAbilityDropdown(
                            label = stringResource(R.string.primary_ability),
                            selected = uiState.primaryAbility,
                            choices = Ability.playableAbilities,
                            onSelected = { onIntent(ClassFormIntent.PrimaryAbilityChanged(it)) },
                            modifier = Modifier.weight(1f)
                        )
                        SpellcastingAbilityDropdown(
                            label = stringResource(R.string.spellcasting_ability_shorted),
                            selected = uiState.spellcastingAbility,
                            choices = Ability.playableAbilities,
                            onSelected = { onIntent(ClassFormIntent.SpellcastingAbilityChanged(it)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Description
                item {
                    DnDSheetOutlinedTextField(
                        fieldState = uiState.description,
                        onValueChange = { onIntent(ClassFormIntent.DescriptionChanged(it)) },
                        label = stringResource(R.string.description),
                        onFocusChanged = { onIntent(ClassFormIntent.DescriptionFocusChanged(it)) },
                        minLines = 3,
                        maxLines = 6,
                        isRequired = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Default
                        ),
                        modifier = Modifier.bringIntoViewOnFocus()
                    )
                }

                // Saving Throws
                item {
                    SavingThrowsSection(
                        selectedThrows = uiState.savingThrows,
                        availableThrows = uiState.availableSavingThrows,
                        onAdd = { onIntent(ClassFormIntent.AddSavingThrow(it)) },
                        onRemove = { onIntent(ClassFormIntent.RemoveSavingThrow(it)) }
                    )
                }

                // Available Skills
                item {
                    Spacer(Modifier.height(Dimens.Spacing.Large))
                    AvailableSkillsSection(
                        selectedSkills = uiState.chosenSkills,
                        availableSkills = uiState.availableSkills,
                        onAddSkill = { onIntent(ClassFormIntent.AddSkill(it)) },
                        onRemoveSkill = { onIntent(ClassFormIntent.RemoveSkill(it)) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HitDiceDropdown(
    label: String,
    selected: String,
    choices: List<String>,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    EnumDropdown(
        value = selected,
        label = label,
        options = choices,
        nameMapper = { it },
        onSelected = { onSelected(it) },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttackAbilityDropdown(
    label: String,
    selected: Ability,
    choices: List<Ability>,
    onSelected: (Ability) -> Unit,
    modifier: Modifier = Modifier
) {
    EnumDropdown(
        value = selected,
        label = label,
        options = choices,
        nameMapper = { stringResource(it.nameRes) },
        onSelected = { onSelected(it) },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpellcastingAbilityDropdown(
    label: String,
    selected: Ability?,
    choices: List<Ability>,
    onSelected: (Ability?) -> Unit,
    modifier: Modifier = Modifier
) {
    EnumDropdown(
        value = selected,
        label = label,
        options = choices + null,
        nameMapper = { it?.let { stringResource(it.nameRes) } ?: stringResource(R.string.none) },
        onSelected = { onSelected(it) },
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun SavingThrowsSection(
    selectedThrows: Set<Ability>,
    availableThrows: List<Ability>,
    onAdd: (Ability) -> Unit,
    onRemove: (Ability) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.saving_throws),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = Dimens.Spacing.Small)
        )

        AnimatedVisibility(visible = selectedThrows.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing.Small),
                modifier = Modifier.padding(bottom = Dimens.Spacing.Small)
            ) {
                selectedThrows.forEach { ability ->
                    InputChip(
                        selected = true,
                        onClick = { onRemove(ability) },
                        label = {
                            Text(stringResource(ability.nameRes).take(3).uppercase())
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.remove),
                                modifier = Modifier.size(InputChipDefaults.AvatarSize)
                            )
                        }
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = availableThrows.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column {
                Text(
                    text = stringResource(R.string.add_saving_throw),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = Dimens.Spacing.XSmall)
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing.Small)) {
                    availableThrows.forEach { ability ->
                        SuggestionChip(
                            onClick = { onAdd(ability) },
                            label = { Text(stringResource(ability.nameRes).take(3).uppercase()) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(SuggestionChipDefaults.IconSize)
                                )
                            }
                        )
                    }
                }
            }
        }
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.padding(top = Dimens.Spacing.XSmall)
        )
    }
}

@Composable
fun AvailableSkillsSection(
    selectedSkills: List<Skill>,
    availableSkills: List<Skill>,
    onAddSkill: (Skill) -> Unit,
    onRemoveSkill: (Skill) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.available_skills),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = Dimens.Spacing.Small)
        )

        AnimatedVisibility(visible = selectedSkills.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing.Small),
                modifier = Modifier.padding(bottom = Dimens.Spacing.Small)
            ) {
                selectedSkills.forEach { skill ->
                    InputChip(
                        selected = true,
                        onClick = { onRemoveSkill(skill) },
                        label = { Text(stringResource(skill.nameRes)) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.remove),
                                modifier = Modifier.size(InputChipDefaults.AvatarSize)
                            )
                        }
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = availableSkills.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column {
                Text(
                    text = stringResource(R.string.add_skill),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = Dimens.Spacing.XSmall)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing.Small),
                ) {
                    availableSkills.forEach { skill ->
                        SuggestionChip(
                            onClick = { onAddSkill(skill) },
                            label = { Text(stringResource(skill.nameRes)) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(SuggestionChipDefaults.IconSize)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun ClassCreateScreenPreview() {
    PreviewThemeWrapper.Preview {
        ClassCreateScreen(
            uiState = PreviewUtils.StateProvider.default,
            onIntent = {}
        )
    }
}