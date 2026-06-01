package com.yablonskyi.dndsheet.ui.compendium.races

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yablonskyi.dndsheet.data.model.rulebook.Race
import com.yablonskyi.dndsheet.domain.repository.RaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RaceCreateViewModel @Inject constructor(
    private val repo: RaceRepository
) : ViewModel() {
    fun createRace(race: Race) {
        viewModelScope.launch {
            repo.insert(race.copy(isHomebrew = true))
        }
    }
}