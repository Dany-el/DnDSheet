package com.yablonskyi.dndsheet.ui.compendium

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yablonskyi.dndsheet.domain.repository.ClassRepository
import com.yablonskyi.dndsheet.domain.repository.RaceRepository
import com.yablonskyi.dndsheet.domain.repository.SpellRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class CompendiumViewModel @Inject constructor(
    classRepository: ClassRepository,
    raceRepository: RaceRepository,
    spellRepository: SpellRepository
) : ViewModel() {

    val racesCount = raceRepository.getAllRaces()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val classesCount = classRepository.getAllClasses()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val spellsCount = spellRepository.getAllSpellsInLibrary()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
}