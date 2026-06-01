package com.yablonskyi.dndsheet.ui.navigation

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.window.core.layout.WindowSizeClass
import com.skydoves.compose.stability.runtime.TraceRecomposition
import com.yablonskyi.dndsheet.R
import com.yablonskyi.dndsheet.ui.attack.AttackViewModel
import com.yablonskyi.dndsheet.ui.character.CharacterDetailViewModel
import com.yablonskyi.dndsheet.ui.character.CharacterEditScreen
import com.yablonskyi.dndsheet.ui.character.CharacterListActions
import com.yablonskyi.dndsheet.ui.character.CharacterListUiState
import com.yablonskyi.dndsheet.ui.character.CharacterListViewModel
import com.yablonskyi.dndsheet.ui.character.CharacterSettingsViewModel
import com.yablonskyi.dndsheet.ui.character.CharacterSheetActions
import com.yablonskyi.dndsheet.ui.character.CharacterSheetScreen
import com.yablonskyi.dndsheet.ui.character.CharacterSheetUiState
import com.yablonskyi.dndsheet.ui.character.ListOfCharactersScreen
import com.yablonskyi.dndsheet.ui.compendium.CompendiumScreen
import com.yablonskyi.dndsheet.ui.compendium.CompendiumViewModel
import com.yablonskyi.dndsheet.ui.compendium.classes.CharacterClassesViewModel
import com.yablonskyi.dndsheet.ui.compendium.classes.ClassCreateScreen
import com.yablonskyi.dndsheet.ui.compendium.classes.ClassCreateViewModel
import com.yablonskyi.dndsheet.ui.compendium.classes.ClassDetailsScreen
import com.yablonskyi.dndsheet.ui.compendium.classes.ClassUpdateViewModel
import com.yablonskyi.dndsheet.ui.compendium.classes.ClassesActions
import com.yablonskyi.dndsheet.ui.compendium.classes.ClassesScreen
import com.yablonskyi.dndsheet.ui.compendium.classes.ClassesUiState
import com.yablonskyi.dndsheet.ui.compendium.classes.toCharacterClass
import com.yablonskyi.dndsheet.ui.compendium.races.RaceCreateScreen
import com.yablonskyi.dndsheet.ui.compendium.races.RaceCreateViewModel
import com.yablonskyi.dndsheet.ui.compendium.races.RaceDetailsScreen
import com.yablonskyi.dndsheet.ui.compendium.races.RaceUpdateViewModel
import com.yablonskyi.dndsheet.ui.compendium.races.RacesActions
import com.yablonskyi.dndsheet.ui.compendium.races.RacesScreen
import com.yablonskyi.dndsheet.ui.compendium.races.RacesUiState
import com.yablonskyi.dndsheet.ui.compendium.races.RacesViewModel
import com.yablonskyi.dndsheet.ui.compendium.races.toRace
import com.yablonskyi.dndsheet.ui.dice.DiceViewModel
import com.yablonskyi.dndsheet.ui.settings.AppSettingsScreen
import com.yablonskyi.dndsheet.ui.settings.AppSettingsViewModel
import com.yablonskyi.dndsheet.ui.spell.CharacterSpellLibraryViewModel
import com.yablonskyi.dndsheet.ui.spell.GlobalSpellLibraryViewModel
import com.yablonskyi.dndsheet.ui.spell.SpellEditScreen
import com.yablonskyi.dndsheet.ui.spell.SpellEditViewModel
import com.yablonskyi.dndsheet.ui.spell.SpellLibraryScreen
import com.yablonskyi.dndsheet.ui.spell.SpellViewModel
import com.yablonskyi.dndsheet.ui.utils.LoadingDialog
import com.yablonskyi.dndsheet.ui.wizard.CharacterCreationWizardScreen
import com.yablonskyi.dndsheet.ui.wizard.CharacterCreationWizardViewModel

