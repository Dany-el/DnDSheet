package com.yablonskyi.dndsheet.data.model.character

import androidx.room.ColumnInfo

data class SpellSettings(
    @ColumnInfo(name = "spell_ability")
    val spellCastingAbility: Ability? = null,
    @ColumnInfo(name = "spell_dc_misc_bonus")
    val dcMiscBonus: Int = 0,
    @ColumnInfo(name = "spell_attack_misc_bonus")
    val attackMiscBonus: Int = 0
)