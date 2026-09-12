package com.yablonskyi.character.navigation

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.State
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CancellationException
import com.yablonskyi.character.CharacterPrintEffect
import com.yablonskyi.character.CharacterPrintError
import com.yablonskyi.character.CharacterPrintState
import com.yablonskyi.ui.R
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.yablonskyi.character.CharacterDetailViewModel
import com.yablonskyi.character.CharacterSettingsScreen
import com.yablonskyi.character.CharacterListActions
import com.yablonskyi.character.CharacterListUiState
import com.yablonskyi.character.CharacterListViewModel
import com.yablonskyi.character.CharacterSettingsViewModel
import com.yablonskyi.character.CharacterSheetActions
import com.yablonskyi.character.CharacterSheetScreen
import com.yablonskyi.character.CharacterSheetUiState
import com.yablonskyi.character.ListOfCharactersScreen
import com.yablonskyi.character.SpellViewModel
import com.yablonskyi.character.attack.AttackViewModel
import com.yablonskyi.dice.DiceIntent
import com.yablonskyi.dice.DiceViewModel
import com.yablonskyi.navigation.CharacterSheetsRoute
import com.yablonskyi.ui.settings.ListView

fun NavGraphBuilder.characterGraph(
    sharedTransitionScope: SharedTransitionScope,
    listViewState: State<ListView>,
    onCreateCharacter: () -> Unit,
    onOpenCharacter: (Long) -> Unit,
    onOpenCharacterSpells: (Long) -> Unit,
    onOpenSettings: (Long) -> Unit,
    onToggleListView: () -> Unit,
    onBack: () -> Unit,
    onPrintCharacterSheet: suspend (String, String) -> Result<Unit>,
) {
    composable<CharacterSheetsRoute> {
        val listViewModel = hiltViewModel<CharacterListViewModel>()
        val characterListState by listViewModel.characterListState.collectAsStateWithLifecycle()
        val searchQuery by listViewModel.searchQuery.collectAsStateWithLifecycle()
        val isSelectionMode by listViewModel.isSelectionMode.collectAsStateWithLifecycle()
        val isAllSelected by listViewModel.isAllSelected.collectAsStateWithLifecycle()
        val selectedCharacters by listViewModel.selectedCharacters.collectAsStateWithLifecycle()
        val printState by listViewModel.printState.collectAsStateWithLifecycle()
        val printCallback by rememberUpdatedState(onPrintCharacterSheet)
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        LaunchedEffect(listViewModel, lifecycleOwner) {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                listViewModel.printEffects.collect { effect ->
                    when (effect) {
                        is CharacterPrintEffect.LaunchPrint -> {
                            if (listViewModel.claimPrintRequest(effect.requestId)) {
                                try {
                                    val result = printCallback(effect.html, effect.jobName)
                                    listViewModel.onPrintLaunchResult(effect.requestId, result)
                                } catch (cancelled: CancellationException) {
                                    throw cancelled
                                } catch (error: Exception) {
                                    listViewModel.onPrintLaunchResult(effect.requestId, Result.failure(error))
                                } finally {
                                    listViewModel.onPrintLaunchCancelled(effect.requestId)
                                }
                            }
                        }
                        is CharacterPrintEffect.Failed -> {
                            val message = when (effect.error) {
                                CharacterPrintError.LOAD_FAILED -> R.string.print_load_failed
                                CharacterPrintError.RENDER_FAILED -> R.string.print_render_failed
                                CharacterPrintError.PRINT_LAUNCH_FAILED -> R.string.print_launch_failed
                            }
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        }
                        CharacterPrintEffect.PrintRequestAccepted ->
                            Toast.makeText(context, R.string.print_request_accepted, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        with(sharedTransitionScope) {
            ListOfCharactersScreen(
                uiState = CharacterListUiState(
                    characters = characterListState.characters,
                    loadingState = characterListState.isLoading,
                    isPrinting = printState != CharacterPrintState.Idle,
                    listView = listViewState.value,
                    searchQuery = searchQuery,
                    isSelectionMode = isSelectionMode,
                    isAllSelected = isAllSelected,
                    selectedCharacters = selectedCharacters,
                    windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
                ),
                actions = CharacterListActions(
                    onSearchQueryChange = listViewModel::updateSearchQuery,
                    onToggleListView = onToggleListView,
                    onClearSelection = listViewModel::closeSelection,
                    onDeleteSelected = listViewModel::deleteSelectedCharacters,
                    onToggleSelectAll = listViewModel::toggleSelectAll,
                    toggleSelection = listViewModel::toggleSelection,
                    onAdd = onCreateCharacter,
                    onDelete = listViewModel::deleteCharacter,
                    onCharacterClick = onOpenCharacter,
                    onImportSheets = listViewModel::importSheets,
                    onExportSheets = listViewModel::getSheetsForExport,
                    onPrintCharacterSheet = listViewModel::prepareCharacterSheetPrint,
                ),
                animatedVisibilityScope = this@composable
            )
        }
    }

    composable<CharacterSheetRoute> {
        val charViewModel = hiltViewModel<CharacterDetailViewModel>()
        val spellViewModel = hiltViewModel<SpellViewModel>()
        val attackViewModel = hiltViewModel<AttackViewModel>()
        val diceViewModel = hiltViewModel<DiceViewModel>()

        val character by charViewModel.character.collectAsStateWithLifecycle()
        val spells by spellViewModel.spellList.collectAsStateWithLifecycle()
        val currentFilter by spellViewModel.currentFilter.collectAsStateWithLifecycle()
        val availableFilters by spellViewModel.availableFilters.collectAsStateWithLifecycle()
        val attacks by attackViewModel.attackList.collectAsStateWithLifecycle()
        val diceState by diceViewModel.diceRollState.collectAsStateWithLifecycle()
        val lessDetails by charViewModel.lessDetails.collectAsStateWithLifecycle()
        val leftSelectedTab by charViewModel.leftSelectedTab.collectAsStateWithLifecycle()
        val rightSelectedTab by charViewModel.rightSelectedTab.collectAsStateWithLifecycle()

        with(sharedTransitionScope) {
            CharacterSheetScreen(
                uiState = CharacterSheetUiState(
                    character = character,
                    spells = spells,
                    attacks = attacks,
                    currentFilter = currentFilter,
                    diceState = diceState,
                    availableFilters = availableFilters,
                    lessDetails = lessDetails,
                    rightSelectedTab = rightSelectedTab,
                    leftSelectedTab = leftSelectedTab
                ),
                actions = CharacterSheetActions(
                    onDiceButtonClick = { notation ->
                        diceViewModel.onIntent(DiceIntent.RegularStringRoll(notation))
                    },
                    onDiceClick = { diceMap ->
                        diceViewModel.onIntent(DiceIntent.RegularRoll(diceMap))
                    },
                    onPinClick = { diceViewModel.onIntent(DiceIntent.PinResult) },
                    onDismissResult = { diceViewModel.onIntent(DiceIntent.DismissResult) },
                    onUpdateCharacter = charViewModel::updateCharacter,
                    onFilterChange = spellViewModel::setFilter,
                    updateAbility = charViewModel::updateAbility,
                    updateProfLevel = charViewModel::updateSkillProficiency,
                    updateSavingThrowProficiency = charViewModel::updateSavingThrowProf,
                    saveAttack = attackViewModel::saveAttack,
                    deleteAttack = attackViewModel::deleteAttack,
                    onSettingsNavigate = onOpenSettings,
                    onNavigateBack = onBack,
                    onManageClick = onOpenCharacterSpells,
                    onSlotClick = charViewModel::useSpellSlot,
                    onRestClick = charViewModel::performLongRest,
                    onLessDetails = charViewModel::toggleDetails,
                    onLeftTabSelected = charViewModel::onLeftTabSelected,
                    onRightTabSelected = charViewModel::onRightTabSelected
                ),
                animatedVisibilityScope = this@composable
            )
        }
    }

    composable<CharacterSettingsRoute> {
        val viewModel = hiltViewModel<CharacterSettingsViewModel>()
        val character by viewModel.character.collectAsStateWithLifecycle()

        character?.let { char ->
            CharacterSettingsScreen(
                character = char,
                onUpdate = viewModel::updateCharacter,
                onSpellSlotsUpdate = viewModel::updateSpellSlot,
                onImageUpdated = viewModel::updateImage,
                onNavigateBack = onBack
            )
        }
    }
}
