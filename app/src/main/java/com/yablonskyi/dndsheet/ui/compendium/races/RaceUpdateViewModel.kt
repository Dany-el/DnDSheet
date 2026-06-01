package com.yablonskyi.dndsheet.ui.compendium.races

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.yablonskyi.dndsheet.data.model.rulebook.Race
import com.yablonskyi.dndsheet.domain.repository.RaceRepository
import com.yablonskyi.dndsheet.ui.navigation.CompendiumRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RaceUpdateViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: RaceRepository
) : ViewModel() {

    private val raceId = savedStateHandle.toRoute<CompendiumRoute.RaceUpdateScreen>().raceId

    private val _updateDone = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val updateDone = _updateDone.asSharedFlow()

    val race: StateFlow<Race?> = flow {
        if (raceId.isBlank()) {
            emit(null)
        } else {
            emitAll(repo.getRaceById(raceId))
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun updateRace(race: Race) {
        viewModelScope.launch {
            repo.update(race.copy(isHomebrew = true))
            _updateDone.emit(Unit)
        }
    }
}