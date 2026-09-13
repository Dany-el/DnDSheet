package com.yablonskyi.characterspells.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.yablonskyi.characterspells.viewmodel.CharacterSpellsLibraryViewModel
import com.yablonskyi.ui.spell.SpellLibraryScreen
import com.yablonskyi.ui.spell.SpellsEffect

fun NavGraphBuilder.characterSpellsGraph(
    onBack: () -> Unit
) {
    composable<CharacterSpellsRoute> {
        val viewModel = hiltViewModel<CharacterSpellsLibraryViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val snackbarHostState = remember { SnackbarHostState() }

        val lifecycleOwner = LocalLifecycleOwner.current
        LaunchedEffect(lifecycleOwner) {
            viewModel.effect
                .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collect { effect ->
                    when (effect) {
                        SpellsEffect.NavigateBack -> onBack()
                        else -> Unit
                    }
                }
        }

        SpellLibraryScreen(
            uiState = uiState,
            snackbarHostState = snackbarHostState,
            onIntent = viewModel::onIntent
        )
    }
}