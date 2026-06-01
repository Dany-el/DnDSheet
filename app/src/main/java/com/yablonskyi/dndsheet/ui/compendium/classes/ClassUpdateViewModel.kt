package com.yablonskyi.dndsheet.ui.compendium.classes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.yablonskyi.dndsheet.data.model.rulebook.CharacterClass
import com.yablonskyi.dndsheet.domain.repository.ClassRepository
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
class ClassUpdateViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: ClassRepository
) : ViewModel() {

    private val classId = savedStateHandle.toRoute<CompendiumRoute.ClassUpdateScreen>().classId

    private val _updateDone = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val updateDone = _updateDone.asSharedFlow()

    val characterClass: StateFlow<CharacterClass?> = flow {
        if (classId.isBlank()) {
            emit(null)
        } else {
            emitAll(repo.getClassById(classId))
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun updateClass(cls: CharacterClass) {
        viewModelScope.launch {
            repo.update(cls.copy(isHomebrew = true))
            _updateDone.emit(Unit)
        }
    }
}