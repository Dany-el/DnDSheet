package com.yablonskyi.dndsheet.ui.compendium.races

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.yablonskyi.dndsheet.data.model.rulebook.Race
import com.yablonskyi.dndsheet.domain.repository.RaceRepository
import com.yablonskyi.dndsheet.ui.navigation.CompendiumRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class RacesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: RaceRepository,
) : ViewModel() {

    private val raceId = savedStateHandle.toRoute<CompendiumRoute.RaceDetailsScreen>().raceId

    private val _internalState = MutableStateFlow(InternalUiState())

    val uiState: StateFlow<RaceUiState> = combine(
        repo.getAllRaces(),
        _internalState
    ) { allRaces, internal ->
        val (homebrew, original) = allRaces.partition { it.isHomebrew }

        val query = _internalState.value.searchQuery
        val filteredOriginal = if (query.isBlank()) original
        else original.filter { it.name.contains(query, ignoreCase = true) }
        val filteredHomebrew = if (query.isBlank()) homebrew
        else homebrew.filter { it.name.contains(query, ignoreCase = true) }

        val selectedDetails =
            allRaces.find { it.id.trim().equals(raceId.trim(), ignoreCase = true) }

        val isAllSelected = homebrew.isNotEmpty() &&
                internal.selectedRaceIds.containsAll(homebrew.map { it.id })

        RaceUiState(
            origRaces = filteredOriginal,
            homebrewRaces = filteredHomebrew,
            selectedRace = selectedDetails,
            selectedRaceIds = internal.selectedRaceIds,
            isSelectionMode = internal.selectedRaceIds.isNotEmpty(),
            isAllSelected = isAllSelected,
            searchQuery = internal.searchQuery,
            origRacesSectionState = CollapsibleSectionState(internal.isOrigExpanded),
            homebrewRacesSectionState = CollapsibleSectionState(internal.isHomebrewExpanded),
            errorMessage = internal.errorMessage,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = RaceUiState(isLoading = true)
    )

    // Sections
    fun collapseOrigRacesSection() {
        _internalState.value = _internalState.value.copy(
            isOrigExpanded = !_internalState.value.isOrigExpanded
        )
    }

    fun collapseHomebrewRacesSection() {
        _internalState.value = _internalState.value.copy(
            isHomebrewExpanded = !_internalState.value.isHomebrewExpanded
        )
    }

    // Search
    fun onSearchQueryChange(query: String) {
        _internalState.update { it.copy(searchQuery = query) }
    }

    // Selection
    fun toggleRaceSelection(raceId: String) {
        _internalState.value = _internalState.value.copy(
            selectedRaceIds = _internalState.value.selectedRaceIds.toMutableSet().apply {
                if (!add(raceId)) remove(raceId)
            }
        )
    }

    fun clearSelection() {
        _internalState.update { it.copy(selectedRaceIds = emptySet()) }
    }

    fun toggleSelectAll() {
        val allHomebrewIds = uiState.value.let { state ->
            (state.homebrewRaces + uiState.value.homebrewRaces)
                .distinctBy { it.id }
                .map { it.id }
                .toSet()
        }
        _internalState.update { internal ->
            val newIds = if (internal.selectedRaceIds.containsAll(allHomebrewIds)) emptySet()
            else allHomebrewIds

            internal.copy(selectedRaceIds = newIds)
        }
    }


    fun delete(race: Race) {
        viewModelScope.launch { repo.delete(race) }
    }

    fun deleteSelected() {
        val idsToDelete = _internalState.value.selectedRaceIds
        viewModelScope.launch {
            uiState.value.homebrewRaces
                .filter { it.id in idsToDelete }
                .forEach { repo.delete(it) }
            clearSelection()
        }
    }

    // Import/Export
    fun generateExportJson(targetRaceId: String? = null): String? {
        val currentState = uiState.value

        val racesToExport = if (targetRaceId != null) {
            currentState.homebrewRaces.filter {
                it.id.trim().equals(targetRaceId.trim(), ignoreCase = true)
            }
        } else {
            currentState.homebrewRaces.filter { currentState.selectedRaceIds.contains(it.id) }
        }

        if (racesToExport.isEmpty()) return null

        return Json.encodeToString(racesToExport)
    }

    fun importRacesFromJson(jsonString: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val importedRaces = Json.decodeFromString<List<Race>>(jsonString)
                val preparedRaces = importedRaces.map { it.copy(isHomebrew = true) }

                repo.insertAll(preparedRaces)
                onComplete()
            } catch (_: Exception) {
                _internalState.value = _internalState.value.copy(
                    errorMessage = "Failed to parse import file."
                )
            }
        }
    }
}

@Stable
class CollapsibleSectionState(initialExpanded: Boolean = true) {
    var isExpanded by mutableStateOf(initialExpanded)
}

private data class InternalUiState(
    val selectedRaceIds: Set<String> = emptySet(),
    val searchQuery: String = "",
    val isOrigExpanded: Boolean = true,
    val isHomebrewExpanded: Boolean = true,
    val errorMessage: String? = null
)

data class RaceUiState(
    val origRaces: List<Race> = emptyList(),
    val homebrewRaces: List<Race> = emptyList(),
    val selectedRace: Race? = null,
    val selectedRaceIds: Set<String> = emptySet(),
    val isSelectionMode: Boolean = false,
    val isAllSelected: Boolean = false,
    val searchQuery: String = "",
    val origRacesSectionState: CollapsibleSectionState = CollapsibleSectionState(initialExpanded = true),
    val homebrewRacesSectionState: CollapsibleSectionState = CollapsibleSectionState(initialExpanded = true),
    val errorMessage: String? = null,
    val isLoading: Boolean = true
)