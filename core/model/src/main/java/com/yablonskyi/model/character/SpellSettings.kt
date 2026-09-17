package com.yablonskyi.model.character

import kotlinx.serialization.Serializable

@Serializable
data class SpellSettings(
    val spellCastingAbility: Ability? = null,
    val dcMiscBonus: Int = 0,
    val attackMiscBonus: Int = 0,
    val spellSlots: Map<SpellLevel, SpellSlot> = SpellLevel.entries.filter { !it.isCantrip }.associateWith{ SpellSlot() }
)

@Serializable
data class SpellSlot(
    val max: Int = 0,
    val current: Int = 0
)
