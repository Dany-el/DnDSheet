package com.yablonskyi.dndsheet.ui.character

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.PreviewDynamicColors
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import com.yablonskyi.dndsheet.data.model.character.Ability
import com.yablonskyi.dndsheet.data.model.character.Attack
import com.yablonskyi.dndsheet.data.model.character.Character
import com.yablonskyi.dndsheet.data.model.character.ProficiencyLevel
import com.yablonskyi.dndsheet.data.model.character.Skill
import com.yablonskyi.dndsheet.data.model.character.Spell
import com.yablonskyi.dndsheet.data.model.character.SpellLevel
import com.yablonskyi.dndsheet.ui.attack.AttackUiModel
import com.yablonskyi.dndsheet.ui.character.slides.AbilitySlide
import com.yablonskyi.dndsheet.ui.character.slides.AttackSlide
import com.yablonskyi.dndsheet.ui.character.slides.BackstorySlide
import com.yablonskyi.dndsheet.ui.character.slides.FeaturesSlide
import com.yablonskyi.dndsheet.ui.character.slides.InventorySlide
import com.yablonskyi.dndsheet.ui.character.slides.NotesSlide
import com.yablonskyi.dndsheet.ui.character.slides.SpellSlide
import com.yablonskyi.dndsheet.ui.dice.DiceRollFloatingActionButton
import com.yablonskyi.dndsheet.ui.dice.DiceRollState
import com.yablonskyi.dndsheet.ui.spell.SpellFilter
import com.yablonskyi.dndsheet.ui.theme.DnDSheetTheme
import com.yablonskyi.dndsheet.ui.utils.UiUtils
import kotlinx.coroutines.launch

sealed class CharacterSheetConfig {
    data class EditAbility(val ability: Ability) : CharacterSheetConfig()
    object EditHealth : CharacterSheetConfig()
    data class EditAttack(val attack: Attack) : CharacterSheetConfig()
    data class ViewSpell(val spell: Spell) : CharacterSheetConfig()
}

@Stable
data class CharacterSheetUiState(
    val character: Character? = null,
    val spells: List<Spell> = emptyList(),
    val attacks: List<AttackUiModel> = emptyList(),
    val currentFilter: SpellFilter = SpellFilter.All,
    val diceState: DiceRollState = DiceRollState(),
    val availableFilters: List<SpellFilter> = emptyList(),
    val rightSelectedTab: CharacterTab = CharacterTab.SPELLS,
    val leftSelectedTab: CharacterTab = CharacterTab.ABILITIES,
)

