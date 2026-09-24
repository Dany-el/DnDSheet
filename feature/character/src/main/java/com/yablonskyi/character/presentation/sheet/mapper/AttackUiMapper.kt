package com.yablonskyi.character.presentation.sheet.mapper

import com.yablonskyi.model.character.Attack
import com.yablonskyi.model.character.Character
import com.yablonskyi.character.presentation.sheet.model.AttackUiModel
import com.yablonskyi.ui.utils.AttackCalculator

internal fun mapAttacks(character: Character?, attacks: List<Attack>): List<AttackUiModel> {
    if (character == null) return emptyList()
    return attacks.map { attack ->
        val calculator = AttackCalculator(character, attack)
        val hit = calculator.getToHitModifier()
        AttackUiModel(
            id = attack.attackId,
            name = attack.name,
            toHit = if (hit >= 0) "+$hit" else "$hit",
            damage = calculator.getDamageString(),
            calculator = calculator,
            description = attack.notes,
            usages = attack.usages,
            canRollDamage = attack.damageMode == com.yablonskyi.model.character.DamageMode.DICE,
        )
    }
}