package com.yablonskyi.character.presentation.settings

import com.yablonskyi.character.platform.image.rememberCharacterImageLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.yablonskyi.character.presentation.common.CharacterStatusContent
import com.yablonskyi.ui.utils.LoadingDialog

@Composable
fun CharacterSettingsRouteContent(
    onBack: () -> Unit,
    viewModel: CharacterSettingsViewModel = hiltViewModel(),
    formViewModel: CharacterSettingsFormViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val form by formViewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.character) { state.character?.let { formViewModel.onIntent(CharacterSettingsFormIntent.Synchronize(it)) } }
    LaunchedEffect(state.formWriteResults) { state.formWriteResults.values.sortedBy { it.write.revision }.forEach { formViewModel.onIntent(CharacterSettingsFormIntent.WriteFinished(it)) } }
    LaunchedEffect(formViewModel, viewModel) {
        formViewModel.effects.collect { effect -> when (effect) {
            is CharacterSettingsFormEffect.ChangeRequested -> viewModel.onIntent(CharacterSettingsIntent.Change(effect.change, effect.write))
        } }
    }
    val back by rememberUpdatedState(onBack)
    val picker = rememberCharacterImageLauncher {
        viewModel.onIntent(CharacterSettingsIntent.ImageSelected(it))
    }
    val owner = LocalLifecycleOwner.current
    LaunchedEffect(viewModel, owner) {
        owner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    CharacterSettingsEffect.Back -> back()
                    CharacterSettingsEffect.LaunchImagePicker -> picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            }
        }
    }
    CharacterStatusContent(state.status, state.errors,
        onRetry = { viewModel.onIntent(CharacterSettingsIntent.Retry) },
        onDismissError = { viewModel.onIntent(CharacterSettingsIntent.DismissError) },
        onBack = { viewModel.onIntent(CharacterSettingsIntent.BackClicked) },
    ) { CharacterSettingsScreen(form = form, onIntent = viewModel::onIntent, onFormIntent = formViewModel::onIntent) }
    if (state.isSavingImage && state.errors.isEmpty()) LoadingDialog()
}
