package com.yablonskyi.wizard.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalResources
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.yablonskyi.wizard.CharacterCreationWizardScreen
import com.yablonskyi.wizard.viewmodel.CharacterCreationWizardViewModel
import com.yablonskyi.wizard.viewmodel.WizardEffect

fun NavGraphBuilder.wizardGraph(
    onCharacterCreated: (Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    composable<CharacterCreationWizardRoute> {
        val viewModel = hiltViewModel<CharacterCreationWizardViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val snackbarHostState = remember { SnackbarHostState() }
        val resources = LocalResources.current
        val lifecycleOwner = LocalLifecycleOwner.current
        LaunchedEffect(viewModel, lifecycleOwner, onCharacterCreated, onNavigateBack, resources) {
            viewModel.effect
                .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collect { effect ->
                    when (effect) {
                        WizardEffect.NavigateBack -> onNavigateBack()
                        is WizardEffect.CharacterCreated -> onCharacterCreated(effect.id)
                        is WizardEffect.ShowSnackbar ->
                            snackbarHostState.showSnackbar(resources.getString(effect.messageRes))
                    }
                }
        }
        CharacterCreationWizardScreen(uiState, viewModel::onIntent, snackbarHostState)
    }
}