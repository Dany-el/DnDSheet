package com.yablonskyi.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.yablonskyi.model.character.Ability
import com.yablonskyi.model.character.AttackType
import com.yablonskyi.model.character.Component
import com.yablonskyi.model.character.DamageType
import com.yablonskyi.model.character.MagicSchool
import com.yablonskyi.model.character.SpellCastTime
import com.yablonskyi.model.character.SpellDuration
import com.yablonskyi.model.character.SpellLevel
import com.yablonskyi.model.character.SpellRangeType

@Entity(
    tableName = "spells",
    indices = [Index(value = ["name"], unique = true)]
)
data class SpellEntity(
    @PrimaryKey(autoGenerate = true) val spellId: Long = 0,
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
