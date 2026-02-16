package com.yablonskyi.dndsheet.ui.character

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.yablonskyi.dndsheet.data.model.character.Ability
import com.yablonskyi.dndsheet.data.model.character.Character
import com.yablonskyi.dndsheet.data.model.character.ProficiencyLevel
import com.yablonskyi.dndsheet.data.model.character.Skill
import com.yablonskyi.dndsheet.data.model.character.SpellLevel
import com.yablonskyi.dndsheet.data.model.character.SpellSlot
import com.yablonskyi.dndsheet.domain.repository.CharacterRepository
import com.yablonskyi.dndsheet.ui.navigation.CharacterSheetRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CharacterDetailViewModel @Inject constructor(
    private val repository: CharacterRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val args = savedStateHandle.toRoute<CharacterSheetRoute>()

    val character: StateFlow<Character?> = repository.getCharacterById(args.id)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = null
        )

    fun updateCharacter(character: Character) {
        viewModelScope.launch {
            repository.updateCharacter(character)
        }
    }

    fun updateSkillProficiency(skill: Skill, level: ProficiencyLevel) {
        val currentCharacter = character.value ?: return

        val profMapCopy = currentCharacter.skillProficiencies.toMutableMap()

        profMapCopy[skill] = level

        val updatedCharacter = currentCharacter.copy(skillProficiencies = profMapCopy)

        updateCharacter(updatedCharacter)
    }

    fun updateAbility(ability: Ability, newScore: Int) {
        val currentCharacter = character.value ?: return

        val abilityBlock = currentCharacter.abilityBlock.update(ability, newScore)

        val updatedCharacter = currentCharacter.copy(abilityBlock = abilityBlock)

        updateCharacter(updatedCharacter)
    }

    fun updateSavingThrowProf(ability: Ability, isProf: Boolean) {
        val currentCharacter = character.value ?: return

        val savingThrowProf = currentCharacter.savingThrowProficiencies

        val newProficiencies = if (isProf) {
            savingThrowProf + ability
        } else {
            savingThrowProf - ability
        }

        val updatedCharacter = currentCharacter.copy(
            savingThrowProficiencies = newProficiencies
        )

        updateCharacter(updatedCharacter)
    }

    /**
     * @param delta to cast +1, to undo -1
     */
    fun useSpellSlot(spellLevel: SpellLevel, delta: Int) {
        val currentCharacter = character.value ?: return

        val updatedMap = currentCharacter.spellSettings.spellSlots.toMutableMap()

        val slotData = updatedMap[spellLevel] ?: SpellSlot()

        // To cast: delta is +1 (add to used)
        // To undo: delta is -1 (remove from used)
        val newCurrentValue = (slotData.current + delta).coerceIn(0, slotData.max)

        Log.i("SpellSlot", "$spellLevel value: $newCurrentValue")

        updatedMap[spellLevel] = slotData.copy(current = newCurrentValue)

        val updatedCharacter = currentCharacter.copy(
            spellSettings = currentCharacter.spellSettings.copy(
                spellSlots = updatedMap
            )
        )

        updateCharacter(updatedCharacter)
    }

    fun performLongRest() {
        val currentCharacter = character.value ?: return

        val clearedSlots = currentCharacter.spellSettings.spellSlots.mapValues { (_, slot) ->
            slot.copy(current = 0)
        }

        val restedCharacter = currentCharacter.copy(
            currentHp = currentCharacter.maxHp,
            tempHp = 0,
            spellSettings = currentCharacter.spellSettings.copy(
                spellSlots = clearedSlots
            )
        )

        updateCharacter(restedCharacter)
    }
}