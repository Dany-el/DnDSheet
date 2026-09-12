package com.yablonskyi.model.character

import kotlinx.serialization.Serializable

@Serializable
data class Spell(
    val spellId: Long = 0,
    val name: String = "",
    val school: MagicSchool = MagicSchool.ABJURATION,
    val level: SpellLevel = SpellLevel.CANTRIP,
    val castTime: SpellCastTime = SpellCastTime.ACTION,
    val rangeType: SpellRangeType = SpellRangeType.SELF,
    val rangeValue: Int? = null,
    val components: List<Component> = emptyList(),
    val material: String? = null,
    val isRitual: Boolean = false,
    val duration: SpellDuration = SpellDuration.INSTANTANEOUS,
    val isConcentration: Boolean = false,
    val attackType: AttackType = AttackType.NONE,
    val saveStat: Ability? = null,
    val damageType: DamageType? = null,
    val damageDice: String? = null,
    val description: String = "",
    val higherLevels: String? = null,
)
