package com.yablonskyi.dndsheet.data.model.character

import androidx.annotation.StringRes
import androidx.room.ColumnInfo
import com.yablonskyi.dndsheet.R
import kotlin.math.floor

enum class Ability(@StringRes val nameRes: Int) {
    STR(R.string.ability_str),
    DEX(R.string.ability_dex),
    CON(R.string.ability_con),
    INT(R.string.ability_int),
    WIS(R.string.ability_wis),
    CHA(R.string.ability_cha),
    NONE(R.string.ability_none)
}

enum class ProficiencyLevel(val multiplier: Double) {
    NONE(0.0),
    HALF(0.5),
    PROFICIENT(1.0),
    EXPERT(2.0)
}

enum class Skill(
    val defaultAbility: Ability,
    @StringRes val nameRes: Int
) {
    ATHLETICS(Ability.STR, R.string.skill_athletics),

    ACROBATICS(Ability.DEX, R.string.skill_acrobatics),
    SLEIGHT_OF_HAND(Ability.DEX, R.string.skill_sleight_of_hand),
    STEALTH(Ability.DEX, R.string.skill_stealth),

    ARCANA(Ability.INT, R.string.skill_arcana),
    HISTORY(Ability.INT, R.string.skill_history),
    INVESTIGATION(Ability.INT, R.string.skill_investigation),
    NATURE(Ability.INT, R.string.skill_nature),
    RELIGION(Ability.INT, R.string.skill_religion),

    ANIMAL_HANDLING(Ability.WIS, R.string.skill_animal_handling),
    INSIGHT(Ability.WIS, R.string.skill_insight),
    MEDICINE(Ability.WIS, R.string.skill_medicine),
    PERCEPTION(Ability.WIS, R.string.skill_perception),
    SURVIVAL(Ability.WIS, R.string.skill_survival),

    DECEPTION(Ability.CHA, R.string.skill_deception),
    INTIMIDATION(Ability.CHA, R.string.skill_intimidation),
    PERFORMANCE(Ability.CHA, R.string.skill_performance),
    PERSUASION(Ability.CHA, R.string.skill_persuasion)
}

data class AbilityBlock(
    @ColumnInfo(name = "strength") val strength: Int = 8,
    @ColumnInfo(name = "dexterity") val dexterity: Int = 8,
    @ColumnInfo(name = "constitution") val constitution: Int = 8,
    @ColumnInfo(name = "intelligence") val intelligence: Int = 8,
    @ColumnInfo(name = "wisdom") val wisdom: Int = 8,
    @ColumnInfo(name = "charisma") val charisma: Int = 8
) {
    fun getScore(ability: Ability): Int {
        return when (ability) {
            Ability.STR -> strength
            Ability.DEX -> dexterity
            Ability.CON -> constitution
            Ability.INT -> intelligence
            Ability.WIS -> wisdom
            Ability.CHA -> charisma
            Ability.NONE -> 10
        }
    }

    fun update(ability: Ability, newValue: Int): AbilityBlock {
        return when (ability) {
            Ability.STR -> copy(strength = newValue)
            Ability.DEX -> copy(dexterity = newValue)
            Ability.CON -> copy(constitution = newValue)
            Ability.INT -> copy(intelligence = newValue)
            Ability.WIS -> copy(wisdom = newValue)
            Ability.CHA -> copy(charisma = newValue)
            Ability.NONE -> this
        }
    }

    fun getModifier(ability: Ability): Int {
        return floor((getScore(ability) - 10) / 2.0).toInt()
    }
}