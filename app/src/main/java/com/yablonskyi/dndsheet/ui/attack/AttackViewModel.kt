package com.yablonskyi.dndsheet.ui.attack

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.yablonskyi.dndsheet.data.model.character.Ability
import com.yablonskyi.dndsheet.data.model.character.Attack
import com.yablonskyi.dndsheet.data.model.character.Character
import com.yablonskyi.dndsheet.domain.repository.AttackRepository
import com.yablonskyi.dndsheet.domain.repository.CharacterRepository
import com.yablonskyi.dndsheet.ui.navigation.CharacterSheetRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class AttackViewModel @Inject constructor(
    private val attackRepository: AttackRepository,
    characterRepository: CharacterRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val args = savedStateHandle.toRoute<CharacterSheetRoute>()
    private val characterId: Long = args.id

    init {
        Log.i("AttackViewModel", "Loaded character with ID: $characterId")
    }

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
                Log.i("AttackViewModel", "Inserting new attack: $attackToSave")
                attackRepository.insertAttack(attackToSave)
            } else {
                Log.i("AttackViewModel", "Updating existing attack: $attackToSave")
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

data class AttackCalculator(val character: Character, val attack: Attack) {
    fun getToHitModifier(): Int {
        val abilityMod = character.getAbilityMod(attack.ability)

        val profBonus = if (attack.isProficient) character.getProfBonus() else 0

        // Mod + Prof + Item Bonus
        return abilityMod + profBonus + attack.bonusToHit
    }

    fun getDamageString(): String {
        if (attack.ability == Ability.NONE) return ""

        val abilityMod = character.getAbilityMod(attack.ability)

        val totalBonus = abilityMod + attack.bonusToDamage

        // "1d6 + 3" or "1d6 - 1"
        val sign = if (totalBonus >= 0) "+" else "-"
        return "${attack.damageDice} $sign ${abs(totalBonus)}"
    }
}