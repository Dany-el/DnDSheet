package com.yablonskyi.compendium.spells.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yablonskyi.domain.repository.SpellRepository
import com.yablonskyi.ui.spell.SpellFilterState
import com.yablonskyi.ui.spell.SpellLibraryItem
import com.yablonskyi.ui.spell.SpellLibraryState
import com.yablonskyi.ui.spell.SpellsDeleteRequest
import com.yablonskyi.ui.spell.SpellsDeleteRequest.Selection
import com.yablonskyi.ui.spell.SpellsDeleteRequest.Single
import com.yablonskyi.ui.spell.SpellsEffect
import com.yablonskyi.ui.spell.SpellsEffect.NavigateBack
import com.yablonskyi.ui.spell.SpellsEffect.NavigateToAddSpell
import com.yablonskyi.ui.spell.SpellsEffect.NavigateToEdit
import com.yablonskyi.ui.spell.SpellsIntent
import com.yablonskyi.ui.spell.filterAndSearchSpells
import com.yablonskyi.model.character.MagicSchool
import com.yablonskyi.model.character.Spell
import com.yablonskyi.model.character.SpellCastTime
import com.yablonskyi.model.character.SpellDuration
import com.yablonskyi.model.character.SpellLevel
import com.yablonskyi.ui.R
import com.yablonskyi.ui.utils.FileOperationEffect
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
class CompendiumSpellsLibraryViewModel @Inject constructor(
    private val repository: SpellRepository
) : ViewModel() {

    private val _effect = Channel<SpellsEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private val _fileEffect = Channel<FileOperationEffect>(Channel.BUFFERED)
    val fileEffect = _fileEffect.receiveAsFlow()

    private val _internalState = MutableStateFlow(InternalUiState())

    private val allLibrarySpells: StateFlow<List<Spell>> = repository.getAllSpellsInLibrary()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val spellContent = combine(
        allLibrarySpells,
        _internalState
            .map { SpellQuery(it.searchQuery, it.filterState) }
            .distinctUntilChanged()
    ) { allSpells, query ->
        filterAndSearchSpells(allSpells, query.searchQuery, query.filterState)
            .map { SpellLibraryItem(spell = it, isLearned = false) }
            .sortedWith(compareBy({ it.spell.level.ordinal }, { it.spell.name }))
            .let(::SpellContent)
    }

    val uiState: StateFlow<SpellLibraryState> = combine(
        spellContent,
        _internalState
    ) { content, internal ->
        val visibleIds = content.spells.mapTo(mutableSetOf()) { it.spell.spellId }
        SpellLibraryState(
            spells = content.spells,
            groupedSpells = content.groupedSpells,
            isLoading = false,
            searchQuery = internal.searchQuery,
            isLearnMode = false,
            filterState = internal.filterState,
            isSelectionMode = internal.isSelectionMode,
            isAllSelected = visibleIds.isNotEmpty() && internal.selectedSpellIds.containsAll(visibleIds),
            selectedSpellIds = internal.selectedSpellIds,
            isFilterExpanded = internal.isFilterExpanded,
            showBottomSheet = internal.showBottomSheet,
            showConfirmDeleteDialog = internal.pendingDelete != null,
            pendingDelete = internal.pendingDelete,
            pendingImport = internal.pendingImport
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SpellLibraryState(isLoading = true)
    )

    fun onIntent(intent: SpellsIntent) {
        when (intent) {
            SpellsIntent.NavigateBack -> sendEffect(NavigateBack)
            SpellsIntent.AddSpell -> sendEffect(NavigateToAddSpell)
            is SpellsIntent.EditSpell -> sendEffect(NavigateToEdit(intent.spellId))
            is SpellsIntent.SearchQueryChanged -> onSearchQueryChange(intent.value)
            SpellsIntent.ToggleFiltersExpanded -> toggleFiltersExpanded()
            is SpellsIntent.ToggleLevelFilter -> toggleLevel(intent.level)
            is SpellsIntent.ToggleSchoolFilter -> toggleSchool(intent.school)
            is SpellsIntent.ToggleDurationFilter -> toggleDuration(intent.duration)
            is SpellsIntent.ToggleCastTimeFilter -> toggleCastTime(intent.castTime)
            SpellsIntent.ToggleRitual -> toggleRitual()
            SpellsIntent.ToggleConcentration -> toggleConcentration()
            is SpellsIntent.ShowDetails -> showDetails(intent.spell)
            SpellsIntent.DismissDetails -> dismissDetails()

            // Learn / unlearn does not apply to the global compendium library
            is SpellsIntent.ToggleSpell -> Unit
            is SpellsIntent.ToggleSelection -> toggleSelection(intent.spell)
            SpellsIntent.ToggleSelectAll -> toggleSelectAll()
            SpellsIntent.EnterSelectionMode -> _internalState.update { it.copy(isSelectionMode = true, selectedSpellIds = emptySet()) }
            SpellsIntent.ClearSelection -> closeSelection()
            is SpellsIntent.Delete -> requestDelete(Single(intent.spell))
            SpellsIntent.DeleteSelected -> requestDelete(Selection)
            SpellsIntent.ConfirmDelete -> confirmDelete()
            SpellsIntent.DismissDelete -> dismissDelete()
            is SpellsIntent.ShareRequested -> shareRequested(intent.spell)
            SpellsIntent.ShareFailed -> sendEffect(SpellsEffect.ShowSnackbar(R.string.failure_share))
            SpellsIntent.ExportAllSelected -> exportAllSelected()
            SpellsIntent.RequestFilePicker -> requestFilePicker()
            is SpellsIntent.ExportCompleted -> onExportCompleted(intent.success)
            is SpellsIntent.ImportRequested -> importFromJson(intent.json)
            SpellsIntent.ConfirmImport -> confirmImport()
            SpellsIntent.DismissImport -> dismissImport()

            // Filter
            SpellsIntent.ClearAllFilters -> clearAllFilters()
        }
    }

    // Filter
    private fun clearAllFilters() {
        _internalState.update {
            it.copy(filterState = SpellFilterState())
        }
    }

    private fun sendEffect(effect: SpellsEffect) {
        viewModelScope.launch { _effect.send(effect) }
    }

    private fun onSearchQueryChange(query: String) {
        _internalState.update { it.copy(searchQuery = query) }
    }

    private fun toggleFiltersExpanded() {
        _internalState.update { it.copy(isFilterExpanded = !it.isFilterExpanded) }
    }

    private fun toggleLevel(level: SpellLevel) {
        _internalState.update { current ->
            val newSet = if (level in current.filterState.levels) {
                current.filterState.levels - level
            } else {
                current.filterState.levels + level
            }
            current.copy(filterState = current.filterState.copy(levels = newSet))
        }
    }

    private fun toggleSchool(school: MagicSchool) {
        _internalState.update { current ->
            val newSet = if (school in current.filterState.schools) {
                current.filterState.schools - school
            } else {
                current.filterState.schools + school
            }
            current.copy(filterState = current.filterState.copy(schools = newSet))
        }
    }

    private fun toggleDuration(duration: SpellDuration) {
        _internalState.update { current ->
            val newSet = if (duration in current.filterState.durations) {
                current.filterState.durations - duration
            } else {
                current.filterState.durations + duration
            }
            current.copy(filterState = current.filterState.copy(durations = newSet))
        }
    }

    private fun toggleCastTime(castTime: SpellCastTime) {
        _internalState.update { current ->
            val newSet = if (castTime in current.filterState.castTimes) {
                current.filterState.castTimes - castTime
            } else {
                current.filterState.castTimes + castTime
            }
            current.copy(filterState = current.filterState.copy(castTimes = newSet))
        }
    }

    private fun toggleConcentration() {
        _internalState.update { it.copy(filterState = it.filterState.copy(onlyConcentration = !it.filterState.onlyConcentration)) }
    }

    private fun toggleRitual() {
        _internalState.update { it.copy(filterState = it.filterState.copy(onlyRitual = !it.filterState.onlyRitual)) }
    }

    private fun showDetails(spell: Spell) {
        _internalState.update { it.copy(showBottomSheet = spell) }
    }

    private fun dismissDetails() {
        _internalState.update { it.copy(showBottomSheet = null) }
    }

    private fun toggleSelection(spell: Spell) {
        _internalState.update { current ->
            val selection = if (spell.spellId in current.selectedSpellIds) {
                current.selectedSpellIds - spell.spellId
            } else {
                current.selectedSpellIds + spell.spellId
            }
            current.copy(
                selectedSpellIds = selection,
                isSelectionMode = true
            )
        }
    }

    private fun toggleSelectAll() {
        val visibleIds = uiState.value.spells.mapTo(mutableSetOf()) { it.spell.spellId }
        _internalState.update { current ->
            val allVisibleSelected = current.selectedSpellIds.containsAll(visibleIds)
            current.copy(
                selectedSpellIds = if (allVisibleSelected) {
                    current.selectedSpellIds - visibleIds
                } else {
                    current.selectedSpellIds + visibleIds
                },
                isSelectionMode = true
            )
        }
    }

    private fun closeSelection() {
        _internalState.update {
            it.copy(isSelectionMode = false, selectedSpellIds = emptySet())
        }
    }

    private fun requestDelete(request: SpellsDeleteRequest) {
        _internalState.update { it.copy(pendingDelete = request) }
    }

    private fun dismissDelete() {
        _internalState.update { it.copy(pendingDelete = null) }
    }

    private fun confirmDelete() {
        val request = _internalState.value.pendingDelete ?: return
        viewModelScope.launch {
            when (request) {
                is SpellsDeleteRequest.Single -> repository.deleteSpell(request.spell)
                SpellsDeleteRequest.Selection -> {
                    val selectedIds = _internalState.value.selectedSpellIds
                    repository.deleteSpells(allLibrarySpells.value.filter { it.spellId in selectedIds })
                    closeSelection()
                }
            }
            _internalState.update { it.copy(pendingDelete = null) }
        }
    }

    private fun shareRequested(spell: Spell) {
        viewModelScope.launch {
            _fileEffect.send(
                FileOperationEffect.ShareReady(
                    fileName = "${spell.name.replace(" ", "_")}.json",
                    json = Json.encodeToString(listOf(spell))
                )
            )
        }
    }

    private fun exportAllSelected() {
        val selectedIds = _internalState.value.selectedSpellIds
        val selected = allLibrarySpells.value.filter { it.spellId in selectedIds }
        if (selected.isEmpty()) return
        viewModelScope.launch {
            _fileEffect.send(
                FileOperationEffect.ExportReady(
                    fileName = "selected_spells.json",
                    json = Json.encodeToString(selected)
                )
            )
        }
    }

    private fun requestFilePicker() {
        viewModelScope.launch { _fileEffect.send(FileOperationEffect.OpenImportPicker) }
    }

    private fun onExportCompleted(success: Boolean) {
        if (success) {
            _internalState.update {
                it.copy(selectedSpellIds = emptySet(), isSelectionMode = false)
            }
        }
        sendEffect(
            if (success) SpellsEffect.ShowSnackbar(R.string.success_export)
            else SpellsEffect.ShowSnackbar(R.string.failure_export)
        )
    }

    private fun importFromJson(jsonString: String) {
        viewModelScope.launch {
            try {
                val importedSpells = Json.decodeFromString<List<Spell>>(jsonString)
                if (importedSpells.isEmpty()) {
                    sendEffect(SpellsEffect.ShowSnackbar(R.string.import_file_empty))
                } else {
                    _internalState.update { it.copy(pendingImport = importedSpells) }
                }
            } catch (_: Exception) {
                sendEffect(SpellsEffect.ShowSnackbar(R.string.failure_import))
            }
        }
    }

    private fun confirmImport() {
        val spells = _internalState.value.pendingImport ?: return
        viewModelScope.launch {
            try {
                repository.insertSpells(spells.map { it.copy(spellId = 0) })
                sendEffect(SpellsEffect.ShowSnackbar(R.string.success_import))
            } catch (_: Exception) {
                sendEffect(SpellsEffect.ShowSnackbar(R.string.failure_import))
            } finally {
                _internalState.update { it.copy(pendingImport = null) }
            }
        }
    }

    private fun dismissImport() {
        _internalState.update { it.copy(pendingImport = null) }
    }

    private data class InternalUiState(
        val searchQuery: String = "",
        val filterState: SpellFilterState = SpellFilterState(),
        val selectedSpellIds: Set<Long> = emptySet(),
        val isSelectionMode: Boolean = false,
        val isFilterExpanded: Boolean = false,
        val showBottomSheet: Spell? = null,
        val pendingDelete: SpellsDeleteRequest? = null,
        val pendingImport: List<Spell>? = null
    )

    private data class SpellQuery(
        val searchQuery: String,
        val filterState: SpellFilterState
    )

private data class SpellContent(
        val spells: List<SpellLibraryItem>
    ) {
        val groupedSpells: Map<SpellLevel, List<SpellLibraryItem>> =
            spells.groupBy { it.spell.level }.toSortedMap()
    }
}
