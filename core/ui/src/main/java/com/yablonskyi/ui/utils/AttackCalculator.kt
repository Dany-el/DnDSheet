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
        if (attack.ability == Ability.NONE) return ""

        // TODO Ability modifier does not count on bonus action
        val abilityMod = character.getAbilityMod(attack.ability)

        val totalBonus = abilityMod + attack.bonusToDamage

        // "1d6 + 3" or "1d6 - 1"
        val sign = if (totalBonus >= 0) "+" else "-"
        return "${attack.damageDice} $sign ${abs(totalBonus)}"
    }
}