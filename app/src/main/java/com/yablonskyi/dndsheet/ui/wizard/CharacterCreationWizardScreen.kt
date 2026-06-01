package com.yablonskyi.dndsheet.ui.wizard

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yablonskyi.dndsheet.R
import com.yablonskyi.dndsheet.data.model.rulebook.CharacterClass
import com.yablonskyi.dndsheet.data.model.rulebook.Race
import com.yablonskyi.dndsheet.ui.theme.DnDSheetTheme

@Composable
fun CharacterCreationWizardScreen(
    viewModel: CharacterCreationWizardViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentStep by viewModel.step.collectAsStateWithLifecycle()
    val canProceed by viewModel.canProceed.collectAsStateWithLifecycle()

    val name by viewModel.name.collectAsStateWithLifecycle()
    val origRaces by viewModel.origRaces.collectAsStateWithLifecycle()
    val homebrewRaces by viewModel.homebrewRaces.collectAsStateWithLifecycle()
    val homebrewRaceQuery by viewModel.raceQuery.collectAsStateWithLifecycle()
    val origClasses by viewModel.origClasses.collectAsStateWithLifecycle()
    val homebrewClasses by viewModel.homebrewClasses.collectAsStateWithLifecycle()
    val homebrewClassQuery by viewModel.classQuery.collectAsStateWithLifecycle()
    val selectedRace by viewModel.selectedRace.collectAsStateWithLifecycle()
    val selectedClass by viewModel.selectedClass.collectAsStateWithLifecycle()
    val selectedSkills by viewModel.selectedSkills.collectAsStateWithLifecycle()
    val availableSkills by viewModel.availableSkills.collectAsStateWithLifecycle()
    val maxSkills by viewModel.maxSkills.collectAsStateWithLifecycle()
    val abilityMethod by viewModel.abilityMethod.collectAsStateWithLifecycle()
    val standardAssignments by viewModel.standardAssignments.collectAsStateWithLifecycle()
    val pendingPoolValue by viewModel.pendingPoolValue.collectAsStateWithLifecycle()
    val pointBuyScores by viewModel.pointBuyScores.collectAsStateWithLifecycle()
    val pointsSpent by viewModel.pointsSpent.collectAsStateWithLifecycle()
    val rolledResults by viewModel.rolledResults.collectAsStateWithLifecycle()
    val rollIndexAssignments by viewModel.rollIndexAssignments.collectAsStateWithLifecycle()
    val pendingRollIndex by viewModel.pendingRollIndex.collectAsStateWithLifecycle()
    val baseAbilityBlock by viewModel.baseAbilityBlock.collectAsStateWithLifecycle()
    val customLevelEnabled by viewModel.customLevelEnabled.collectAsStateWithLifecycle()
    val level by viewModel.level.collectAsStateWithLifecycle()
    val calculatedHp by viewModel.calculatedHp.collectAsStateWithLifecycle()

    BackHandler {
        if (!viewModel.goBack()) onNavigateBack()
    }

    Scaffold(
        topBar = {
            CreationWizardTopBar(
                step = currentStep,
                selectedRace = selectedRace,
                selectedClass = selectedClass,
                onNavigateBack = onNavigateBack,
            )
        },
        bottomBar = {
            CreationWizardBottomBar(
                step = currentStep,
                canProceed = canProceed,
                onBack = viewModel::goBack,
                onNext = viewModel::goNext,
                onFinish = viewModel::finish,
            )
        },
        modifier = modifier
    ) { contentPadding ->
        AnimatedContent(
            targetState = currentStep,
            modifier = Modifier.padding(contentPadding),
            transitionSpec = {
                val forward = targetState.ordinal > initialState.ordinal
                val enter = if (forward)
                    slideInHorizontally { it } + fadeIn()
                else
                    slideInHorizontally { -it } + fadeIn()
                val exit = if (forward)
                    slideOutHorizontally { -it } + fadeOut()
                else
                    slideOutHorizontally { it } + fadeOut()
                enter togetherWith exit
            },
        ) { step ->
            when (step) {
                WizardStep.NAME -> {
                    WizardNameStep(
                        name = name,
                        onNameChange = viewModel::setName
                    )
                }

                WizardStep.RACE -> WizardRaceStep(
                    origRaces = origRaces,
                    homebrewRaces = homebrewRaces,
                    query = homebrewRaceQuery,
                    onQueryChange = viewModel::setRaceQuery,
                    selectedRace = selectedRace,
                    onRaceSelected = viewModel::selectRace
                )

                WizardStep.ABILITIES -> WizardAbilitiesStep(
                    method = abilityMethod,
                    selectedRace = selectedRace,
                    standardAssignments = standardAssignments,
                    pendingPoolValue = pendingPoolValue,
                    pointBuyScores = pointBuyScores,
                    pointsSpent = pointsSpent,
                    rolledResults = rolledResults,
                    rollIndexAssignments = rollIndexAssignments,
                    pendingRollIndex = pendingRollIndex,
                    onMethodChange = viewModel::setAbilityMethod,
                    onSelectPoolValue = viewModel::selectPoolValue,
                    onAssignToAbility = viewModel::assignValueToAbility,
                    onUnassignAbility = viewModel::unassignAbility,
                    onIncrementPB = viewModel::incrementPointBuy,
                    onDecrementPB = viewModel::decrementPointBuy,
                    onRollAll = viewModel::rollAllAbilities,
                    onSelectRollIndex = viewModel::selectRollIndex,
                )

                WizardStep.CLASS -> WizardClassStep(
                    origClasses = origClasses,
                    homebrewClasses = homebrewClasses,
                    query = homebrewClassQuery,
                    onQueryChange = viewModel::setClassQuery,
                    selectedClass = selectedClass,
                    onClassSelected = viewModel::selectClass
                )

                WizardStep.SKILLS -> {
                    WizardSkillsStep(
                        availableSkills = availableSkills,
                        selectedSkills = selectedSkills,
                        abilityBlock = baseAbilityBlock,
                        maxSkills = maxSkills,
                        onSkillToggle = viewModel::toggleSkill
                    )
                }

                WizardStep.LEVEL -> WizardLevelStep(
                    selectedClass = selectedClass,
                    customLevelEnabled = customLevelEnabled,
                    level = level,
                    calculatedHp = calculatedHp,
                    onToggleCustomLevel = viewModel::setCustomLevelEnabled,
                    onLevelChange = viewModel::setLevel,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreationWizardTopBar(
    step: WizardStep,
    selectedRace: Race?,
    selectedClass: CharacterClass?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val title = when (step) {
        WizardStep.NAME -> stringResource(R.string.wizard_step_name)
        WizardStep.RACE -> selectedRace?.name ?: stringResource(R.string.wizard_step_race)
        WizardStep.CLASS -> selectedClass?.name ?: stringResource(R.string.wizard_step_class)
        WizardStep.SKILLS -> stringResource(R.string.wizard_step_skills)
        WizardStep.ABILITIES -> stringResource(R.string.wizard_step_abilities)
        WizardStep.LEVEL -> stringResource(R.string.wizard_step_level)
    }
    val totalSteps = WizardStep.entries.size
    val currentIndex = step.ordinal

    TopAppBar(
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.back)
                )
            }
        },
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(
                        R.string.wizard_step_counter,
                        currentIndex + 1,
                        totalSteps
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        actions = {
            // Step dot indicators
            Row(
                modifier = Modifier.padding(end = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                WizardStep.entries.forEach { s ->
                    val isActive = s == step
                    val isPast = s.ordinal < step.ordinal
                    val dotSize by animateDpAsState(
                        targetValue = if (isActive) 10.dp else 7.dp,
                        label = "dotSize"
                    )
                    val dotColor by animateColorAsState(
                        targetValue = when {
                            isActive -> MaterialTheme.colorScheme.primary
                            isPast -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                            else -> MaterialTheme.colorScheme.outlineVariant
                        },
                        label = "dotColor"
                    )
                    Box(
                        modifier = Modifier
                            .size(dotSize)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                }
            }
        }
    )
}

@Composable
fun CreationWizardBottomBar(
    step: WizardStep,
    canProceed: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLastStep = step == WizardStep.LEVEL
    val isFirstStep = step == WizardStep.NAME

    BottomAppBar(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button — ghost on first step, outlined otherwise
            if (isFirstStep) {
                Spacer(Modifier.weight(1f))
            } else {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.back))
                }
            }

            Spacer(Modifier.width(12.dp))

            Button(
                onClick = if (isLastStep) onFinish else onNext,
                enabled = canProceed,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    if (isLastStep) stringResource(R.string.wizard_create)
                    else stringResource(R.string.next)
                )
                if (!isLastStep) {
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun WizardChip(
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraSmall,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun WizardSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Preview
@Composable
private fun CreationWizardTopBarPreview() {
    DnDSheetTheme {
        CreationWizardBottomBar(
            step = WizardStep.SKILLS,
            canProceed = true,
            onNext = {},
            onBack = {},
            onFinish = {},
        )
    }
}