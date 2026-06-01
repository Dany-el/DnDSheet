package com.yablonskyi.dndsheet.data.model.rulebook

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.yablonskyi.dndsheet.data.model.character.Ability
import com.yablonskyi.dndsheet.data.model.character.Skill
import com.yablonskyi.dndsheet.data.model.dice.DiceRoles
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "classes")
data class CharacterClass(
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