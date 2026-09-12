package com.yablonskyi.wizard

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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import com.yablonskyi.model.rulebook.CharacterClass
import com.yablonskyi.model.rulebook.Race
import com.yablonskyi.ui.utils.PreviewThemeWrapper
import com.yablonskyi.ui.R
import com.yablonskyi.ui.theme.Dimens
import com.yablonskyi.wizard.viewmodel.WizardIntent
import com.yablonskyi.wizard.viewmodel.WizardStep
import com.yablonskyi.wizard.viewmodel.WizardUiState

@Composable
fun CharacterCreationWizardScreen(
    uiState: WizardUiState,
    onIntent: (WizardIntent) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val currentStep = uiState.form.step
    val canProceed = uiState.form.canProceed

    val name = uiState.form.name
    val origRaces = uiState.origRaces
    val homebrewRaces = uiState.homebrewRaces
    val homebrewRaceQuery = uiState.form.raceQuery
    val origClasses = uiState.origClasses
    val homebrewClasses = uiState.homebrewClasses
    val homebrewClassQuery = uiState.form.classQuery
    val selectedRace = uiState.form.selectedRace
    val selectedClass = uiState.form.selectedClass
    val selectedSkills = uiState.form.selectedSkills
    val availableSkills = uiState.form.availableSkills
    val maxSkills = uiState.form.maxSkills
    val abilityMethod = uiState.form.abilityMethod
    val standardAssignments = uiState.form.standardAssignments
    val pendingPoolValue = uiState.form.pendingPoolValue
    val pointBuyScores = uiState.form.pointBuyScores
    val pointsSpent = uiState.form.pointsSpent
    val rolledResults = uiState.form.rolledResults
    val rollIndexAssignments = uiState.form.rollIndexAssignments
    val pendingRollIndex = uiState.form.pendingRollIndex
    val baseAbilityBlock = uiState.form.baseAbilityBlock
    val level = uiState.form.level
    val calculatedHp = uiState.form.calculatedHp

    BackHandler {
        onIntent(WizardIntent.Back)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CreationWizardTopBar(
                step = currentStep,
                selectedRace = selectedRace,
                selectedClass = selectedClass,
                onNavigateBack = { onIntent(WizardIntent.Close) },
            )
        },
        bottomBar = {
            CreationWizardBottomBar(
                step = currentStep,
                canProceed = canProceed,
                isSubmitting = uiState.form.isSubmitting,
                onBack = { onIntent(WizardIntent.Back) },
                onNext = { onIntent(WizardIntent.Next) },
                onFinish = { onIntent(WizardIntent.Finish) },
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
                        onNameChange = { onIntent(WizardIntent.NameChanged(it)) }
                    )
                }

                WizardStep.RACE -> WizardRaceStep(
                    origRaces = origRaces,
                    homebrewRaces = homebrewRaces,
                    query = homebrewRaceQuery,
                    onQueryChange = { onIntent(WizardIntent.RaceQueryChanged(it)) },
                    selectedRace = selectedRace,
                    onRaceSelected = { onIntent(WizardIntent.RaceSelected(it)) }
                )

                WizardStep.ABILITIES -> WizardAbilitiesStep(
                    method = abilityMethod,
                    selectedRace = selectedRace!!,
                    standardAssignments = standardAssignments,
                    pendingPoolValue = pendingPoolValue,
                    pointBuyScores = pointBuyScores,
                    pointsSpent = pointsSpent,
                    rolledResults = rolledResults,
                    rollIndexAssignments = rollIndexAssignments,
                    pendingRollIndex = pendingRollIndex,
                    onMethodChange = { onIntent(WizardIntent.AbilityMethodChanged(it)) },
                    onSelectPoolValue = { onIntent(WizardIntent.PoolValueSelected(it)) },
                    onAssignToAbility = { onIntent(WizardIntent.AbilityAssigned(it)) },
                    onUnassignAbility = { onIntent(WizardIntent.AbilityUnassigned(it)) },
                    onIncrementPB = { onIntent(WizardIntent.PointBuyIncremented(it)) },
                    onDecrementPB = { onIntent(WizardIntent.PointBuyDecremented(it)) },
                    onRollAll = { onIntent(WizardIntent.RollAll) },
                    onSelectRollIndex = { onIntent(WizardIntent.RollIndexSelected(it)) },
                )

                WizardStep.CLASS -> WizardClassStep(
                    origClasses = origClasses,
                    homebrewClasses = homebrewClasses,
                    query = homebrewClassQuery,
                    onQueryChange = { onIntent(WizardIntent.ClassQueryChanged(it)) },
                    selectedClass = selectedClass,
                    onClassSelected = { onIntent(WizardIntent.ClassSelected(it)) }
                )

                WizardStep.SKILLS -> {
                    WizardSkillsStep(
                        availableSkills = availableSkills,
                        selectedSkills = selectedSkills,
                        abilityBlock = baseAbilityBlock,
                        maxSkills = maxSkills,
                        onSkillToggle = { onIntent(WizardIntent.SkillToggled(it)) }
                    )
                }

                WizardStep.LEVEL -> WizardLevelStep(
                    selectedClassHitDice = selectedClass?.hitDice!!,
                    level = level,
                    calculatedHp = calculatedHp,
                    onLevelChange = { onIntent(WizardIntent.LevelChanged(it)) },
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
    modifier: Modifier = Modifier,
    isSubmitting: Boolean = false
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
            if (isFirstStep) {
                Spacer(Modifier.weight(1f))
            } else {
                OutlinedButton(
                    onClick = onBack,
                    enabled = !isSubmitting,
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
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(
                horizontal = Dimens.Spacing.Medium,
                vertical = Dimens.Spacing.Small
            )
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
    PreviewThemeWrapper.Preview {
        CreationWizardBottomBar(
            step = WizardStep.SKILLS,
            canProceed = true,
            onNext = {},
            onBack = {},
            onFinish = {},
        )
    }
}