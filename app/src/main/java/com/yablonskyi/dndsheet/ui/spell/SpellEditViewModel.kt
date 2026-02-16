package com.yablonskyi.dndsheet.ui.spell

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.yablonskyi.dndsheet.data.model.character.Spell
import com.yablonskyi.dndsheet.domain.repository.SpellRepository
import com.yablonskyi.dndsheet.ui.navigation.UpdateSpellRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpellEditViewModel @Inject constructor(
    private val repository: SpellRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val route = savedStateHandle.toRoute<UpdateSpellRoute>()
    private val spellId = route.spellId

    val spell: StateFlow<Spell?> = flow {
        if (spellId == 0L) {
            emit(Spell())
        } else {
            emitAll(repository.getSpellById(spellId))
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun saveSpell(spell: Spell) {
        viewModelScope.launch {
            if (spell.spellId == 0L) {
                repository.insertSpell(spell)
            } else {
                repository.updateSpell(spell)
            }
        }
    }
}