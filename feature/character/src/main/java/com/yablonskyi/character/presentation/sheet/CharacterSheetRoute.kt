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
    onBack: () -> Unit,
    viewModel: CharacterSheetViewModel = hiltViewModel(),
    diceViewModel: DiceViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val dice by diceViewModel.diceRollState.collectAsStateWithLifecycle()
    val settings by rememberUpdatedState(onOpenSettings)
    val spells by rememberUpdatedState(onOpenCharacterSpells)
    val back by rememberUpdatedState(onBack)
    val owner = LocalLifecycleOwner.current
    LaunchedEffect(viewModel, owner) {
        owner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    is CharacterSheetEffect.OpenSettings -> settings(effect.id)
                    is CharacterSheetEffect.ManageSpells -> spells(effect.id)
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
            CharacterSheetScreen(state, viewModel::onIntent, dice, diceViewModel::onIntent, animatedVisibilityScope)
        }
    }
}
