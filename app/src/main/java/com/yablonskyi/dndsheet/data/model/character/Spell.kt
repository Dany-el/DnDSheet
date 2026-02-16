package com.yablonskyi.dndsheet.data.model.character

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "spells",
    indices = [Index(value = ["name"], unique = true)]
)
data class Spell(
    @PrimaryKey(autoGenerate = true) val spellId: Long = 0,
    // Identity
    val name: String = "",
    val school: MagicSchool = MagicSchool.ABJURATION,
    val level: SpellLevel = SpellLevel.CANTRIP,
    // Casting
    val castTime: SpellCastTime = SpellCastTime.ACTION,
    val rangeType: SpellRangeType = SpellRangeType.SELF,
    val rangeValue: Int? = null,
    val components: List<Component> = emptyList(),
    val material: String? = null,
    val isRitual: Boolean = false,
    // Duration
    val duration: SpellDuration = SpellDuration.INSTANTANEOUS,
    val isConcentration: Boolean = false,
    // Combat
    val attackType: AttackType = AttackType.NONE,
    val saveStat: Ability? = null,
    val damageType: DamageType? = null,
    val damageDice: String? = null,
    // Description
    val description: String = "",
    val higherLevels: String? = null
)