package com.yablonskyi.character.attack

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.yablonskyi.domain.repository.AttackRepository
import com.yablonskyi.domain.repository.CharacterRepository
import com.yablonskyi.character.navigation.CharacterSheetRoute
import com.yablonskyi.model.character.Attack
import com.yablonskyi.ui.utils.AttackCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AttackViewModel @Inject constructor(
    private val attackRepository: AttackRepository,
    characterRepository: CharacterRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val args = savedStateHandle.toRoute<CharacterSheetRoute>()
    private val characterId: Long = args.id

    val attackList: StateFlow<List<AttackUiModel>> = combine(
        characterRepository.getCharacterById(characterId),
        attackRepository.getAttacksForCharacter(characterId)
    ) { character, attacks ->
        attacks.map { attack ->
            val calculator = AttackCalculator(character!!, attack)

            AttackUiModel(
                id = attack.attackId,
                name = attack.name,
                toHit = calculator.getToHitModifier().let { if (it >= 0) "+$it" else "$it" },
                damage = calculator.getDamageString(),
                originalAttack = attack
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun saveAttack(attack: Attack) {
        viewModelScope.launch {
            val attackToSave = attack.copy(characterId = characterId)

            if (attackToSave.attackId == 0L) {
                attackRepository.insertAttack(attackToSave)
            } else {
                attackRepository.updateAttack(attackToSave)
            }
        }
    }

    fun deleteAttack(attack: Attack) {
        viewModelScope.launch {
            attackRepository.deleteAttack(attack)
        }
    }
}

data class AttackUiModel(
    val id: Long,
    val name: String,
    val toHit: String,
    val damage: String,
    val originalAttack: Attack
)