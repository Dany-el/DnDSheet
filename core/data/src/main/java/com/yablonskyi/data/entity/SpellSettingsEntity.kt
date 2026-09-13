package com.yablonskyi.data.entity

import androidx.room.ColumnInfo
import com.yablonskyi.model.character.Ability
import com.yablonskyi.model.character.SpellLevel
import com.yablonskyi.model.character.SpellSlot

data class SpellSettingsEntity(
    @ColumnInfo(name = "spell_ability")
    val spellCastingAbility: Ability? = null,
    @ColumnInfo(name = "spell_dc_misc_bonus")
    val dcMiscBonus: Int = 0,
    @ColumnInfo(name = "spell_attack_misc_bonus")
    val attackMiscBonus: Int = 0,
    @ColumnInfo(name = "spell_slots")
    val spellSlots: Map<SpellLevel, SpellSlot> = SpellLevel.entries.filter { !it.isCantrip }.associateWith { SpellSlot() }
)
