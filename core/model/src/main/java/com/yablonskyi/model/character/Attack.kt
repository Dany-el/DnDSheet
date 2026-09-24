package com.yablonskyi.model.character

import kotlinx.serialization.Serializable

@Serializable
data class Attack(
    val attackId: Long = 0,
    val characterId: Long = 0,
    val name: String = "",
    val attackType: AttackType = AttackType.NONE,
    val ability: Ability = Ability.NONE,
    val isProficient: Boolean = false,
    val bonusToHit: Int = 0,
    val bonusToDamage: Int = 0,
    val damageDice: String = "",
    val damageType: DamageType = DamageType.SLASHING,
    val range: String = "5",
    val notes: String = "",
    val damageMode: DamageMode = DamageMode.DICE,
    val fixedDamage: Int = 0,
    val usages: Set<AttackUsage> = setOf(AttackUsage.ACTION),
    val damageAbilityModifier: DamageAbilityModifier = DamageAbilityModifier.FULL,
)

@Serializable
enum class DamageMode { DICE, FIXED }

@Serializable
enum class AttackUsage { ACTION, BONUS_ACTION, REACTION }

@Serializable
enum class DamageAbilityModifier {
    FULL, NONE, NEGATIVE_ONLY;

    fun contribution(modifier: Int): Int = when (this) {
        FULL -> modifier
        NONE -> 0
        NEGATIVE_ONLY -> modifier.coerceAtMost(0)
    }
}
