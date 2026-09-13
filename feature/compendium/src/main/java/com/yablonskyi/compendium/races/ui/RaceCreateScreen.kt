package com.yablonskyi.compendium.races.ui

import androidx.compose.foundation.layout.consumeWindowInsets
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.window.core.layout.WindowSizeClass
import com.yablonskyi.compendium.races.utils.PreviewUtils
import com.yablonskyi.compendium.races.viewmodel.RaceFormIntent
import com.yablonskyi.compendium.races.viewmodel.RaceFormUiState
import com.yablonskyi.model.character.Ability
import com.yablonskyi.model.character.Skill
import com.yablonskyi.model.rulebook.RaceSize
import com.yablonskyi.ui.R
import com.yablonskyi.ui.theme.Dimens
import com.yablonskyi.ui.utils.DnDSheetOutlinedTextField
import com.yablonskyi.ui.utils.EnumDropdown
import com.yablonskyi.ui.utils.PreviewThemeWrapper
import com.yablonskyi.ui.utils.SaveButton
import com.yablonskyi.ui.utils.bringIntoViewOnFocus

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RaceCreateScreen(
    uiState: RaceFormUiState,
    onIntent: (RaceFormIntent) -> Unit,
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
                        else stringResource(R.string.edit_race),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        textAlign = if (isWideScreen) TextAlign.Center else TextAlign.Left,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onIntent(RaceFormIntent.NavigateBack) }) {
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
                        onClick = { onIntent(RaceFormIntent.Submit) }
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
                        onValueChange = { onIntent(RaceFormIntent.NameChanged(it)) },
                        label = stringResource(R.string.race_name),
                        onFocusChanged = { onIntent(RaceFormIntent.NameFocusChanged(it)) },
                        isRequired = true
                    )
                }
                // Size & Speed
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing.Small),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RaceSizeDropdown(
                            label = stringResource(R.string.race_size),
                            selected = uiState.size.text,
                            onSelected = { onIntent(RaceFormIntent.SizeChanged(it)) },
                            modifier = Modifier.weight(1f)
                        )
                        DnDSheetOutlinedTextField(
                            fieldState = uiState.speedField,
                            onValueChange = { onIntent(RaceFormIntent.SpeedChanged(it)) },
                            label = "${stringResource(R.string.race_speed)} (${stringResource(R.string.feets)})",
                            errorText = uiState.speedField.error?.let {
                                stringResource(
                                    it,
                                    uiState.speedField.maxValue,
                                    uiState.speedField.minValue
                                )
                            },
                            onFocusChanged = { onIntent(RaceFormIntent.SpeedFocusChanged(it)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Traits
                item {
                    DnDSheetOutlinedTextField(
                        fieldState = uiState.traits,
                        onValueChange = { onIntent(RaceFormIntent.TraitsChanged(it)) },
                        label = stringResource(R.string.traits),
                        onFocusChanged = { onIntent(RaceFormIntent.TraitsFocusChanged(it)) },
                        maxLines = 2,
                        isRequired = true
                    )
                }

                // Description
                item {
                    DnDSheetOutlinedTextField(
                        fieldState = uiState.description,
                        onValueChange = { onIntent(RaceFormIntent.DescriptionChanged(it)) },
                        label = stringResource(R.string.description),
                        onFocusChanged = { onIntent(RaceFormIntent.DescriptionFocusChanged(it)) },
                        minLines = 3,
                        maxLines = 6,
                        isRequired = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            capitalization = KeyboardCapitalization.Sentences
                        ),
                        modifier = Modifier.bringIntoViewOnFocus()
                    )
                }

                // Ability Bonuses
                item {
                    Text(
                        text = stringResource(R.string.ability_score_bonuses),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = Dimens.Spacing.Small)
                    )
                }
                uiState.abilityBonuses.forEach { (ability, bonus) ->
                    item {
                        ActiveAbilityBonusRow(
                            ability = ability,
                            currentBonus = bonus,
                            onValueChange = {
                                onIntent(RaceFormIntent.AbilityBonusValueChanged(ability, it))
                            },
                            onRemove = { onIntent(RaceFormIntent.RemoveAbilityBonus(ability)) }
                        )
                    }
                }
                item {
                    AddAbilityBonusSelector(
                        availableChoices = uiState.availableAbilities,
                        onAbilitySelected = { onIntent(RaceFormIntent.AddAbilityBonus(it)) }
                    )
                }

                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = Dimens.Spacing.Small))
                }

                item {
                    GrantedSkillsSection(
                        grantedSkills = uiState.grantedSkills,
                        availableSkills = uiState.availableSkills,
                        onAddSkill = { onIntent(RaceFormIntent.AddGrantedSkill(it)) },
                        onRemoveSkill = { onIntent(RaceFormIntent.RemoveGrantedSkill(it)) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveAbilityBonusRow(
    ability: Ability,
    currentBonus: Int,
    onValueChange: (Int) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bonusValues = listOf(1, 2)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = Dimens.Spacing.XSmall)
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = Dimens.Spacing.Medium,
                vertical = Dimens.Spacing.Small
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing.Small)
        ) {
            Text(
                text = stringResource(ability.nameRes).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            SingleChoiceSegmentedButtonRow(modifier = Modifier.weight(0.6f)) {
                bonusValues.forEachIndexed { index, value ->
                    SegmentedButton(
                        selected = currentBonus == value,
                        onClick = { onValueChange(value) },
                        shape = SegmentedButtonDefaults.itemShape(index, bonusValues.size),
                        label = { Text("+$value") },
                        icon = {}
                    )
                }
            }
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = stringResource(R.string.remove),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun RaceSizeDropdown(
    label: String,
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    EnumDropdown(
        value = RaceSize.fromString(selected),
        label = label,
        options = RaceSize.entries,
        nameMapper = { stringResource(it.resId) },
        onSelected = { onSelected(it.name) },
        modifier = modifier
    )
}

@Composable
fun AddAbilityBonusSelector(
    availableChoices: List<Ability>,
    onAbilitySelected: (Ability) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = availableChoices.isNotEmpty(),
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Column(modifier = modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.add_ability_bonus),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    vertical = Dimens.Spacing.Small,
                )
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing.Small),
            ) {
                availableChoices.forEach { ability ->
                    SuggestionChip(
                        onClick = { onAbilitySelected(ability) },
                        label = { Text(stringResource(ability.nameRes)) },
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

@Composable
fun GrantedSkillsSection(
    grantedSkills: List<Skill>,
    availableSkills: List<Skill>,
    onAddSkill: (Skill) -> Unit,
    onRemoveSkill: (Skill) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.granted_skills),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = Dimens.Spacing.Small)
        )

        AnimatedVisibility(visible = grantedSkills.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing.Small),
                modifier = Modifier.padding(bottom = Dimens.Spacing.Small)
            ) {
                grantedSkills.forEach { skill ->
                    ActiveGrantedSkillChip(
                        skill = skill,
                        onRemove = { onRemoveSkill(skill) }
                    )
                }
            }
        }

        AddSkillSelector(
            availableSkills = availableSkills,
            onSkillSelected = onAddSkill
        )
    }
}

