package com.yablonskyi.dndsheet.ui.spell

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.yablonskyi.dndsheet.data.model.character.CharacterSpellCrossRef
import com.yablonskyi.dndsheet.data.model.character.Spell
import com.yablonskyi.dndsheet.domain.repository.SpellRepository
import com.yablonskyi.dndsheet.ui.navigation.SpellLibraryRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpellLibraryViewModel @Inject constructor(
    private val repository: SpellRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val args = savedStateHandle.toRoute<SpellLibraryRoute>()
    val characterId: Long? = if (args.characterId != -1L) args.characterId else null

    val isSelectionMode: Boolean = characterId != null

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _currentFilter = MutableStateFlow<SpellFilter>(SpellFilter.All)
    val currentFilter = _currentFilter.asStateFlow()

    private val _allLibrarySpells = repository.getAllSpellsInLibrary()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _learnedSpellIds = if (characterId != null) {
        repository.getCharacterSpells(characterId)
            .map { spells -> spells.map { it.spellId }.toSet() }
    } else {
        flowOf(emptySet())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptySet()
    )

    val availableFilters: StateFlow<List<SpellFilter>> = _allLibrarySpells
        .map { spells ->
            val filters = mutableListOf<SpellFilter>()
            filters.add(SpellFilter.All)

            val distinctLevels = spells.map { it.level }.distinct().sortedBy { it.ordinal }
            distinctLevels.forEach { levelEnum ->
                filters.add(SpellFilter.ByLevel(levelEnum.ordinal))
            }
            if (spells.any { it.isConcentration }) filters.add(SpellFilter.Concentration)
            if (spells.any { it.isRitual }) filters.add(SpellFilter.Ritual)
            filters
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf(SpellFilter.All)
        )

    val spellLibraryList: StateFlow<List<SpellLibraryItem>> = combine(
        _allLibrarySpells,
        _learnedSpellIds,
        _searchQuery,
        _currentFilter
    ) { allSpells, learnedIds, query, filter ->

        val filtered = allSpells.filter { spell ->
            val matchesSearch = query.isEmpty() || spell.name.contains(query, ignoreCase = true)
            val matchesFilter = when (filter) {
                is SpellFilter.All -> true
                is SpellFilter.ByLevel -> spell.level.ordinal == filter.level
                is SpellFilter.Concentration -> spell.isConcentration
                is SpellFilter.Ritual -> spell.isRitual
            }
            matchesSearch && matchesFilter
        }

        filtered.map { spell ->
            SpellLibraryItem(
                spell = spell,
                isLearned = learnedIds.contains(spell.spellId)
            )
        }.sortedWith(compareBy({ it.spell.level.ordinal }, { it.spell.name }))

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun setFilter(filter: SpellFilter) {
        _currentFilter.value = filter
    }

    fun toggleSpellSelection(spell: Spell) {
        val cId = characterId ?: return

        viewModelScope.launch {
            val isLearned = _learnedSpellIds.value.contains(spell.spellId)

            if (isLearned) {
                repository.removeSpellFromCharacter(cId, spell.spellId)
            } else {
                val crossRef = CharacterSpellCrossRef(
                    characterId = cId,
                    spellId = spell.spellId
                )
                repository.assignSpellToCharacter(crossRef)
            }
        }
    }

    fun deleteSpellGlobally(spell: Spell) {
        viewModelScope.launch {
            repository.deleteSpell(spell)
        }
    }
}

data class SpellLibraryItem(
    val spell: Spell,
    val isLearned: Boolean
)