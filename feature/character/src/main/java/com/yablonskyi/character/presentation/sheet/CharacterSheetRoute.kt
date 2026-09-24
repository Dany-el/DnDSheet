package com.yablonskyi.character.presentation.sheet

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.*
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.yablonskyi.character.presentation.common.CharacterStatusContent
import com.yablonskyi.dice.DiceViewModel

@Composable
fun CharacterSheetRouteContent(
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onOpenCharacterSpells: (Long) -> Unit,
    onOpenSettings: (Long) -> Unit,
    onOpenDiceHistory: (Long) -> Unit,
    onOpenNote: (Long, String?) -> Unit,
    onBack: () -> Unit,
    viewModel: CharacterSheetViewModel = hiltViewModel(),
    diceViewModel: DiceViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val dice by diceViewModel.diceRollState.collectAsStateWithLifecycle()
//    val dicePersistence by diceViewModel.persistenceState.collectAsStateWithLifecycle()
    val settings by rememberUpdatedState(onOpenSettings)
    val spells by rememberUpdatedState(onOpenCharacterSpells)
    val back by rememberUpdatedState(onBack)
    val history by rememberUpdatedState(onOpenDiceHistory)
    val openNote by rememberUpdatedState(onOpenNote)
    val owner = LocalLifecycleOwner.current
    val characterId = state.character?.id
    LaunchedEffect(diceViewModel, characterId) {
        characterId?.let(diceViewModel::observeHistory)
    }
    LaunchedEffect(viewModel, owner) {
        owner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    is CharacterSheetEffect.OpenSettings -> settings(effect.id)
                    is CharacterSheetEffect.ManageSpells -> spells(effect.id)
                    is CharacterSheetEffect.OpenDiceHistory -> history(effect.characterId)
                    CharacterSheetEffect.Back -> back()
                }
            }
        }
    }
    CharacterStatusContent(state.status, state.errors,
        onRetry = { viewModel.onIntent(CharacterSheetIntent.Retry) },
        onDismissError = { viewModel.onIntent(CharacterSheetIntent.DismissError) },
        onBack = { viewModel.onIntent(CharacterSheetIntent.BackClicked) },
    ) {
        with(sharedTransitionScope) {
            CharacterSheetScreen(
                state,
                viewModel::onIntent,
                dice,
                onDiceIntent = { intent ->
                    characterId?.let { diceViewModel.onIntent(it, intent) }
                },
                onOpenNote = openNote,
                animatedVisibilityScope = animatedVisibilityScope,
            )
        }
    }
}
