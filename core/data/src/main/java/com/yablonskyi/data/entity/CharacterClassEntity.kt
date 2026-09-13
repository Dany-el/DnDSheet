package com.yablonskyi.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.yablonskyi.model.character.Ability
import com.yablonskyi.model.character.Skill
import com.yablonskyi.model.dice.DiceRoles
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "classes")
data class CharacterClassEntity(
    @PrimaryKey val id: String,
    val name: String,
    val hitDice: String = DiceRoles.hitDices.first(),
    val primaryAbility: Ability = Ability.STR,
    val savingThrows: Set<Ability> = emptySet(),
    val skillChoiceCount: Int = 2,
    val availableSkills: List<Skill> = emptyList(),
    val spellcastingAbility: Ability? = null,
    val description: String = "",
    val isHomebrew: Boolean = false,
)