@Composable
fun ActiveGrantedSkillChip(
    skill: Skill,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    InputChip(
        selected = true,
        onClick = onRemove,
        label = { Text(stringResource(skill.nameRes)) },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.remove),
                modifier = Modifier.size(InputChipDefaults.AvatarSize)
            )
        },
        modifier = modifier
    )
}

@Composable
fun AddSkillSelector(
    availableSkills: List<Skill>,
    onSkillSelected: (Skill) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = availableSkills.isNotEmpty(),
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        Column {
            Text(
                text = stringResource(R.string.add_skill),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = Dimens.Spacing.Small)
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing.Small),
            ) {
                availableSkills.forEach { skill ->
                    SuggestionChip(
                        onClick = { onSkillSelected(skill) },
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

@Preview
@Composable
private fun ActiveAbilityBonusRowPreview() {
    PreviewThemeWrapper.Preview {
        ActiveAbilityBonusRow(
            ability = Ability.CHA,
            onValueChange = {},
            onRemove = {},
            currentBonus = 1,
        )
    }
}

@Preview
@Composable
private fun RaceCreateScreenPreview() {
    PreviewThemeWrapper.Preview {
        RaceCreateScreen(
            uiState = PreviewUtils.StateProvider.default,
            onIntent = {}
        )
    }
}