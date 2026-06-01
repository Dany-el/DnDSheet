package com.yablonskyi.dndsheet.ui.compendium.races

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.window.core.layout.WindowSizeClass
import com.yablonskyi.dndsheet.R
import com.yablonskyi.dndsheet.data.model.character.Ability
import com.yablonskyi.dndsheet.data.model.character.Skill
import com.yablonskyi.dndsheet.data.model.rulebook.Race
import com.yablonskyi.dndsheet.ui.theme.DnDSheetTheme
import com.yablonskyi.dndsheet.ui.utils.IntTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RaceCreateScreen(
    race: Race?,
    onCreate: (RaceFormUiState) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    formViewModel: RaceFormViewModel = viewModel(),
) {
    val uiState by formViewModel.uiState.collectAsStateWithLifecycle()
    val availableAbilities by formViewModel.availableAbilities.collectAsStateWithLifecycle()
    val availableSkills by formViewModel.availableSkills.collectAsStateWithLifecycle()

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val isWideScreen =
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

    LaunchedEffect(race) {
        if (race != null) {
            formViewModel.initializeWithRace(race)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (race == null) stringResource(R.string.creating)
                        else stringResource(R.string.edit_race),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        textAlign = if (isWideScreen) TextAlign.Center else TextAlign.Left,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (uiState.isFormValid) {
                        onCreate(uiState)
                    }
                },
                containerColor = if (uiState.isFormValid) FloatingActionButtonDefaults.containerColor else Color.Gray,
            ) {
                Icon(Icons.Default.Save, contentDescription = "Save")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 8.dp,
                end = 8.dp,
                top = 12.dp,
                bottom = innerPadding.calculateBottomPadding() + 88.dp
            ),
            modifier = Modifier
                .padding(innerPadding)
        ) {
            // Name
            item {
                DnDSheetOutlinedTextField(
                    fieldState = uiState.nameField,
                    onValueChange = formViewModel::onNameChanged,
                    label = stringResource(R.string.race_name),
                    onFocusChanged = formViewModel::onNameFocusChanged,
                    isRequired = true
                )
            }
            // Size & Speed
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DnDSheetOutlinedTextField(
                        fieldState = uiState.sizeField,
                        onValueChange = formViewModel::onSizeChanged,
                        label = stringResource(R.string.race_size),
                        onFocusChanged = formViewModel::onSizeFocusChanged,
                        modifier = Modifier.weight(1f),
                        isRequired = true
                    )
                    DnDSheetOutlinedTextField(
                        fieldState = uiState.speedField,
                        onValueChange = formViewModel::onSpeedChanged,
                        label = "${stringResource(R.string.race_speed)} (${stringResource(R.string.feets)})",
                        onFocusChanged = formViewModel::onSpeedFocusChanged,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Traits
            item {
                DnDSheetOutlinedTextField(
                    fieldState = uiState.traitsField,
                    onValueChange = formViewModel::onTraitsChanged,
                    label = stringResource(R.string.traits),
                    onFocusChanged = formViewModel::onTraitsFocusChanged,
                    maxLines = 2,
                    isRequired = true
                )
            }

            // Description
            item {
                DnDSheetOutlinedTextField(
                    fieldState = uiState.descriptionField,
                    onValueChange = formViewModel::onDescriptionChanged,
                    label = stringResource(R.string.description),
                    onFocusChanged = formViewModel::onDescriptionFocusChanged,
                    maxLines = 3,
                    isRequired = true
                )
            }

            // Ability Bonuses
            item {
                Text(
                    text = stringResource(R.string.ability_score_bonuses),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            uiState.abilityBonuses.forEach { (ability, bonus) ->
                item {
                    ActiveAbilityBonusRow(
                        ability = ability,
                        currentBonus = bonus,
                        onValueChange = { formViewModel.updateAbilityValue(ability, it) },
                        onRemove = { formViewModel.removeAbilityBonus(ability) }
                    )
                }
            }
            item {
                AddAbilityBonusSelector(
                    availableChoices = availableAbilities,
                    onAbilitySelected = formViewModel::addAbilityBonus
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            item {
                GrantedSkillsSection(
                    grantedSkills = uiState.grantedSkills,
                    availableSkills = availableSkills,
                    onAddSkill = formViewModel::addGrantedSkill,
                    onRemoveSkill = formViewModel::removeGrantedSkill
                )
            }
        }
    }
}

@Composable
fun DnDSheetOutlinedTextField(
    fieldState: FieldState,
    onValueChange: (String) -> Unit,
    label: String,
    onFocusChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.Next,
        capitalization = KeyboardCapitalization.Sentences
    ),
    keyboardActions: KeyboardActions = KeyboardActions(),
    maxLines: Int = 1,
    isRequired: Boolean = false,
) {
    OutlinedTextField(
        value = fieldState.text,
        onValueChange = onValueChange,
        label = {
            Text(text = "$label${if (isRequired) "*" else ""}")
        },
        isError = fieldState.error != null,
        supportingText = {
            fieldState.error?.let {
                Text(
                    text = stringResource(it),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        keyboardActions = keyboardActions,
        keyboardOptions = keyboardOptions,
        maxLines = maxLines,
        minLines = maxLines,
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                onFocusChanged(focusState.isFocused)
            }
    )
}

@Composable
fun DnDSheetOutlinedTextField(
    fieldState: IntFieldState,
    onValueChange: (Int) -> Unit,
    label: String,
    onFocusChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    IntTextField(
        value = fieldState.value,
        onValueChange = onValueChange,
        label = label,
        isError = fieldState.error != null,
        enableSupportingText = true,
        errorText = if (fieldState.error != null) {
            stringResource(fieldState.error, fieldState.maxValue, fieldState.minValue )
        } else "",
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                onFocusChanged(focusState.isFocused)
            }
    )
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
            .padding(bottom = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
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
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
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
            modifier = Modifier.padding(vertical = 8.dp)
        )

        AnimatedVisibility(visible = grantedSkills.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
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
                modifier = Modifier.padding(bottom = 4.dp)
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
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
    DnDSheetTheme {
        ActiveAbilityBonusRow(
            ability = Ability.CHA,
            onValueChange = {},
            onRemove = {},
            currentBonus = 1,
        )
    }
}