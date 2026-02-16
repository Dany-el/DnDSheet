package com.yablonskyi.dndsheet.ui.navigation

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.yablonskyi.dndsheet.R
import com.yablonskyi.dndsheet.data.model.character.Character
import com.yablonskyi.dndsheet.ui.attack.AttackViewModel
import com.yablonskyi.dndsheet.ui.character.CharacterDetailViewModel
import com.yablonskyi.dndsheet.ui.character.CharacterEditScreen
import com.yablonskyi.dndsheet.ui.character.CharacterListViewModel
import com.yablonskyi.dndsheet.ui.character.CharacterSettingsViewModel
import com.yablonskyi.dndsheet.ui.character.CharacterSheetScreen
import com.yablonskyi.dndsheet.ui.character.ListOfCharactersScreen
import com.yablonskyi.dndsheet.ui.dice.DiceViewModel
import com.yablonskyi.dndsheet.ui.settings.AppSettingsScreen
import com.yablonskyi.dndsheet.ui.spell.CharacterSpellLibraryViewModel
import com.yablonskyi.dndsheet.ui.spell.GlobalSpellLibraryViewModel
import com.yablonskyi.dndsheet.ui.spell.SpellEditScreen
import com.yablonskyi.dndsheet.ui.spell.SpellEditViewModel
import com.yablonskyi.dndsheet.ui.spell.SpellLibraryScreen
import com.yablonskyi.dndsheet.ui.spell.SpellViewModel

@Composable
fun DnDNavGraph(
    modifier: Modifier = Modifier,
    diceViewModel: DiceViewModel = viewModel(),
    navController: NavHostController = rememberNavController(),
) {
    val diceState by diceViewModel.diceRollState.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = ListOfCharactersRoute,
        modifier = modifier,
        enterTransition = {
            val initialIndex = getTabIndex(initialState.destination.route)
            val targetIndex = getTabIndex(targetState.destination.route)

            if (initialIndex != -1 && targetIndex != -1) {
                if (targetIndex > initialIndex) {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(400)
                    )
                } else {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right,
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
                if (targetIndex > initialIndex) {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(400)
                    )
                } else {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right,
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
            val characterListState by viewModel.characterListState.collectAsStateWithLifecycle()
            val lastCharacterId by viewModel.lastCreatedId.collectAsStateWithLifecycle()

            val isSelectionMode by viewModel.isSelectionMode.collectAsStateWithLifecycle()
            val selectedCharacters by viewModel.selectedCharacters.collectAsStateWithLifecycle()
            val isAllSelected by viewModel.isAllSelected.collectAsStateWithLifecycle()

            val defaultName = stringResource(R.string.unknown_character)

            LaunchedEffect(lastCharacterId) {
                lastCharacterId?.let {
                    navController.navigate(
                        CharacterSheetRoute(id = it)
                    )
                    viewModel.clearLastCreatedId()
                }
            }

            ListOfCharactersScreen(
                characters = characterListState.characters,
                loadingState = characterListState.isLoading,
                isSelectionMode = isSelectionMode,
                isAllSelected = isAllSelected,
                selectedCharacters = selectedCharacters,
                onClearSelection = viewModel::closeSelection,
                onToggleSelectAll = viewModel::toggleSelectAll,
                onDeleteSelected = viewModel::deleteSelectedCharacters,
                toggleSelection = viewModel::toggleSelection,
                onAdd = {
                    viewModel.createCharacter(Character(name = defaultName))
                },
                onDelete = viewModel::deleteCharacter,
                onImportSheets = viewModel::importSheets,
                onExportSheets = viewModel::getSheetsForExport,
                onCharacterClick = { charId ->
                    navController.navigate(
                        CharacterSheetRoute(id = charId)
                    )
                }
            )
        }

        composable<CharacterSheetRoute>(
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(400))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(400))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(400))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(400))
            }
        ) { backstackEntry ->
            val charViewModel: CharacterDetailViewModel = hiltViewModel()
            val spellViewModel: SpellViewModel = hiltViewModel()
            val attackViewModel: AttackViewModel = hiltViewModel()

            val character by charViewModel.character.collectAsStateWithLifecycle()
            val characterSpells by spellViewModel.spellList.collectAsStateWithLifecycle()
            val currentFilter by spellViewModel.currentFilter.collectAsStateWithLifecycle()
            val availableFilters by spellViewModel.availableFilters.collectAsStateWithLifecycle()
            val characterAttacks by attackViewModel.attackList.collectAsStateWithLifecycle()

            CharacterSheetScreen(
                character = character,
                spells = characterSpells,
                attacks = characterAttacks,
                currentFilter = currentFilter,
                diceState = diceState,
                availableFilters = availableFilters,

                onDiceButtonClick = diceViewModel::rollDiceFromString,
                onDiceClick = diceViewModel::rollDice,
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
            )
        }

        composable<CharacterSettingsRoute>(
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(400))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(400))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(400))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(400))
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

        composable<GlobalSpellLibraryRoute> {
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
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(400))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(400))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(400))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(400))
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

        composable<UpdateSpellRoute>(
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(400))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(400))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(400))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(400))
            }
        ) {
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
    }
}

// Helper function to determine the visual order of the tabs
private fun getTabIndex(route: String?): Int {
    if (route == null) return -1
    return when {
        route.contains("ListOfCharactersRoute") -> 0
        route.contains("GlobalSpellLibraryRoute") -> 1
        route.contains("AppSettingsRoute") -> 2
        else -> -1
    }
}