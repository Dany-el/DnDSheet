package com.yablonskyi.dndsheet.ui.compendium.classes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.yablonskyi.dndsheet.data.model.rulebook.CharacterClass
import com.yablonskyi.dndsheet.domain.repository.ClassRepository
import com.yablonskyi.dndsheet.ui.compendium.races.CollapsibleSectionState
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
class CharacterClassesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: ClassRepository,
) : ViewModel() {

    private val classId =
        savedStateHandle.toRoute<CompendiumRoute.ClassDetailsScreen>().classId

    private val _internalState = MutableStateFlow(InternalUiState())

    val uiState: StateFlow<ClassUiState> = combine(
        repo.getAllClasses(),
        _internalState
    ) { allClasses, internal ->
        val (homebrew, original) = allClasses.partition { it.isHomebrew }

        val query = internal.searchQuery
        val filteredOriginal = if (query.isBlank()) original
        else original.filter { it.name.contains(query, ignoreCase = true) }
        val filteredHomebrew = if (query.isBlank()) homebrew
        else homebrew.filter { it.name.contains(query, ignoreCase = true) }

        val selectedDetails =
            allClasses.find { it.id.trim().equals(classId.trim(), ignoreCase = true) }

        val isAllSelected = homebrew.isNotEmpty() &&
                internal.selectedClassIds.containsAll(homebrew.map { it.id })

        ClassUiState(
            origClasses = filteredOriginal,
            homebrewClasses = filteredHomebrew,
            selectedClass = selectedDetails,
            selectedClassesIds = internal.selectedClassIds,
            isSelectionMode = internal.selectedClassIds.isNotEmpty(),
            isAllSelected = isAllSelected,
            searchQuery = internal.searchQuery,
            origClassesSectionState = CollapsibleSectionState(internal.isOrigExpanded),
            homebrewClassesSectionState = CollapsibleSectionState(internal.isHomebrewExpanded),
            errorMessage = internal.errorMessage,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = ClassUiState(isLoading = true)
    )

    fun collapseOrigClassesSection() {
        _internalState.value = _internalState.value.copy(
            isOrigExpanded = !_internalState.value.isOrigExpanded
        )
    }

    fun collapseHomebrewClassesSection() {
        _internalState.value = _internalState.value.copy(
            isHomebrewExpanded = !_internalState.value.isHomebrewExpanded
        )
    }

    fun onSearchQueryChange(query: String) {
        _internalState.update { it.copy(searchQuery = query) }
    }

    fun toggleClassSelection(classId: String) {
        _internalState.value = _internalState.value.copy(
            selectedClassIds = _internalState.value.selectedClassIds.toMutableSet().apply {
                if (!add(classId)) remove(classId)
            }
        )
    }

    fun clearSelection() {
        _internalState.update { it.copy(selectedClassIds = emptySet()) }
    }

    fun toggleSelectAll() {
        val allHomebrewIds = uiState.value.homebrewClasses.map { it.id }.toSet()
        _internalState.update { internal ->
            val newIds = if (internal.selectedClassIds.containsAll(allHomebrewIds)) emptySet()
            else allHomebrewIds
            internal.copy(selectedClassIds = newIds)
        }
    }

    fun delete(cls: CharacterClass) {
        viewModelScope.launch { repo.delete(cls) }
    }

    fun deleteSelected() {
        val idsToDelete = _internalState.value.selectedClassIds
        viewModelScope.launch {
            uiState.value.homebrewClasses
                .filter { it.id in idsToDelete }
                .forEach { repo.delete(it) }
            clearSelection()
        }
    }

    fun generateExportJson(targetClassId: String? = null): String? {
        val currentState = uiState.value

        val racesToExport = if (targetClassId != null) {
            currentState.homebrewClasses.filter {
                it.id.trim().equals(targetClassId.trim(), ignoreCase = true)
            }
        } else {
            currentState.homebrewClasses.filter { currentState.selectedClassesIds.contains(it.id) }
        }

        if (racesToExport.isEmpty()) return null

        return Json.encodeToString(racesToExport)
    }

    fun importClassesFromJson(jsonString: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val importedClasses = Json.decodeFromString<List<CharacterClass>>(jsonString)
                val preparedClasses = importedClasses.map { it.copy(isHomebrew = true) }

                repo.insertAll(preparedClasses)
                onComplete()
            } catch (_: Exception) {
                _internalState.value = _internalState.value.copy(
                    errorMessage = "Failed to parse import file."
                )
            }
        }
    }
}

private data class InternalUiState(
    val selectedClassIds: Set<String> = emptySet(),
    val searchQuery: String = "",
    val isOrigExpanded: Boolean = true,
    val isHomebrewExpanded: Boolean = true,
    val errorMessage: String? = null
)

data class ClassUiState(
    val origClasses: List<CharacterClass> = emptyList(),
    val homebrewClasses: List<CharacterClass> = emptyList(),
    val selectedClass: CharacterClass? = null,
    val selectedClassesIds: Set<String> = emptySet(),
    val isSelectionMode: Boolean = false,
    val isAllSelected: Boolean = false,
    val searchQuery: String = "",
    val origClassesSectionState: CollapsibleSectionState = CollapsibleSectionState(initialExpanded = true),
    val homebrewClassesSectionState: CollapsibleSectionState = CollapsibleSectionState(
        initialExpanded = true
    ),
    val errorMessage: String? = null,
    val isLoading: Boolean = true
)