@TraceRecomposition(tag = "DnDNavGraph")
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun DnDNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    diceViewModel: DiceViewModel = viewModel(),
) {
    val diceState by diceViewModel.diceRollState.collectAsStateWithLifecycle()

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val hasEnoughWidth =
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
    val hasEnoughHeight =
        windowSizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND)

    val isWideScreen = hasEnoughWidth && hasEnoughHeight

    Scaffold { innerPadding ->
        SharedTransitionLayout {
            NavHost(
                navController = navController,
                startDestination = ListOfCharactersRoute,
                modifier = modifier,
                enterTransition = {
                    val initialIndex = getTabIndex(initialState.destination.route)
                    val targetIndex = getTabIndex(targetState.destination.route)

                    if (initialIndex != -1 && targetIndex != -1) {
                        val forwardDirection =
                            if (isWideScreen) AnimatedContentTransitionScope.SlideDirection.Up else AnimatedContentTransitionScope.SlideDirection.Left
                        val backwardDirection =
                            if (isWideScreen) AnimatedContentTransitionScope.SlideDirection.Down else AnimatedContentTransitionScope.SlideDirection.Right

                        if (targetIndex > initialIndex) {
                            slideIntoContainer(
                                forwardDirection,
                                animationSpec = tween(400)
                            )
                        } else {
                            slideIntoContainer(
                                backwardDirection,
                                animationSpec = tween(400)
                            )
                        }
                    } else {
                        fadeIn(animationSpec = tween(400))
                    }
                },
                exitTransition = {
                    val initialIndex = getTabIndex(initialState.destination.route)
                    val targetIndex = getTabIndex(targetState.destination.route)

                    if (initialIndex != -1 && targetIndex != -1) {
                        val forwardDirection =
                            if (isWideScreen) AnimatedContentTransitionScope.SlideDirection.Up else AnimatedContentTransitionScope.SlideDirection.Left
                        val backwardDirection =
                            if (isWideScreen) AnimatedContentTransitionScope.SlideDirection.Down else AnimatedContentTransitionScope.SlideDirection.Right

                        if (targetIndex > initialIndex) {
                            slideOutOfContainer(
                                forwardDirection,
                                animationSpec = tween(400)
                            )
                        } else {
                            slideOutOfContainer(
                                backwardDirection,
                                animationSpec = tween(400)
                            )
                        }
                    } else {
                        fadeOut(animationSpec = tween(400))
                    }
                }
            ) {
                composable<ListOfCharactersRoute> {
                    val activity = LocalActivity.current as ComponentActivity

                    val viewModel: CharacterListViewModel = hiltViewModel(activity)
                    val settingsViewModel: AppSettingsViewModel = hiltViewModel(activity)

                    val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()

                    val characterListState by viewModel.characterListState.collectAsStateWithLifecycle()

                    val isSelectionMode by viewModel.isSelectionMode.collectAsStateWithLifecycle()
                    val selectedCharacters by viewModel.selectedCharacters.collectAsStateWithLifecycle()
                    val isAllSelected by viewModel.isAllSelected.collectAsStateWithLifecycle()

                    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

                    ListOfCharactersScreen(
                        uiState = CharacterListUiState(
                            characters = characterListState.characters,
                            loadingState = characterListState.isLoading,
                            listView = uiState.listView,
                            searchQuery = searchQuery,
                            isSelectionMode = isSelectionMode,
                            isAllSelected = isAllSelected,
                            selectedCharacters = selectedCharacters,
                            windowSizeClass = windowSizeClass
                        ),
                        actions = CharacterListActions(
                            onSearchQueryChange = viewModel::updateSearchQuery,
                            onClearSelection = viewModel::closeSelection,
                            onToggleListView = settingsViewModel::toggleListView,
                            onToggleSelectAll = viewModel::toggleSelectAll,
                            onDeleteSelected = viewModel::deleteSelectedCharacters,
                            toggleSelection = viewModel::toggleSelection,
                            onAdd = {
                                navController.navigate(CharacterCreationWizardRoute)
                            },
                            onDelete = viewModel::deleteCharacter,
                            onImportSheets = viewModel::importSheets,
                            onExportSheets = viewModel::getSheetsForExport,
                            onGetCharacterSheet = viewModel::getCharacterSheetForExport,
                            onCharacterClick = { charId ->
                                navController.navigate(
                                    CharacterSheetRoute(id = charId)
                                )
                            }
                        ),
                        animatedVisibilityScope = this@composable
                    )
                }

                composable<CharacterSheetRoute> {
                    val charViewModel: CharacterDetailViewModel = hiltViewModel()
                    val spellViewModel: SpellViewModel = hiltViewModel()
                    val attackViewModel: AttackViewModel = hiltViewModel()

                    val character by charViewModel.character.collectAsStateWithLifecycle()
                    val characterSpells by spellViewModel.spellList.collectAsStateWithLifecycle()
                    val currentFilter by spellViewModel.currentFilter.collectAsStateWithLifecycle()
                    val availableFilters by spellViewModel.availableFilters.collectAsStateWithLifecycle()
                    val characterAttacks by attackViewModel.attackList.collectAsStateWithLifecycle()

                    val lessDetails by charViewModel.lessDetails.collectAsStateWithLifecycle()

                    val rightSelectedTab by charViewModel.rightSelectedTab.collectAsStateWithLifecycle()
                    val leftSelectedTab by charViewModel.leftSelectedTab.collectAsStateWithLifecycle()

                    CharacterSheetScreen(
                        uiState = CharacterSheetUiState(
                            character = character,
                            spells = characterSpells,
                            attacks = characterAttacks,
                            currentFilter = currentFilter,
                            diceState = diceState,
                            availableFilters = availableFilters,
                            lessDetails = lessDetails,
                            rightSelectedTab = rightSelectedTab,
                            leftSelectedTab = leftSelectedTab
                        ),
                        actions = CharacterSheetActions(
                            onDiceButtonClick = diceViewModel::rollDiceFromString,
                            onDiceClick = diceViewModel::rollDice,
                            onPinClick = diceViewModel::pinResult,
                            onDismissResult = diceViewModel::dismissResult,
                            onUpdateCharacter = charViewModel::updateCharacter,
                            onFilterChange = spellViewModel::setFilter,
                            updateAbility = charViewModel::updateAbility,
                            updateProfLevel = charViewModel::updateSkillProficiency,
                            updateSavingThrowProficiency = charViewModel::updateSavingThrowProf,
                            saveAttack = attackViewModel::saveAttack,
                            deleteAttack = attackViewModel::deleteAttack,
                            onSettingsNavigate = { charId ->
                                navController.navigate(CharacterSettingsRoute(charId))
                            },
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            onManageClick = {
                                navController.navigate(
                                    SpellLibraryRoute(it)
                                )
                            },
                            onSlotClick = charViewModel::useSpellSlot,
                            onRestClick = charViewModel::performLongRest,
                            onLessDetails = charViewModel::toggleDetails,
                            onRightTabSelected = charViewModel::onRightTabSelected,
                            onLeftTabSelected = charViewModel::onLeftTabSelected
                        ),
                        animatedVisibilityScope = this@composable
                    )
                }

                composable<CharacterSettingsRoute>(
                    enterTransition = {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            tween(400)
                        )
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            tween(400)
                        )
                    },
                    popEnterTransition = {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            tween(400)
                        )
                    },
                    popExitTransition = {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            tween(400)
                        )
                    }
                ) {
                    val charViewModel: CharacterSettingsViewModel = hiltViewModel()

                    val character by charViewModel.character.collectAsStateWithLifecycle()

                    character?.let {
                        CharacterEditScreen(
                            character = it,
                            onUpdate = charViewModel::updateCharacter,
                            onSpellSlotsUpdate = charViewModel::updateSpellSlot,
                            onImageUpdated = charViewModel::updateImage,
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }

                composable<CompendiumRoute.SpellsLibrary> {
                    val activity = LocalActivity.current as ComponentActivity

                    val viewModel: GlobalSpellLibraryViewModel = hiltViewModel(activity)

                    val spells by viewModel.spellLibraryList.collectAsStateWithLifecycle()
                    val state by viewModel.spellListState.collectAsStateWithLifecycle()
                    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
                    val filterState by viewModel.filterState.collectAsStateWithLifecycle()
                    val isSelectionMode by viewModel.isSelectionMode.collectAsStateWithLifecycle()
                    val selectedSpells by viewModel.selectedSpells.collectAsStateWithLifecycle()
                    val isAllSelected by viewModel.isAllSelected.collectAsStateWithLifecycle()

                    SpellLibraryScreen(
                        spells = spells,
                        loadingState = state.isLoading,
                        searchQuery = searchQuery,
                        isLearnMode = viewModel.isLearnMode,

                        onNavigateBack = { navController.popBackStack() },
                        onAddSpell = { navController.navigate(UpdateSpellRoute(0)) },
                        onSearchQueryChange = viewModel::onSearchQueryChange,
                        onEditSpell = { spellId -> navController.navigate(UpdateSpellRoute(spellId)) },
                        onToggleSpell = {},
                        onDeleteSpell = viewModel::deleteSpellGlobally,
                        onImportSpells = viewModel::importSpells,
                        // Toggles
                        selectedSchool = filterState.schools,
                        selectedLevels = filterState.levels,
                        selectedDurations = filterState.durations,
                        selectedCastTimes = filterState.castTimes,
                        selectedConcentration = filterState.onlyConcentration,
                        selectedRitual = filterState.onlyRitual,
                        // Filters
                        toggleSchoolFilter = viewModel::toggleSchool,
                        toggleLevelFilter = viewModel::toggleLevel,
                        toggleCastTimeFilter = viewModel::toggleCastTime,
                        toggleDurationFilter = viewModel::toggleDuration,
                        toggleRitual = viewModel::toggleRitual,
                        toggleConcentration = viewModel::toggleConcentration,
                        // Selection
                        isSelectionMode = isSelectionMode,
                        selectedSpells = selectedSpells,
                        onDeleteSelected = viewModel::deleteSelectedSpells,
                        onToggleSelectAll = viewModel::toggleSelectAll,
                        onClearSelection = viewModel::closeSelection,
                        toggleSelection = viewModel::toggleSelection,
                        isAllSelected = isAllSelected,
                    )
                }

                composable<SpellLibraryRoute>(
                    enterTransition = {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            tween(400)
                        )
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            tween(400)
                        )
                    },
                    popEnterTransition = {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            tween(400)
                        )
                    },
                    popExitTransition = {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            tween(400)
                        )
                    }
                ) {
                    val viewModel: CharacterSpellLibraryViewModel = hiltViewModel()

                    val spells by viewModel.spellLibraryList.collectAsStateWithLifecycle()
                    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
                    val filterState by viewModel.filterState.collectAsStateWithLifecycle()

                    SpellLibraryScreen(
                        spells = spells,
                        searchQuery = searchQuery,
                        loadingState = false,
                        // Filters
                        isLearnMode = viewModel.isSelectionMode,
                        onNavigateBack = { navController.popBackStack() },
                        onAddSpell = {},
                        onSearchQueryChange = viewModel::onSearchQueryChange,

                        onEditSpell = {},
                        onToggleSpell = viewModel::toggleSpellSelection,
                        onDeleteSpell = {},
                        onImportSpells = {},
                        // Toggles
                        selectedSchool = filterState.schools,
                        selectedLevels = filterState.levels,
                        selectedDurations = filterState.durations,
                        selectedCastTimes = filterState.castTimes,
                        selectedConcentration = filterState.onlyConcentration,
                        selectedRitual = filterState.onlyRitual,
                        // Filters
                        toggleSchoolFilter = viewModel::toggleSchool,
                        toggleLevelFilter = viewModel::toggleLevel,
                        toggleCastTimeFilter = viewModel::toggleCastTime,
                        toggleDurationFilter = viewModel::toggleDuration,
                        toggleRitual = viewModel::toggleRitual,
                        toggleConcentration = viewModel::toggleConcentration,
                        // Selection
                        isSelectionMode = false,
                        selectedSpells = emptySet(),
                        onClearSelection = {},
                        onDeleteSelected = {},
                        onToggleSelectAll = {},
                        toggleSelection = {},
                        isAllSelected = false,
                    )
                }

                composable<UpdateSpellRoute> {
                    val viewModel: SpellEditViewModel = hiltViewModel()

                    val spellState by viewModel.spell.collectAsStateWithLifecycle()

                    spellState?.let { spell ->
                        SpellEditScreen(
                            spell = spell,
                            onUpdate = { updatedSpell ->
                                viewModel.saveSpell(updatedSpell)
                            },
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
                composable<AppSettingsRoute> {
                    AppSettingsScreen()
                }
                composable<CharacterCreationWizardRoute> {
                    val viewModel: CharacterCreationWizardViewModel = hiltViewModel()

                    LaunchedEffect(Unit) {
                        viewModel.createdId.collect { id ->
                            navController.popBackStack()
                            navController.navigate(CharacterSheetRoute(id = id))
                        }
                    }

                    CharacterCreationWizardScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable<CompendiumRoute.Home> {
                    val viewModel = hiltViewModel<CompendiumViewModel>()

                    val racesCount by viewModel.racesCount.collectAsStateWithLifecycle()
                    val classesCount by viewModel.classesCount.collectAsStateWithLifecycle()
                    val spellsCount by viewModel.spellsCount.collectAsStateWithLifecycle()

                    CompendiumScreen(
                        racesCount = racesCount,
                        classesCount = classesCount,
                        spellsCount = spellsCount,
                        onRacesClick = { navController.navigate(CompendiumRoute.RacesScreen) },
                        onClassesClick = { navController.navigate(CompendiumRoute.ClassesScreen) },
                        onSpellsClick = { navController.navigate(CompendiumRoute.SpellsLibrary) },
                    )
                }
                composable<CompendiumRoute.RacesScreen> {
                    val viewModel = hiltViewModel<RacesViewModel>()

                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                    val context = LocalContext.current
                    val res = LocalResources.current

                    RacesScreen(
                        uiState = RacesUiState(
                            origRaces = uiState.origRaces,
                            homebrewRaces = uiState.homebrewRaces,
                            origSectionState = uiState.origRacesSectionState,
                            homebrewSectionState = uiState.homebrewRacesSectionState,
                            isSelectionMode = uiState.isSelectionMode,
                            isAllSelected = uiState.isAllSelected,
                            searchQuery = uiState.searchQuery,
                            selectedRaceIds = uiState.selectedRaceIds
                        ),
                        actions = RacesActions(
                            onSearchQueryChange = viewModel::onSearchQueryChange,
                            onClearSelection = viewModel::clearSelection,
                            onDeleteSelected = viewModel::deleteSelected,
                            onToggleSelectAll = viewModel::toggleSelectAll,
                            onToggleRaceSelection = viewModel::toggleRaceSelection,
                            onExportRequested = viewModel::generateExportJson,
                            onExportAllSelected = { viewModel.generateExportJson(targetRaceId = null) },
                            onImportRequested = { json ->
                                viewModel.importRacesFromJson(json) {
                                    Toast.makeText(
                                        context,
                                        res.getString(R.string.success_import),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            onEdit = { navController.navigate(CompendiumRoute.RaceUpdateScreen(it)) },
                            onDelete = viewModel::delete,
                            onCreateRace = { navController.navigate(CompendiumRoute.RaceCreateScreen()) },
                            onDetails = {
                                navController.navigate(
                                    CompendiumRoute.RaceDetailsScreen(
                                        it
                                    )
                                )
                            },
                            onNavigateBack = { navController.popBackStack() },
                            onCollapseOrigSection = viewModel::collapseOrigRacesSection,
                            onCollapseHomebrewSection = viewModel::collapseHomebrewRacesSection,
                        )
                    )
                }
                composable<CompendiumRoute.RaceDetailsScreen> {
                    val viewModel = hiltViewModel<RacesViewModel>()

                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                    if (uiState.isLoading) {
                        LoadingDialog()
                    } else {
                        RaceDetailsScreen(
                            race = uiState.selectedRace!!,
                            onEdit = { navController.navigate(CompendiumRoute.RaceUpdateScreen(it)) },
                            onExportRequested = { viewModel.generateExportJson(it) },
                            onNavigateBack = { navController.popBackStack() },
                        )
                    }
                }

                composable<CompendiumRoute.RaceUpdateScreen> {
                    val viewModel = hiltViewModel<RaceUpdateViewModel>()

                    val race by viewModel.race.collectAsStateWithLifecycle()

                    LaunchedEffect(Unit) {
                        viewModel.updateDone.collect {
                            navController.popBackStack()
                        }
                    }

                    if (race == null) {
                        LoadingDialog()
                    } else {
                        RaceCreateScreen(
                            race = race,
                            onCreate = { raceState ->
                                viewModel.updateRace(raceState.toRace())
                            },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }

                composable<CompendiumRoute.RaceCreateScreen> {
                    val viewModel = hiltViewModel<RaceCreateViewModel>()

                    RaceCreateScreen(
                        race = null,
                        onCreate = { raceState ->
                            viewModel.createRace(raceState.toRace())
                            navController.popBackStack()
                        },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable<CompendiumRoute.ClassesScreen> {
                    val viewModel = hiltViewModel<CharacterClassesViewModel>()

                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                    val context = LocalContext.current
                    val res = LocalResources.current

                    ClassesScreen(
                        uiState = ClassesUiState(
                            origClasses = uiState.origClasses,
                            homebrewClasses = uiState.homebrewClasses,
                            selectedClassesIds = uiState.selectedClassesIds,
                            isSelectionMode = uiState.isSelectionMode,
                            isAllSelected = uiState.isAllSelected,
                            searchQuery = uiState.searchQuery,
                            origSectionState = uiState.origClassesSectionState,
                            homebrewSectionState = uiState.homebrewClassesSectionState
                        ),
                        actions = ClassesActions(
                            onSearchQueryChange = viewModel::onSearchQueryChange,
                            onClearSelection = viewModel::clearSelection,
                            onDeleteSelected = viewModel::deleteSelected,
                            onToggleSelectAll = viewModel::toggleSelectAll,
                            onToggleRaceSelection = viewModel::toggleClassSelection,
                            onExportRequested = viewModel::generateExportJson,
                            onExportAllSelected = { viewModel.generateExportJson(targetClassId = null) },
                            onImportRequested = { json ->
                                viewModel.importClassesFromJson(json) {
                                    Toast.makeText(
                                        context,
                                        res.getString(R.string.success_import),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            onDelete = viewModel::delete,
                            onEdit = {
                                navController.navigate(
                                    CompendiumRoute.ClassUpdateScreen(it)
                                )
                            },
                            onCreateClass = { navController.navigate(CompendiumRoute.ClassCreateScreen()) },
                            onDetails = {
                                navController.navigate(
                                    CompendiumRoute.ClassDetailsScreen(
                                        it
                                    )
                                )
                            },
                            onNavigateBack = { navController.popBackStack() },
                            onCollapseOrigSection = viewModel::collapseOrigClassesSection,
                            onCollapseHomebrewSection = viewModel::collapseHomebrewClassesSection,
                        )
                    )
                }

                composable<CompendiumRoute.ClassDetailsScreen> {
                    val viewModel = hiltViewModel<CharacterClassesViewModel>()

                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                    if (uiState.isLoading) {
                        LoadingDialog()
                    } else {
                        ClassDetailsScreen(
                            characterClass = uiState.selectedClass!!,
                            onEdit = { navController.navigate(CompendiumRoute.ClassUpdateScreen(it)) },
                            onExportRequested = { viewModel.generateExportJson(it) },
                            onNavigateBack = { navController.popBackStack() },
                        )
                    }
                }

                composable<CompendiumRoute.ClassCreateScreen> {
                    val viewModel = hiltViewModel<ClassCreateViewModel>()

                    ClassCreateScreen(
                        characterClass = null,
                        onCreate = { clsState ->
                            viewModel.createClass((clsState.toCharacterClass()))
                            navController.popBackStack()
                        },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable<CompendiumRoute.ClassUpdateScreen> {
                    val viewModel = hiltViewModel<ClassUpdateViewModel>()

                    val cls by viewModel.characterClass.collectAsStateWithLifecycle()

                    LaunchedEffect(Unit) {
                        viewModel.updateDone.collect {
                            navController.popBackStack()
                        }
                    }

                    if (cls == null) {
                        LoadingDialog()
                    } else {
                        ClassCreateScreen(
                            characterClass = cls,
                            onCreate = { clsState ->
                                viewModel.updateClass((clsState.toCharacterClass()))
                                navController.popBackStack()
                            },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}

private fun getTabIndex(route: String?): Int {
    if (route == null) return -1
    return when {
        route.contains("ListOfCharactersRoute") -> 0
        route.contains("GlobalSpellLibraryRoute") -> 1
        route.contains("AppSettingsRoute") -> 2
        else -> -1
    }
}