package com.yablonskyi.dndsheet.data.model.character

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "spells")
data class Spell(
    @PrimaryKey(autoGenerate = true) val spellId: Long = 0,
    // Identity
    val name: String = "",
    val school: MagicSchool = MagicSchool.ABJURATION,
    val level: SpellLevel = SpellLevel.CANTRIP,
    // Casting
    val castTime: SpellCastTime = SpellCastTime.ACTION,
    val range: String = "",
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