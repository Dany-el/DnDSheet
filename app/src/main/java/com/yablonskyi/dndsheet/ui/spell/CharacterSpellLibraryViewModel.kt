package com.yablonskyi.dndsheet.ui.spell

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.yablonskyi.dndsheet.data.model.character.CharacterSpellCrossRef
import com.yablonskyi.dndsheet.data.model.character.MagicSchool
import com.yablonskyi.dndsheet.data.model.character.Spell
import com.yablonskyi.dndsheet.data.model.character.SpellCastTime
import com.yablonskyi.dndsheet.data.model.character.SpellDuration
import com.yablonskyi.dndsheet.data.model.character.SpellLevel
import com.yablonskyi.dndsheet.domain.repository.SpellRepository
import com.yablonskyi.dndsheet.ui.navigation.SpellLibraryRoute
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
class CharacterSpellLibraryViewModel @Inject constructor(
    private val repository: SpellRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val args = savedStateHandle.toRoute<SpellLibraryRoute>()
    val characterId: Long = args.characterId

    val isSelectionMode = true

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _filterState = MutableStateFlow(SpellFilterState())
    val filterState = _filterState.asStateFlow()

    private val _allLibrarySpells = repository.getAllSpellsInLibrary()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _learnedSpellIds = repository.getCharacterSpells(characterId)
        .map { spells -> spells.map { it.spellId }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val spellLibraryList: StateFlow<List<SpellLibraryItem>> = combine(
        _allLibrarySpells,
        _learnedSpellIds,
        _searchQuery,
        _filterState
    ) { allSpells, learnedIds, query, filter ->

        val filtered = filterAndSearchSpells(allSpells, query, filter)

        filtered.map { spell ->
            SpellLibraryItem(
                spell = spell,
                isLearned = learnedIds.contains(spell.spellId)
            )
        }.sortedWith(compareBy({ it.spell.level.ordinal }, { it.spell.name }))

    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    fun toggleSpellSelection(spell: Spell) {
        viewModelScope.launch {
            val isLearned = _learnedSpellIds.value.contains(spell.spellId)
            if (isLearned) {
                repository.removeSpellFromCharacter(characterId, spell.spellId)
            } else {
                repository.assignSpellToCharacter(
                    CharacterSpellCrossRef(
                        characterId,
                        spell.spellId
                    )
                )
            }
        }
    }
}