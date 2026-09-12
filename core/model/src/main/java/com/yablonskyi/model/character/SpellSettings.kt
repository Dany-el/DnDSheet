package com.yablonskyi.model.character

data class SpellSettings(
    val spellCastingAbility: Ability? = null,
    val dcMiscBonus: Int = 0,
    val attackMiscBonus: Int = 0,
    val spellSlots: Map<SpellLevel, SpellSlot> = SpellLevel.entries.filter { !it.isCantrip }.associateWith{ SpellSlot() }
)

data class SpellSlot(
    val max: Int = 0,
    val current: Int = 0
)
