package com.yablonskyi.character.presentation.notes

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle

@Composable
fun NoteEditorRouteContent(
    onBack: () -> Unit,
    viewModel: NoteEditorViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentBack by rememberUpdatedState(onBack)
    val owner = LocalLifecycleOwner.current

    PredictiveBackHandler(enabled = true) { progress ->
        progress.collect { }
        viewModel.backRequested()
    }
    DisposableEffect(owner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) viewModel.flushDraft()
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(viewModel, owner) {
        owner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                if (effect == NoteEditorEffect.BACK) currentBack()
            }
        }
    }
    NoteEditorScreen(
        state = state,
        onBack = viewModel::backRequested,
        onTopicChanged = viewModel::topicChanged,
        onBodyChanged = viewModel::bodyChanged,
        onSave = viewModel::save,
        onDeleteRequested = viewModel::deleteRequested,
        onDeleteDismissed = viewModel::dismissDelete,
        onDeleteConfirmed = viewModel::delete,
        onDiscardDismissed = viewModel::dismissDiscard,
        onDiscardConfirmed = viewModel::discard,
    )
}