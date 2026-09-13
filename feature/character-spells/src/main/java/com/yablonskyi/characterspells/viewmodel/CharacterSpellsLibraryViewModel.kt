package com.yablonskyi.characterspells.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.yablonskyi.characterspells.navigation.CharacterSpellsRoute
import com.yablonskyi.domain.repository.SpellRepository
import com.yablonskyi.model.character.CharacterSpellCrossRef
import com.yablonskyi.model.character.MagicSchool
import com.yablonskyi.model.character.Spell
import com.yablonskyi.model.character.SpellCastTime
import com.yablonskyi.model.character.SpellDuration
import com.yablonskyi.model.character.SpellLevel
import com.yablonskyi.ui.spell.SpellFilterState
import com.yablonskyi.ui.spell.SpellLibraryItem
import com.yablonskyi.ui.spell.SpellLibraryState
import com.yablonskyi.ui.spell.SpellsEffect
import com.yablonskyi.ui.spell.SpellsIntent
import com.yablonskyi.ui.spell.filterAndSearchSpells
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(FlowPreview::class)
class CharacterSpellsLibraryViewModel @Inject constructor(
    private val repository: SpellRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val characterId = savedStateHandle.toRoute<CharacterSpellsRoute>().characterId

    private val _effect = Channel<SpellsEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private val searchQuery = MutableStateFlow("")

    private val filterState = MutableStateFlow(SpellFilterState())

    private val isFilterExpanded = MutableStateFlow(false)

    private val allLibrarySpells = repository.getAllSpellsInLibrary()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val learnedSpellIds = repository.getCharacterSpells(characterId)
        .map { spells -> spells.map { it.spellId }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    private val filteredLibrarySpells = combine(
        allLibrarySpells,
        searchQuery
            .debounce { query -> if (query.isBlank()) 0L else SEARCH_DEBOUNCE_MILLIS }
            .distinctUntilChanged(),
        filterState,
    ) { allSpells, query, filter ->
        filterAndSearchSpells(allSpells, query, filter)
    }

    val uiState: StateFlow<SpellLibraryState> = combine(
        filteredLibrarySpells,
        learnedSpellIds,
        searchQuery,
        filterState,
        isFilterExpanded,
    ) { filteredSpells, learnedIds, query, filter, isFilterExpanded ->
        val items = filteredSpells.map { spell ->
            SpellLibraryItem(
                spell = spell,
                isLearned = learnedIds.contains(spell.spellId)
            )
        }.sortedWith(compareBy({ it.spell.level.ordinal }, { it.spell.name }))

        SpellLibraryState(
            spells = items,
            groupedSpells = items.groupBy { it.spell.level }.toSortedMap(),
            searchQuery = query,
            isLearnMode = true,
            filterState = filter,
            isFilterExpanded = isFilterExpanded,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SpellLibraryState(isLoading = true)
    )

    fun onIntent(intent: SpellsIntent) {
        when(intent) {
            SpellsIntent.NavigateBack -> sendEffect(SpellsEffect.NavigateBack)
            is SpellsIntent.SearchQueryChanged -> onSearchQueryChange(intent.value)
            is SpellsIntent.ToggleLevelFilter -> toggleLevel(intent.level)
            is SpellsIntent.ToggleSchoolFilter -> toggleSchool(intent.school)
            is SpellsIntent.ToggleDurationFilter -> toggleDuration(intent.duration)
            is SpellsIntent.ToggleCastTimeFilter -> toggleCastTime(intent.castTime)
            SpellsIntent.ToggleRitual -> toggleRitual()
            SpellsIntent.ToggleConcentration -> toggleConcentration()
            SpellsIntent.ClearAllFilters -> clearAllFilters()
            is SpellsIntent.ToggleSpell -> toggleSpellSelection(intent.spell)
            SpellsIntent.ToggleFiltersExpanded -> isFilterExpanded.update { !it }

            else -> Unit
        }
    }

    private fun sendEffect(effect: SpellsEffect) {
        viewModelScope.launch { _effect.send(effect) }
    }

    fun onSearchQueryChange(newQuery: String) {
        searchQuery.value = newQuery
    }

    fun toggleLevel(level: SpellLevel) {
        filterState.update { current ->
            val newSet = if (level in current.levels) {
                current.levels - level
            } else {
                current.levels + level
            }
            current.copy(levels = newSet)
        }
    }

    fun toggleSchool(school: MagicSchool) {
        filterState.update { current ->
            val newSet = if (school in current.schools) {
                current.schools - school
            } else {
                current.schools + school
            }
            current.copy(schools = newSet)
        }
    }

    fun toggleCastTime(castTime: SpellCastTime) {
        filterState.update { current ->
            val newSet = if (castTime in current.castTimes) {
                current.castTimes - castTime
            } else {
                current.castTimes + castTime
            }
            current.copy(castTimes = newSet)
        }
    }

    fun toggleDuration(spellDuration: SpellDuration) {
        filterState.update { current ->
            val newSet = if (spellDuration in current.durations) {
                current.durations - spellDuration
            } else {
                current.durations + spellDuration
            }
            current.copy(durations = newSet)
        }
    }

    fun toggleConcentration() {
        filterState.update { it.copy(onlyConcentration = !it.onlyConcentration) }
    }

    fun toggleRitual() {
        filterState.update { it.copy(onlyRitual = !it.onlyRitual) }
    }

    fun clearAllFilters() {
        filterState.value = SpellFilterState()
    }

    fun toggleSpellSelection(spell: Spell) {
        viewModelScope.launch {
            val isLearned = learnedSpellIds.value.contains(spell.spellId)
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

    private companion object {
        const val SEARCH_DEBOUNCE_MILLIS = 300L
    }
}