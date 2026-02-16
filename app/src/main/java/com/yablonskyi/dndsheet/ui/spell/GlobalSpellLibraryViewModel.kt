package com.yablonskyi.dndsheet.ui.spell

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yablonskyi.dndsheet.data.model.character.MagicSchool
import com.yablonskyi.dndsheet.data.model.character.Spell
import com.yablonskyi.dndsheet.data.model.character.SpellCastTime
import com.yablonskyi.dndsheet.data.model.character.SpellDuration
import com.yablonskyi.dndsheet.data.model.character.SpellLevel
import com.yablonskyi.dndsheet.domain.repository.SpellRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GlobalSpellLibraryViewModel @Inject constructor(
    private val repository: SpellRepository
) : ViewModel() {

    val isLearnMode = false

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _filterState = MutableStateFlow(SpellFilterState())
    val filterState = _filterState.asStateFlow()

    private val _selectedSpells = MutableStateFlow<Set<Spell>>(emptySet())
    val selectedSpells = _selectedSpells.asStateFlow()

    val spellListState: StateFlow<SpellListState> = repository.getAllSpellsInLibrary()
        .map { spellsList ->
            SpellListState(
                spells = spellsList,
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SpellListState(isLoading = true)
        )

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode = _isSelectionMode.asStateFlow()

    val isAllSelected: StateFlow<Boolean> =
        combine(_selectedSpells, spellListState) { selected, state ->
            selected.size == state.spells.size && state.spells.isNotEmpty()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val spellLibraryList: StateFlow<List<SpellLibraryItem>> = combine(
        spellListState,
        _searchQuery,
        _filterState
    ) { allSpells, query, filter ->

        val filtered = filterAndSearchSpells(allSpells.spells, query, filter)

        filtered.map { spell ->
            SpellLibraryItem(spell = spell, isLearned = false)
        }.sortedWith(compareBy({ it.spell.level.ordinal }, { it.spell.name }))

    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun importSpells(importedSpells: List<Spell>) {
        viewModelScope.launch {
            val spellsToInsert = importedSpells.map { it.copy(spellId = 0) }
            repository.insertSpells(spellsToInsert)
        }
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun toggleLevel(level: SpellLevel) {
        _filterState.update { current ->
            val newSet = if (level in current.levels) {
                current.levels - level
            } else {
                current.levels + level
            }
            current.copy(levels = newSet)
        }
    }

    fun toggleSchool(school: MagicSchool) {
        _filterState.update { current ->
            val newSet = if (school in current.schools) {
                current.schools - school
            } else {
                current.schools + school
            }
            current.copy(schools = newSet)
        }
    }

    fun toggleCastTime(castTime: SpellCastTime) {
        _filterState.update { current ->
            val newSet = if (castTime in current.castTimes) {
                current.castTimes - castTime
            } else {
                current.castTimes + castTime
            }
            current.copy(castTimes = newSet)
        }
    }

    fun toggleDuration(spellDuration: SpellDuration) {
        _filterState.update { current ->
            val newSet = if (spellDuration in current.durations) {
                current.durations - spellDuration
            } else {
                current.durations + spellDuration
            }
            current.copy(durations = newSet)
        }
    }

    fun toggleConcentration() {
        _filterState.update { it.copy(onlyConcentration = !it.onlyConcentration) }
    }

    fun toggleRitual() {
        _filterState.update { it.copy(onlyRitual = !it.onlyRitual) }
    }

    fun clearFilters() {
        _filterState.value = SpellFilterState()
    }

    fun toggleSelection(spell: Spell) {
        if (!_isSelectionMode.value) {
            _isSelectionMode.value = true
        }

        _selectedSpells.update { currentSelection ->
            if (currentSelection.contains(spell)) {
                currentSelection - spell
            } else {
                currentSelection + spell
            }
        }
    }

    fun toggleSelectAll() {
        if (isAllSelected.value) {
            _selectedSpells.value = emptySet()
        } else {
            _selectedSpells.value = spellListState.value.spells.toSet()
        }
    }

    fun closeSelection() {
        _isSelectionMode.value = false
        _selectedSpells.value = emptySet()
    }

    fun deleteSelectedSpells() {
        viewModelScope.launch {
            val spellsToDelete = _selectedSpells.value.toList()
            repository.deleteSpells(spellsToDelete)
            closeSelection()
        }
    }

    fun deleteSpellGlobally(spell: Spell) {
        viewModelScope.launch { repository.deleteSpell(spell) }
    }
}

data class SpellListState(
    val spells: List<Spell> = emptyList(),
    val isLoading: Boolean = false
)