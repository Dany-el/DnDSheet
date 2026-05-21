package com.yablonskyi.dndsheet.data.model.rulebook

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.yablonskyi.dndsheet.data.model.character.Ability
import com.yablonskyi.dndsheet.data.model.character.Skill

@Entity(tableName = "races")
data class Race(
    @PrimaryKey val id: String,           // e.g. "elf_high"
    val name: String,
    val size: String = "Medium",
    val speed: Int = 30,
    val abilityBonuses: Map<Ability, Int> = emptyMap(),
    val grantedSkills: List<Skill> = emptyList(),
    val traits: List<String> = emptyList(),
    val description: String = "",
    val isHomebrew: Boolean = false,
)