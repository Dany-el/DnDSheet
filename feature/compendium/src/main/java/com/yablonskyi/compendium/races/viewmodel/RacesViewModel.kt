package com.yablonskyi.compendium.races.viewmodel

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yablonskyi.domain.repository.RaceRepository
import com.yablonskyi.model.rulebook.Race
import com.yablonskyi.ui.R
import com.yablonskyi.ui.utils.FileOperationEffect
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class RacesViewModel @Inject constructor(
    private val repo: RaceRepository,
) : ViewModel() {
    private val _effect = Channel<RacesEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private val _fileEffect = Channel<FileOperationEffect>(Channel.BUFFERED)
    val fileEffect = _fileEffect.receiveAsFlow()

    private val _internalState = MutableStateFlow(InternalUiState())

    private val raceContent = combine(
        repo.getAllRaces(),
        _internalState.map { it.searchQuery }.distinctUntilChanged()
    ) { allRaces, query ->
        val (homebrew, original) = allRaces.partition { it.isHomebrew }
        val filteredOriginal = if (query.isBlank()) original
        else original.filter { it.name.contains(query, ignoreCase = true) }
        val filteredHomebrew = if (query.isBlank()) homebrew
        else homebrew.filter { it.name.contains(query, ignoreCase = true) }

        RaceContent(
            original = filteredOriginal,
            homebrew = filteredHomebrew
        )
    }

    val uiState: StateFlow<RaceUiState> = combine(
        raceContent,
        _internalState
    ) { content, internal ->
        RaceUiState(
            origRaces = content.original,
            homebrewRaces = content.homebrew,
            selectedRaceIds = internal.selectedRaceIds,
            isSelectionMode = internal.isSelectionMode,
            isAllSelected = content.homebrewIds.isNotEmpty() &&
                    internal.selectedRaceIds.containsAll(content.homebrewIds),
            searchQuery = internal.searchQuery,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = RaceUiState(isLoading = true)
    )

    fun onIntent(intent: RacesIntent) {
        when (intent) {
            is RacesIntent.SearchQueryChanged -> onSearchQueryChange(intent.value)
            is RacesIntent.ToggleSelection -> toggleRaceSelection(intent.raceId)
            is RacesIntent.ShareRequested -> shareRequested(intent.raceId)
            is RacesIntent.ImportRequested -> importRacesFromJson(intent.json)
            is RacesIntent.Delete -> delete(intent.race)
            is RacesIntent.Edit -> sendEffect(RacesEffect.NavigateToEdit(intent.raceId))
            is RacesIntent.Details -> sendEffect(RacesEffect.NavigateToDetails(intent.raceId))
            RacesIntent.EnterSelectionMode -> _internalState.update { it.copy(isSelectionMode = true, selectedRaceIds = emptySet()) }
            RacesIntent.ClearSelection -> clearSelection()
            RacesIntent.DeleteSelected -> deleteSelected()
            RacesIntent.ToggleSelectAll -> toggleSelectAll()
            RacesIntent.ExportAllSelected -> exportAllSelected()
            RacesIntent.RequestFilePicker -> requestFilePicker()
            RacesIntent.ShareFailed -> sendEffect(RacesEffect.ShowSnackbar(R.string.failure_share))
            is RacesIntent.ExportCompleted -> onExportCompleted(intent.success)
            RacesIntent.CreateRace -> sendEffect(RacesEffect.NavigateToCreate)
            RacesIntent.NavigateBack -> sendEffect(RacesEffect.NavigateBack)
        }
    }

    private fun sendEffect(effect: RacesEffect) {
        viewModelScope.launch { _effect.send(effect) }
    }

    private fun onSearchQueryChange(query: String) {
        _internalState.update { it.copy(searchQuery = query) }
    }

    private fun toggleRaceSelection(raceId: String) {
        _internalState.value = _internalState.value.copy(
            isSelectionMode = true,
            selectedRaceIds = _internalState.value.selectedRaceIds.toMutableSet().apply {
                if (!add(raceId)) remove(raceId)
            }
        )
    }

    private fun clearSelection() {
        _internalState.update { it.copy(selectedRaceIds = emptySet(), isSelectionMode = false) }
    }

    private fun toggleSelectAll() {
        val allHomebrewIds = uiState.value.homebrewRaces.map { it.id }.toSet()
        _internalState.update { internal ->
            val newIds = if (internal.selectedRaceIds.containsAll(allHomebrewIds)) internal.selectedRaceIds - allHomebrewIds
            else internal.selectedRaceIds + allHomebrewIds

            internal.copy(selectedRaceIds = newIds, isSelectionMode = true)
        }
    }

    private fun delete(race: Race) {
        viewModelScope.launch { repo.delete(race) }
    }

    private fun deleteSelected() {
        val idsToDelete = _internalState.value.selectedRaceIds
        viewModelScope.launch {
            repo.getAllRaces().first().filter { it.isHomebrew }
                .asSequence()
                .filter { it.id in idsToDelete }
                .forEach { repo.delete(it) }
            clearSelection()
        }
    }


    // ── Import/Export ─────────────────────────────────────────────────────────

    private fun shareRequested(raceId: String) {
        val race = uiState.value.homebrewRaces.firstOrNull {
            it.id.trim().equals(raceId.trim(), ignoreCase = true)
        } ?: return
        viewModelScope.launch {
            _fileEffect.send(
                FileOperationEffect.ShareReady(
                    fileName = "${race.name.replace(" ", "_")}.json",
                    json = Json.encodeToString(listOf(race))
                )
            )
        }
    }

    private fun exportAllSelected() {
        val selectedIds = _internalState.value.selectedRaceIds
        if (selectedIds.isEmpty()) return
        viewModelScope.launch {
            val selected = repo.getAllRaces().first().filter { it.isHomebrew && it.id in selectedIds }
            if (selected.isEmpty()) return@launch
            _fileEffect.send(
                FileOperationEffect.ExportReady(
                    fileName = "races_backup.json",
                    json = Json.encodeToString(selected)
                )
            )
        }
    }

    private fun requestFilePicker() {
        viewModelScope.launch { _fileEffect.send(FileOperationEffect.OpenImportPicker) }
    }

    private fun onExportCompleted(success: Boolean) {
        if (success) clearSelection()
        sendEffect(
            if (success) RacesEffect.ShowSnackbar(R.string.success_export)
            else RacesEffect.ShowSnackbar(R.string.failure_export)
        )
    }

    private fun importRacesFromJson(jsonString: String) {
        viewModelScope.launch {
            try {
                val importedRaces = Json.decodeFromString<List<Race>>(jsonString)
                if (importedRaces.isEmpty()) {
                    sendEffect(RacesEffect.ShowSnackbar(R.string.import_file_empty))
                } else {
                    val preparedRaces = importedRaces.map { it.copy(isHomebrew = true) }
                    repo.insertAll(preparedRaces)
                    sendEffect(RacesEffect.ShowSnackbar(R.string.success_import))
                }
            } catch (_: Exception) {
                sendEffect(RacesEffect.ShowSnackbar(R.string.failure_import))
            }
        }
    }

    private data class InternalUiState(
        val isSelectionMode: Boolean = false,
        val selectedRaceIds: Set<String> = emptySet(),
        val searchQuery: String = "",
    )

    private data class RaceContent(
        val original: List<Race>,
        val homebrew: List<Race>
    ) {
        val homebrewIds: Set<String> = homebrew.mapTo(mutableSetOf()) { it.id }
    }
}

sealed interface RacesIntent {
    data class SearchQueryChanged(val value: String) : RacesIntent
    data object EnterSelectionMode : RacesIntent
    data object ClearSelection : RacesIntent
    data object DeleteSelected : RacesIntent
    data object ToggleSelectAll : RacesIntent
    data class ToggleSelection(val raceId: String) : RacesIntent
    data class ShareRequested(val raceId: String) : RacesIntent
    data object ShareFailed : RacesIntent
    data object ExportAllSelected : RacesIntent
    data object RequestFilePicker : RacesIntent
    data class ExportCompleted(val success: Boolean) : RacesIntent
    data class ImportRequested(val json: String) : RacesIntent
    data class Delete(val race: Race) : RacesIntent
    data object CreateRace : RacesIntent
    data class Edit(val raceId: String) : RacesIntent
    data class Details(val raceId: String) : RacesIntent
    data object NavigateBack : RacesIntent
}

sealed interface RacesEffect {
    data object NavigateBack : RacesEffect
    data object NavigateToCreate : RacesEffect
    data class NavigateToEdit(val raceId: String) : RacesEffect
    data class NavigateToDetails(val raceId: String) : RacesEffect
    data class ShowSnackbar(@param:StringRes val messageRes: Int) : RacesEffect
}

@Immutable
data class RaceUiState(
    val origRaces: List<Race> = emptyList(),
    val homebrewRaces: List<Race> = emptyList(),
    val selectedRaceIds: Set<String> = emptySet(),
    val isSelectionMode: Boolean = false,
    val isAllSelected: Boolean = false,
    val searchQuery: String = "",
    val isLoading: Boolean = true
)
