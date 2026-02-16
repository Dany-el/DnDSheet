package com.yablonskyi.dndsheet.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
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
import com.yablonskyi.dndsheet.ui.spell.SpellEditScreen
import com.yablonskyi.dndsheet.ui.spell.SpellEditViewModel
import com.yablonskyi.dndsheet.ui.spell.SpellLibraryScreen
import com.yablonskyi.dndsheet.ui.spell.SpellLibraryViewModel
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
        modifier = modifier
    ) {
        composable<ListOfCharactersRoute> {
            val charViewModel: CharacterListViewModel = hiltViewModel()
            val characters by charViewModel.characters.collectAsStateWithLifecycle()

            val defaultName = stringResource(R.string.unknown_character)

            ListOfCharactersScreen(
                characters = characters,
                onAdd = { charViewModel.createCharacter(Character(name = defaultName)) },
                onDelete = charViewModel::deleteCharacter,
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
                onManageClick = {
                    navController.navigate(
                        SpellLibraryRoute(it)
                    )
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
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

            if (character != null) {
                CharacterEditScreen(
                    character = character!!,
                    onUpdate = charViewModel::updateCharacter,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

        }

        composable<SpellLibraryRoute> {
            val spellLibraryViewModel: SpellLibraryViewModel = hiltViewModel()

            val spells by spellLibraryViewModel.spellLibraryList.collectAsStateWithLifecycle()
            val searchQuery by spellLibraryViewModel.searchQuery.collectAsStateWithLifecycle()
            val currentFilter by spellLibraryViewModel.currentFilter.collectAsStateWithLifecycle()
            val availableFilters by spellLibraryViewModel.availableFilters.collectAsStateWithLifecycle()

            SpellLibraryScreen(
                spells = spells,
                searchQuery = searchQuery,
                currentFilter = currentFilter,
                availableFilters = availableFilters,
                isSelectionMode = spellLibraryViewModel.isSelectionMode,

                onNavigateBack = { navController.popBackStack() },
                onAddSpell = { navController.navigate(UpdateSpellRoute(0)) },
                onEditSpell = { spellId -> navController.navigate(UpdateSpellRoute(spellId)) },

                onSearchQueryChange = spellLibraryViewModel::onSearchQueryChange,
                onFilterChange = spellLibraryViewModel::setFilter,
                onToggleSpell = spellLibraryViewModel::toggleSpellSelection,
                onDeleteSpell = spellLibraryViewModel::deleteSpellGlobally
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

            if (spellState != null) {
                SpellEditScreen(
                    spell = spellState!!,
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