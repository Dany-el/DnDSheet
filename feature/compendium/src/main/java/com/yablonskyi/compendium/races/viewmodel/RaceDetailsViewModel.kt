package com.yablonskyi.compendium.races.viewmodel

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.yablonskyi.compendium.CompendiumRaceDetailsRoute
import com.yablonskyi.domain.repository.RaceRepository
import com.yablonskyi.model.rulebook.Race
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
class RaceDetailsViewModel @Inject constructor(
    repo: RaceRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val raceId =
        savedStateHandle.toRoute<CompendiumRaceDetailsRoute>().raceId

    private val _effect = Channel<RaceDetailsEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private val _fileEffect = Channel<FileOperationEffect>(Channel.BUFFERED)
    val fileEffect = _fileEffect.receiveAsFlow()

    val uiState: StateFlow<RaceDetailsUiState> = repo.getRaceById(raceId)
        .map { race -> RaceDetailsUiState(selectedRace = race, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = RaceDetailsUiState(isLoading = true)
        )

    fun onIntent(intent: RaceDetailsIntent) {
        when (intent) {
            RaceDetailsIntent.Edit -> sendEffect(RaceDetailsEffect.NavigateToEdit(raceId))
            RaceDetailsIntent.NavigateBack -> sendEffect(RaceDetailsEffect.NavigateBack)
            RaceDetailsIntent.ShareRequested -> shareRequested()
            RaceDetailsIntent.ShareFailed -> sendEffect(RaceDetailsEffect.ShowSnackbar(R.string.failure_share))
        }
    }

    private fun sendEffect(effect: RaceDetailsEffect) {
        viewModelScope.launch { _effect.send(effect) }
    }

    private fun shareRequested() {
        val race = uiState.value.selectedRace ?: return
        viewModelScope.launch {
            _fileEffect.send(
                FileOperationEffect.ShareReady(
                    fileName = "${race.name.replace(" ", "_")}.json",
                    json = Json.encodeToString(listOf(race))
                )
            )
        }
    }
}

sealed interface RaceDetailsIntent {
    data object Edit : RaceDetailsIntent
    data object NavigateBack : RaceDetailsIntent
    data object ShareRequested : RaceDetailsIntent
    data object ShareFailed : RaceDetailsIntent
}

sealed interface RaceDetailsEffect {
    data object NavigateBack : RaceDetailsEffect
    data class NavigateToEdit(val raceId: String) : RaceDetailsEffect
    data class ShowSnackbar(@param:StringRes val messageRes: Int) : RaceDetailsEffect
}

@Immutable
data class RaceDetailsUiState(
    val selectedRace: Race? = null,
    val isLoading: Boolean = false
)
