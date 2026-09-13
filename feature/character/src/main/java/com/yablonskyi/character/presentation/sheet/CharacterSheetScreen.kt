package com.yablonskyi.character.presentation.sheet

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.PreviewDynamicColors
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.window.core.layout.WindowSizeClass
import com.yablonskyi.character.presentation.common.UiUtils
import com.yablonskyi.character.presentation.sheet.components.CharacterSheetBottomSheets
import com.yablonskyi.character.presentation.sheet.components.CharacterTopAppBar
import com.yablonskyi.character.presentation.sheet.components.DiceResultOverlay
import com.yablonskyi.character.presentation.sheet.components.ExpandedTopAppBar
import com.yablonskyi.character.presentation.sheet.components.VerticalCharacterLayout
import com.yablonskyi.character.presentation.sheet.components.WideCharacterLayout
import com.yablonskyi.character.presentation.sheet.model.CharacterSheetEditor
import com.yablonskyi.character.presentation.sheet.model.CharacterTab
import com.yablonskyi.character.presentation.sheet.model.SpellFilter
import com.yablonskyi.character.presentation.sheet.slides.AbilitySlide
import com.yablonskyi.character.presentation.sheet.slides.AttackSlide
import com.yablonskyi.character.presentation.sheet.slides.BackstorySlide
import com.yablonskyi.character.presentation.sheet.slides.FeaturesSlide
import com.yablonskyi.character.presentation.sheet.slides.InventorySlide
import com.yablonskyi.character.presentation.sheet.slides.NotesSlide
import com.yablonskyi.character.presentation.sheet.slides.SpellSlide
import com.yablonskyi.dice.DiceIntent
import com.yablonskyi.dice.DiceRollFloatingActionButton
import com.yablonskyi.dice.DiceRollState
import com.yablonskyi.domain.character.CharacterChange
import com.yablonskyi.domain.character.CharacterTextField
import com.yablonskyi.ui.theme.DnDSheetTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedTransitionScope.CharacterSheetScreen(
    uiState: CharacterSheetState,
    onIntent: (CharacterSheetIntent) -> Unit,
    diceState: DiceRollState = DiceRollState(),
    onDiceIntent: (DiceIntent) -> Unit = {},
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val character = uiState.character

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = character == null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            CircularProgressIndicator()
        }

        AnimatedVisibility(
            visible = character != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            if (character != null) {
                val charId = character.id

                val imageModifier = Modifier.sharedBounds(
                    sharedContentState = rememberSharedContentState(key = "image_$charId"),
                    animatedVisibilityScope = animatedVisibilityScope,
                    resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
                    clipInOverlayDuringTransition = OverlayClip(CircleShape)
                )

                val nameModifier = Modifier
                    .sharedBounds(
                        sharedContentState = rememberSharedContentState(key = "name_$charId"),
                        animatedVisibilityScope = animatedVisibilityScope,
                        resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
                    )
                    .skipToLookaheadSize()

                val classRaceModifier = Modifier
                    .sharedBounds(
                        sharedContentState = rememberSharedContentState(key = "class_$charId"),
                        animatedVisibilityScope = animatedVisibilityScope,
                        resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                    )
                    .skipToLookaheadSize()

                val scope = rememberCoroutineScope()
                val configuration = LocalConfiguration.current
                val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
                val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
                val hasEnoughWidth =
                    windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
                val isWideScreen = hasEnoughWidth && isLandscape

                val tabs = CharacterTab.entries
                val pagerState = rememberPagerState(pageCount = { tabs.size })

                // Sheets
                val activeSheet = uiState.editor
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                val closeSheet: () -> Unit = remember(scope, sheetState, onIntent) {
                    {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            onIntent(CharacterSheetIntent.EditorChanged(null))
                        }
                    }
                }

                val focusManager = LocalFocusManager.current
                LaunchedEffect(pagerState.currentPage) {
                    focusManager.clearFocus()
                }

                val onHealthClick = remember(onIntent) {
                    {
                        onIntent(
                            CharacterSheetIntent.EditorChanged(CharacterSheetEditor.EditHealth)
                        )
                    }
                }
                val onSettingsClick = remember(character.id, onIntent) {
                    { onIntent(CharacterSheetIntent.OpenSettings) }
                }

                val movableTabContent: @Composable (CharacterTab, Modifier) -> Unit =
                    { tab: CharacterTab, modifier: Modifier ->
                        when (tab) {
                            CharacterTab.ABILITIES -> {
                                AbilitySlide(
                                    character = character,
                                    onRollClick = { onDiceIntent(DiceIntent.RegularStringRoll(it)) },
                                    onAbilityClick = { ability ->
                                        onIntent(
                                            CharacterSheetIntent.EditorChanged(
                                                CharacterSheetEditor.EditAbility(ability)
                                            )
                                        )
                                    },
                                    onProfSavingThrowClick = { ability, proficient ->
                                        onIntent(
                                            CharacterSheetIntent.Change(
                                                CharacterChange.SavingThrow(
                                                    ability,
                                                    proficient
                                                )
                                            )
                                        )
                                    },
                                    onProficiencyChange = { skill, level ->
                                        onIntent(
                                            CharacterSheetIntent.Change(
                                                CharacterChange.SkillProficiency(
                                                    skill,
                                                    level
                                                )
                                            )
                                        )
                                    },
                                    modifier = modifier
                                )
                            }

                            CharacterTab.SPELLS -> {
                                SpellSlide(
                                    character = character,
                                    spells = uiState.spells,
                                    availableFilters = uiState.availableFilters,
                                    currentFilter = uiState.currentFilter,
                                    onFilterChange = {
                                        onIntent(
                                            CharacterSheetIntent.FilterChanged(
                                                it
                                            )
                                        )
                                    },
                                    onRollClick = { onDiceIntent(DiceIntent.RegularStringRoll(it)) },
                                    onManageSpellsClick = { _ -> onIntent(CharacterSheetIntent.ManageSpells) },
                                    onSlotClick = { level, delta ->
                                        onIntent(
                                            CharacterSheetIntent.Change(
                                                CharacterChange.SlotUsed(level, delta)
                                            )
                                        )
                                    },
                                    onSpellClick = {
                                        onIntent(
                                            CharacterSheetIntent.EditorChanged(
                                                CharacterSheetEditor.ViewSpell(it.spellId)
                                            )
                                        )
                                    },
                                    modifier = modifier,
                                )
                            }

                            CharacterTab.ATTACKS -> {
                                AttackSlide(
                                    attacks = uiState.attacks,
                                    onAdd = {
                                        onIntent(
                                            CharacterSheetIntent.EditorChanged(
                                                CharacterSheetEditor.EditAttack()
                                            )
                                        )
                                    },
                                    onUpdate = {
                                        onIntent(
                                            CharacterSheetIntent.EditorChanged(
                                                CharacterSheetEditor.EditAttack(it.attackId)
                                            )
                                        )
                                    },
                                    onRollClick = { onDiceIntent(DiceIntent.RegularStringRoll(it)) },
                                    modifier = modifier
                                )
                            }

                            CharacterTab.FEATURES -> {
                                FeaturesSlide(
                                    traits = character.traits,
                                    feats = character.feats,
                                    proficiencies = character.proficiencies,
                                    updateFeats = {
                                        onIntent(
                                            CharacterSheetIntent.Change(
                                                CharacterChange.Text(CharacterTextField.FEATS, it)
                                            )
                                        )
                                    },
                                    updateTraits = {
                                        onIntent(
                                            CharacterSheetIntent.Change(
                                                CharacterChange.Text(CharacterTextField.TRAITS, it)
                                            )
                                        )
                                    },
                                    updateProficiencies = {
                                        onIntent(
                                            CharacterSheetIntent.Change(
                                                CharacterChange.Text(
                                                    CharacterTextField.PROFICIENCIES,
                                                    it
                                                )
                                            )
                                        )
                                    },
                                    modifier = modifier
                                )
                            }

                            CharacterTab.INVENTORY -> {
                                InventorySlide(
                                    coins = character.coins,
                                    inventory = character.inventory,
                                    onCoinChange = {
                                        onIntent(
                                            CharacterSheetIntent.Change(
                                                CharacterChange.Coins(it)
                                            )
                                        )
                                    },
                                    onSaveText = {
                                        onIntent(
                                            CharacterSheetIntent.Change(
                                                CharacterChange.Text(
                                                    CharacterTextField.INVENTORY,
                                                    it
                                                )
                                            )
                                        )
                                    },
                                    modifier = modifier
                                )
                            }

                            CharacterTab.BACKSTORY -> {
                                BackstorySlide(
                                    backstory = character.backstory,
                                    onSaveText = {
                                        onIntent(
                                            CharacterSheetIntent.Change(
                                                CharacterChange.Text(
                                                    CharacterTextField.BACKSTORY,
                                                    it
                                                )
                                            )
                                        )
                                    },
                                    modifier = modifier
                                )
                            }

                            CharacterTab.NOTES -> {
                                NotesSlide(
                                    notes = character.notes,
                                    onSaveText = {
                                        onIntent(
                                            CharacterSheetIntent.Change(
                                                CharacterChange.Text(CharacterTextField.NOTES, it)
                                            )
                                        )
                                    },
                                    modifier = modifier
                                )
                            }
                        }
                    }

                Scaffold(
                    topBar = {
                        if (isWideScreen) {
                            ExpandedTopAppBar(
                                name = character.name,
                                race = character.race,
                                charClass = character.charClass,
                                imagePath = character.imagePath,
                                armorClass = character.armorClass,
                                speed = character.speed,
                                proficiencyBonus = character.getProfBonus(),
                                onSettingsNavigate = onSettingsClick,
                                onNavigateBack = { onIntent(CharacterSheetIntent.BackClicked) },
                                nameModifier = nameModifier,
                                classRaceModifier = classRaceModifier,
                                imageModifier = imageModifier,
                            )
                        } else {
                            CharacterTopAppBar(
                                name = character.name,
                                race = character.race,
                                charClass = character.charClass,
                                imagePath = character.imagePath,
                                onNavigateBack = { onIntent(CharacterSheetIntent.BackClicked) },
                                onSettingsNavigate = onSettingsClick,
                                nameModifier = nameModifier,
                                classRaceModifier = classRaceModifier,
                                imageModifier = imageModifier,
                                lessDetails = uiState.lessDetails,
                                onLessDetails = { onIntent(CharacterSheetIntent.ToggleDetails) }
                            )
                        }
                    },
                    floatingActionButton = {
                        DiceRollFloatingActionButton(
                            onClick = { onDiceIntent(DiceIntent.RegularRoll(it)) }
                        )
                    },
                ) { padding ->
                    Box(
                        Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .consumeWindowInsets(padding)
                            .imePadding()
                    ) {
                        if (isWideScreen) {
                            WideCharacterLayout(
                                character = character,
                                leftSelectedTab = uiState.leftSelectedTab,
                                rightSelectedTab = uiState.rightSelectedTab,
                                onLeftTabSelected = {
                                    onIntent(
                                        CharacterSheetIntent.LeftTabSelected(
                                            it
                                        )
                                    )
                                },
                                onRightTabSelected = {
                                    onIntent(
                                        CharacterSheetIntent.RightTabSelected(
                                            it
                                        )
                                    )
                                },
                                onDiceButtonClick = { onDiceIntent(DiceIntent.RegularStringRoll(it)) },
                                onRestClick = { onIntent(CharacterSheetIntent.Change(CharacterChange.LongRest)) },
                                onHealthClick = onHealthClick,
                                tabContent = movableTabContent
                            )
                        } else {
                            VerticalCharacterLayout(
                                currentHp = character.currentHp,
                                maxHp = character.maxHp,
                                tempHp = character.tempHp,
                                initiativeBonus = character.getInitiativeBonus(),
                                armorClass = character.armorClass,
                                speed = character.speed,
                                proficiencyBonus = character.getProfBonus(),
                                tabs = tabs,
                                pagerState = pagerState,
                                onDiceButtonClick = { onDiceIntent(DiceIntent.RegularStringRoll(it)) },
                                onRestClick = { onIntent(CharacterSheetIntent.Change(CharacterChange.LongRest)) },
                                onHealthClick = onHealthClick,
                                onTabSelected = { newTab ->
                                    scope.launch { pagerState.animateScrollToPage(newTab.ordinal) }
                                },
                                lessDetails = uiState.lessDetails,
                                tabContent = movableTabContent
                            )
                        }
                        DiceResultOverlay(
                            diceState = diceState,
                            onDismiss = { onDiceIntent(DiceIntent.DismissResult) },
                            onPinClick = { onDiceIntent(DiceIntent.PinResult) },
                            modifier = Modifier.align(Alignment.BottomStart)
                        )
                    }
                }
                CharacterSheetBottomSheets(
                    activeSheet = activeSheet,
                    attacks = uiState.rawAttacks,
                    spells = uiState.allSpells,
                    character = character,
                    sheetState = sheetState,
                    onDismiss = { onIntent(CharacterSheetIntent.EditorChanged(null)) },
                    onCloseSheet = closeSheet,
                    onChange = { onIntent(CharacterSheetIntent.Change(it)) },
                    updateAbility = { ability, score ->
                        onIntent(
                            CharacterSheetIntent.Change(
                                CharacterChange.AbilityScore(ability, score)
                            )
                        )
                    },
                    saveAttack = { onIntent(CharacterSheetIntent.AttackSaved(it)) },
                    deleteAttack = { onIntent(CharacterSheetIntent.AttackDeleted(it)) }
                )
            }
        }
    }
}

@PreviewDynamicColors
@PreviewScreenSizes
@PreviewLightDark
@Composable
private fun CharacterSheetScreenPreview() {
    DnDSheetTheme {
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
                CharacterSheetScreen(
                    uiState = CharacterSheetState(
                        character = UiUtils.sampleCharacters.first(),
                        allSpells = UiUtils.sampleSpells,
                        rawAttacks = emptyList(),
                        currentFilter = SpellFilter.All,
                        leftSelectedTab = CharacterTab.ABILITIES,
                        rightSelectedTab = CharacterTab.SPELLS,
                    ),
                    onIntent = {},
                    animatedVisibilityScope = this
                )
            }
        }
    }
}