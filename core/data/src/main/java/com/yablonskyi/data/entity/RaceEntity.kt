package com.yablonskyi.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.yablonskyi.model.character.Ability
import com.yablonskyi.model.character.Skill
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(tableName = "races")
data class RaceEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val size: String = "Medium",
    val speed: Int = 30,
    val abilityBonuses: Map<Ability, Int> = emptyMap(),
    val grantedSkills: List<Skill> = emptyList(),
    val traits: List<String> = emptyList(),
    val description: String = "",
    val isHomebrew: Boolean = false,
)
