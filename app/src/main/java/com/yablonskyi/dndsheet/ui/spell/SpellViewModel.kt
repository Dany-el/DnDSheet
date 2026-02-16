package com.yablonskyi.dndsheet.ui.spell

import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.yablonskyi.dndsheet.R
import com.yablonskyi.dndsheet.data.model.character.CharacterSpellCrossRef
import com.yablonskyi.dndsheet.data.model.character.Spell
import com.yablonskyi.dndsheet.domain.repository.SpellRepository
import com.yablonskyi.dndsheet.ui.navigation.CharacterSheetRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@HiltViewModel
class SpellViewModel @Inject constructor(
    private val repository: SpellRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val args = savedStateHandle.toRoute<CharacterSheetRoute>()
    private val characterId: Long = args.id

    init {
        Log.i("SpellViewModel", "Loaded character with ID: $characterId")
    }

    val allLibrarySpells = repository.getAllSpellsInLibrary()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _allCharacterSpells = repository.getCharacterSpells(characterId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _currentFilter = MutableStateFlow<SpellFilter>(SpellFilter.All)
    val currentFilter = _currentFilter.asStateFlow()

    val availableFilters: StateFlow<List<SpellFilter>> = _allCharacterSpells
        .map { spells ->
            val filters = mutableListOf<SpellFilter>()
            filters.add(SpellFilter.All)

            val distinctLevels = spells.map { it.level }.distinct().sortedBy { it.ordinal }
            distinctLevels.forEach { levelEnum ->
                filters.add(SpellFilter.ByLevel(levelEnum.ordinal))
            }

            if (spells.any { it.isConcentration }) {
                filters.add(SpellFilter.Concentration)
            }

            if (spells.any { it.isRitual }) {
                filters.add(SpellFilter.Ritual)
            }

            filters
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf(SpellFilter.All)
        )

    val spellList: StateFlow<List<Spell>> = combine(_allCharacterSpells, _currentFilter) { spells, filter ->
        when (filter) {
            is SpellFilter.All -> spells.sortedBy { it.level.ordinal }
            is SpellFilter.ByLevel -> spells.filter { it.level.ordinal == filter.level }
            is SpellFilter.Concentration -> spells.filter { it.isConcentration }
            is SpellFilter.Ritual -> spells.filter { it.isRitual }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setFilter(filter: SpellFilter) {
        _currentFilter.value = filter
    }

    fun addSpell(spell: Spell) {
        viewModelScope.launch {
            repository.insertSpell(spell)
        }
    }

    fun learnExistingSpell(spell: Spell) {
        viewModelScope.launch {
            val crossRef = CharacterSpellCrossRef(
                characterId = characterId,
                spellId = spell.spellId
            )
            repository.assignSpellToCharacter(crossRef)
        }
    }

    fun unlearnSpell(spell: Spell) {
        viewModelScope.launch {
            repository.removeSpellFromCharacter(characterId, spell.spellId)
        }
    }

    fun deleteSpellGlobally(spell: Spell) {
        viewModelScope.launch {
            repository.deleteSpell(spell)
        }
    }
}

@Parcelize
sealed class SpellFilter : Parcelable {
    object All : SpellFilter()
    data class ByLevel(val level: Int) : SpellFilter()
    object Concentration : SpellFilter()
    object Ritual : SpellFilter()

    fun getLabelResId(): Int {
        return when (this) {
            All -> R.string.filter_all
            is ByLevel -> {
                when (level) {
                    0 -> R.string.level_cantrip
                    1 -> R.string.level_1
                    2 -> R.string.level_2
                    3 -> R.string.level_3
                    4 -> R.string.level_4
                    5 -> R.string.level_5
                    6 -> R.string.level_6
                    7 -> R.string.level_7
                    8 -> R.string.level_8
                    9 -> R.string.level_9
                    else -> R.string.level_cantrip
                }
            }

            Concentration -> R.string.concentration
            Ritual -> R.string.ritual
        }
    }
}