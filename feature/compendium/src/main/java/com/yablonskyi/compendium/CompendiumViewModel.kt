package com.yablonskyi.compendium

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yablonskyi.domain.repository.ClassRepository
import com.yablonskyi.domain.repository.RaceRepository
import com.yablonskyi.domain.repository.SpellRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class CompendiumViewModel @Inject constructor(
    classRepository: ClassRepository,
    raceRepository: RaceRepository,
    spellRepository: SpellRepository
) : ViewModel() {

    val compendiumState: StateFlow<CompendiumState> = combine(
        raceRepository.getAllRaces(),
        classRepository.getAllClasses(),
        spellRepository.getAllSpellsInLibrary()
    ) { races, classes, spells ->
        CompendiumState(
            racesCount = races.size,
            classesCount = classes.size,
            spellsCount = spells.size
        )
    }.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(5000L),
        initialValue = CompendiumState()
    )
}

data class CompendiumState(
    val racesCount: Int = 0,
    val classesCount: Int = 0,
    val spellsCount: Int = 0
)