@Immutable
data class CharacterSheetActions(
    val onDiceButtonClick: (String) -> Unit = {},
    val onDiceClick: (Map<Int, Int>) -> Unit = {},
    val onPinClick: () -> Unit = {},
    val onDismissResult: () -> Unit = {},
    val onUpdateCharacter: (Character) -> Unit = {},
    val onFilterChange: (SpellFilter) -> Unit = {},
    val updateAbility: (Ability, Int) -> Unit = { _, _ -> },
    val updateProfLevel: (Skill, ProficiencyLevel) -> Unit = { _, _ -> },
    val updateSavingThrowProficiency: (Ability, Boolean) -> Unit = { _, _ -> },
    val saveAttack: (Attack) -> Unit = {},
    val deleteAttack: (Attack) -> Unit = {},
    val onSettingsNavigate: (Long) -> Unit = {},
    val onNavigateBack: () -> Unit = {},
    val onManageClick: (Long) -> Unit = {},
    val onSlotClick: (SpellLevel, Int) -> Unit = { _, _ -> },
    val onRestClick: () -> Unit = {},
    val onLeftTabSelected: (CharacterTab) -> Unit = {},
    val onRightTabSelected: (CharacterTab) -> Unit = {},
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedTransitionScope.CharacterSheetScreen(
    uiState: CharacterSheetUiState,
    actions: CharacterSheetActions,
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
                var activeSheet by remember { mutableStateOf<CharacterSheetConfig?>(null) }
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                val closeSheet: () -> Unit = remember(scope, sheetState) {
                    {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            activeSheet = null
                        }
                    }
                }

                val focusManager = LocalFocusManager.current
                LaunchedEffect(pagerState.currentPage) {
                    focusManager.clearFocus()
                }

                val onHealthClick = remember { { activeSheet = CharacterSheetConfig.EditHealth } }
                val onSettingsClick = remember(character.id) {
                    { actions.onSettingsNavigate(character.id) }
                }

                val movableTabContent: @Composable (CharacterTab, Modifier) -> Unit =
                    { tab: CharacterTab, modifier: Modifier ->
                        when (tab) {
                            CharacterTab.ABILITIES -> {
                                AbilitySlide(
                                    character = character,
                                    onRollClick = actions.onDiceButtonClick,
                                    onAbilityClick = { ability ->
                                        activeSheet = CharacterSheetConfig.EditAbility(ability)
                                    },
                                    onProfSavingThrowClick = actions.updateSavingThrowProficiency,
                                    onProficiencyChange = actions.updateProfLevel,
                                    modifier = modifier
                                )
                            }

                            CharacterTab.SPELLS -> {
                                SpellSlide(
                                    character = character,
                                    spells = uiState.spells,
                                    availableFilters = uiState.availableFilters,
                                    currentFilter = uiState.currentFilter,
                                    onFilterChange = actions.onFilterChange,
                                    onRollClick = actions.onDiceButtonClick,
                                    onManageSpellsClick = actions.onManageClick,
                                    onSlotClick = actions.onSlotClick,
                                    onSpellClick = {
                                        activeSheet = CharacterSheetConfig.ViewSpell(it)
                                    },
                                    modifier = modifier,
                                )
                            }

                            CharacterTab.ATTACKS -> {
                                AttackSlide(
                                    attacks = uiState.attacks,
                                    onAdd = {
                                        activeSheet =
                                            CharacterSheetConfig.EditAttack(Attack(characterId = character.id))
                                    },
                                    onUpdate = {
                                        activeSheet = CharacterSheetConfig.EditAttack(it)
                                    },
                                    onRollClick = actions.onDiceButtonClick,
                                    modifier = modifier
                                )
                            }

                            CharacterTab.FEATURES -> {
                                FeaturesSlide(
                                    traits = character.traits,
                                    feats = character.feats,
                                    proficiencies = character.proficiencies,
                                    updateFeats = { actions.onUpdateCharacter(character.copy(feats = it)) },
                                    updateTraits = { actions.onUpdateCharacter(character.copy(traits = it)) },
                                    updateProficiencies = {
                                        actions.onUpdateCharacter(
                                            character.copy(
                                                proficiencies = it
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
                                    onCoinChange = { actions.onUpdateCharacter(character.copy(coins = it)) },
                                    onSaveText = {
                                        actions.onUpdateCharacter(
                                            character.copy(
                                                inventory = it
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
                                        actions.onUpdateCharacter(
                                            character.copy(
                                                backstory = it
                                            )
                                        )
                                    },
                                    modifier = modifier
                                )
                            }

                            CharacterTab.NOTES -> {
                                NotesSlide(
                                    notes = character.notes,
                                    onSaveText = { actions.onUpdateCharacter(character.copy(notes = it)) },
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
                                onNavigateBack = actions.onNavigateBack,
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
                                onNavigateBack = actions.onNavigateBack,
                                onSettingsNavigate = onSettingsClick,
                                nameModifier = nameModifier,
                                classRaceModifier = classRaceModifier,
                                imageModifier = imageModifier,
                            )
                        }
                    },
                    floatingActionButton = {
                        DiceRollFloatingActionButton(
                            onClick = actions.onDiceClick
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
                                onLeftTabSelected = actions.onLeftTabSelected,
                                onRightTabSelected = actions.onRightTabSelected,
                                onDiceButtonClick = actions.onDiceButtonClick,
                                onRestClick = actions.onRestClick,
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
                                onDiceButtonClick = actions.onDiceButtonClick,
                                onRestClick = actions.onRestClick,
                                onHealthClick = onHealthClick,
                                onTabSelected = { newTab ->
                                    scope.launch { pagerState.animateScrollToPage(newTab.ordinal) }
                                },
                                tabContent = movableTabContent
                            )
                        }
                        DiceResultOverlay(
                            diceState = uiState.diceState,
                            onDismiss = actions.onDismissResult,
                            onPinClick = actions.onPinClick,
                            modifier = Modifier.align(Alignment.BottomStart)
                        )
                    }
                }
                CharacterSheetBottomSheets(
                    activeSheet = activeSheet,
                    character = character,
                    sheetState = sheetState,
                    onDismiss = { activeSheet = null },
                    onCloseSheet = closeSheet,
                    onUpdateCharacter = actions.onUpdateCharacter,
                    updateAbility = actions.updateAbility,
                    saveAttack = actions.saveAttack,
                    deleteAttack = actions.deleteAttack
                )
            }
        }
    }
}

@Composable
fun WideCharacterLayout(
    character: Character,
    leftSelectedTab: CharacterTab,
    rightSelectedTab: CharacterTab,
    onLeftTabSelected: (CharacterTab) -> Unit,
    onRightTabSelected: (CharacterTab) -> Unit,
    onDiceButtonClick: (String) -> Unit,
    onRestClick: () -> Unit,
    onHealthClick: () -> Unit,
    tabContent: @Composable (CharacterTab, Modifier) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .fillMaxSize()
            .padding(top = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.Top)
                    .padding(vertical = 4.dp)
                    .padding(end = 8.dp)
            ) {
                SlideSelector(
                    tabs = CharacterTab.entries,
                    currentTab = leftSelectedTab,
                    onTabSelected = onLeftTabSelected,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(16.dp))
                HealthBar(
                    currentHp = character.currentHp,
                    maxHp = character.maxHp,
                    tempHp = character.tempHp,
                    onHealthClick = onHealthClick,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.width(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                CharacterDetailsRowExpanded(
                    initiativeBonus = character.getInitiativeBonus(),
                    onRollClick = onDiceButtonClick,
                    onRestClick = onRestClick,
                    modifier = Modifier.weight(1f)
                )

                SlideSelector(
                    tabs = CharacterTab.entries,
                    currentTab = rightSelectedTab,
                    onTabSelected = onRightTabSelected,
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.Top)
                        .padding(4.dp)
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Crossfade(
                    targetState = leftSelectedTab,
                    modifier = Modifier.align(Alignment.TopCenter),
                    label = "LeftPaneAnimation"
                ) { currentTab ->
                    tabContent(currentTab, Modifier)
                }
            }
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Crossfade(
                    targetState = rightSelectedTab,
                    modifier = Modifier.align(Alignment.TopCenter),
                    label = "RightPaneAnimation"
                ) { currentTab ->
                    tabContent(currentTab, Modifier)
                }
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
                    uiState = CharacterSheetUiState(
                        character = UiUtils.sampleCharacters.first(),
                        spells = UiUtils.sampleSpells,
                        attacks = emptyList(),
                        currentFilter = SpellFilter.All,
                        diceState = DiceRollState(),
                        availableFilters = emptyList(),
                        leftSelectedTab = CharacterTab.ABILITIES,
                        rightSelectedTab = CharacterTab.SPELLS,
                    ),
                    actions = CharacterSheetActions(),
                    animatedVisibilityScope = this
                )
            }
        }
    }
}