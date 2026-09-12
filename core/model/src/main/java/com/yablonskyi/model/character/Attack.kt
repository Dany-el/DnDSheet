package com.yablonskyi.model.character

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
)
