package com.yablonskyi.compendium.classes.viewmodel

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.yablonskyi.compendium.CompendiumClassDetailsRoute
import com.yablonskyi.domain.repository.ClassRepository
import com.yablonskyi.model.rulebook.CharacterClass
import com.yablonskyi.ui.R
import com.yablonskyi.ui.utils.FileOperationEffect
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class ClassDetailsViewModel @Inject constructor(
    repo: ClassRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val classId =
        savedStateHandle.toRoute<CompendiumClassDetailsRoute>().classId

    private val _effect = Channel<ClassDetailsEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private val _fileEffect = Channel<FileOperationEffect>(Channel.BUFFERED)
    val fileEffect = _fileEffect.receiveAsFlow()

    val uiState: StateFlow<ClassDetailsUiState> = repo.getClassById(classId)
        .map { cls -> ClassDetailsUiState(selectedClass = cls, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = ClassDetailsUiState(isLoading = true)
        )

    fun onIntent(intent: ClassDetailsIntent) {
        when (intent) {
            ClassDetailsIntent.Edit -> sendEffect(ClassDetailsEffect.NavigateToEdit(classId))
            ClassDetailsIntent.NavigateBack -> sendEffect(ClassDetailsEffect.NavigateBack)
            ClassDetailsIntent.ShareRequested -> shareRequested()
            ClassDetailsIntent.ShareFailed -> sendEffect(ClassDetailsEffect.ShowSnackbar(R.string.failure_share))
        }
    }

    private fun sendEffect(effect: ClassDetailsEffect) {
        viewModelScope.launch { _effect.send(effect) }
    }

    private fun shareRequested() {
        val cls = uiState.value.selectedClass ?: return
        viewModelScope.launch {
            _fileEffect.send(
                FileOperationEffect.ShareReady(
                    fileName = "${cls.name.replace(" ", "_")}.json",
                    json = Json.encodeToString(listOf(cls))
                )
            )
        }
    }
}

sealed interface ClassDetailsIntent {
    data object Edit : ClassDetailsIntent
    data object NavigateBack : ClassDetailsIntent
    data object ShareRequested : ClassDetailsIntent
    data object ShareFailed : ClassDetailsIntent
}

sealed interface ClassDetailsEffect {
    data object NavigateBack : ClassDetailsEffect
    data class NavigateToEdit(val classId: String) : ClassDetailsEffect
    data class ShowSnackbar(@param:StringRes val messageRes: Int) : ClassDetailsEffect
}

@Immutable
data class ClassDetailsUiState(
    val selectedClass: CharacterClass? = null,
    val isLoading: Boolean = false
)
