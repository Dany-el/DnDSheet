package com.yablonskyi.ui.utils

import com.yablonskyi.model.character.Ability
import com.yablonskyi.model.character.Attack
import com.yablonskyi.model.character.Character
import kotlin.math.abs

class AttackCalculator(val character: Character, val attack: Attack) {
    fun getToHitModifier(): Int {
        val abilityMod = character.getAbilityMod(attack.ability)

        val profBonus = if (attack.isProficient) character.getProfBonus() else 0

        // Mod + Prof + Item Bonus
        return abilityMod + profBonus + attack.bonusToHit
    }

    fun getDamageString(): String {
        val abilityMod = if (attack.ability == Ability.NONE) 0 else character.getAbilityMod(attack.ability)
        val totalBonus = attack.damageAbilityModifier.contribution(abilityMod).toLong() + attack.bonusToDamage
        if (attack.damageMode == com.yablonskyi.model.character.DamageMode.FIXED) {
            return (attack.fixedDamage.toLong() + totalBonus).coerceIn(0, Int.MAX_VALUE.toLong()).toString()
        }
        val sign = if (totalBonus >= 0) "+" else "-"
        return "${attack.damageDice} $sign ${abs(totalBonus)}"
    }
}
