package com.yablonskyi.dndsheet.ui.compendium.classes

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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.window.core.layout.WindowSizeClass
import com.yablonskyi.dndsheet.R
import com.yablonskyi.dndsheet.data.model.character.Ability
import com.yablonskyi.dndsheet.data.model.character.Skill
import com.yablonskyi.dndsheet.data.model.dice.DiceRoles
import com.yablonskyi.dndsheet.data.model.rulebook.CharacterClass
import com.yablonskyi.dndsheet.ui.compendium.races.DnDSheetOutlinedTextField
import com.yablonskyi.dndsheet.ui.theme.DnDSheetTheme
import com.yablonskyi.dndsheet.ui.utils.EnumDropdown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassCreateScreen(
    characterClass: CharacterClass?,
    onCreate: (ClassFormUiState) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    formViewModel: ClassFormViewModel = viewModel(),
) {
    val uiState by formViewModel.uiState.collectAsStateWithLifecycle()
    val availableSavingThrows by formViewModel.availableSavingThrows.collectAsStateWithLifecycle()
    val availableSkills by formViewModel.availableSkills.collectAsStateWithLifecycle()

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val isWideScreen =
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

    LaunchedEffect(characterClass) {
        if (characterClass != null) {
            formViewModel.initializeWithClass(characterClass)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (characterClass == null) stringResource(R.string.creating)
                        else stringResource(R.string.edit_class),
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
                            contentDescription = stringResource(R.string.back)
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
                onClick = { if (uiState.isFormValid) onCreate(uiState) },
                containerColor = if (uiState.isFormValid) FloatingActionButtonDefaults.containerColor
                else Color.Gray,
            ) {
                Icon(Icons.Default.Save, contentDescription = stringResource(R.string.save))
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
            modifier = Modifier.padding(innerPadding)
        ) {
            // Name
            item {
                DnDSheetOutlinedTextField(
                    fieldState = uiState.nameField,
                    onValueChange = formViewModel::onNameChanged,
                    label = stringResource(R.string.class_name),
                    onFocusChanged = formViewModel::onNameFocusChanged,
                    isRequired = true
                )
            }

            // Hit Dice / Skill Choice Count
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HitDiceDropdown(
                        label = stringResource(R.string.hit_dice),
                        selected = uiState.hitDiceField.text,
                        onSelected = {
                            formViewModel.onHitDiceChanged(it)
                        },
                        choices = DiceRoles.hitDices,
                        modifier = Modifier.weight(1f)
                    )
                    DnDSheetOutlinedTextField(
                        fieldState = uiState.skillChoiceCountField,
                        onValueChange = formViewModel::onSkillChoiceCountChanged,
                        label = stringResource(R.string.skill_choices),
                        onFocusChanged = formViewModel::onSkillChoiceCountFocusChanged,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Primary / Spellcasting Ability
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    AttackAbilityDropdown(
                        label = stringResource(R.string.primary_ability),
                        selected = uiState.primaryAbility,
                        choices = Ability.playableAbilities,
                        onSelected = formViewModel::onPrimaryAbilityChanged,
                        modifier = Modifier.weight(1f)
                    )
                    SpellcastingAbilityDropdown(
                        label = stringResource(R.string.spellcasting_ability_shorted),
                        selected = uiState.spellcastingAbility,
                        choices = Ability.playableAbilities,
                        onSelected = formViewModel::onSpellcastingAbilityChanged,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Description
            item {
                DnDSheetOutlinedTextField(
                    fieldState = uiState.descriptionField,
                    onValueChange = formViewModel::onDescriptionChanged,
                    label = stringResource(R.string.description),
                    onFocusChanged = formViewModel::onDescriptionFocusChanged,
                    maxLines = 3,
                    isRequired = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Default
                    )
                )
            }

            // Saving Throws
            item {
                SavingThrowsSection(
                    selectedThrows = uiState.savingThrows,
                    availableThrows = availableSavingThrows,
                    onAdd = formViewModel::addSavingThrow,
                    onRemove = formViewModel::removeSavingThrow
                )
            }

            // Available Skills
            item {
                Spacer(Modifier.height(16.dp))
                AvailableSkillsSection(
                    selectedSkills = uiState.availableSkills,
                    availableSkills = availableSkills,
                    onAddSkill = formViewModel::addAvailableSkill,
                    onRemoveSkill = formViewModel::removeAvailableSkill
                )
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
            modifier = Modifier.padding(vertical = 8.dp)
        )

        AnimatedVisibility(visible = selectedThrows.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
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
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
            modifier = Modifier.padding(top = 4.dp)
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
            modifier = Modifier.padding(vertical = 8.dp)
        )

        AnimatedVisibility(visible = selectedSkills.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
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
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
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
    DnDSheetTheme() {
        ClassCreateScreen(
            characterClass = null,
            onCreate = {},
            onNavigateBack = {}
        )
    }
}