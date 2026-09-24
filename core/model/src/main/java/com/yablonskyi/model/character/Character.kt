package com.yablonskyi.model.character

import kotlin.math.floor
import kotlinx.serialization.Serializable

@Serializable
data class Character(
    val id: Long = 0,
    val name: String = "",
    val level: Int = 1,
    val imagePath: String? = null,
    // HP
    val currentHp: Int = 0,
    val maxHp: Int = 0,
    val tempHp: Int = 0,
    val hitDice: String = "",
    // Class
    val charClass: String = "",
    val subClass: String = "",
    // Other
    val race: String = "",
    val speed: Int = 30,
    val armorClass: Int = 8,
    val shield: Int = 0,
    val coins: Money = Money(),
    val initiativeMiscBonus: Int = 0,
    val proficiencies: String = "",
    val traits: String = "",
    val feats: String = "",
    val inventory: String = "",
    val backstory: String = "",
    val notes: List<Note> = emptyList(),
    // Spells
    val spellSettings: SpellSettings = SpellSettings(),
    // Abilities
    val abilityBlock: AbilityBlock = AbilityBlock(),
    val skillProficiencies: Map<Skill, ProficiencyLevel> = emptyMap(),
    val savingThrowProficiencies: Set<Ability> = emptySet(),
    // Other
    val passivePerceptionBonus: Int = 0,
    val hasJackOfAllTrades: Boolean = false,
) {
    fun getProfBonus(): Int {
        return ((level - 1) / 4) + 2
    }

    fun getAbilityMod(ability: Ability): Int {
        return abilityBlock.getModifier(ability)
    }

    fun getSkillMod(skill: Skill): Int {
        val abilityMod = getAbilityMod(skill.defaultAbility)

        val proficiency = skillProficiencies[skill] ?: ProficiencyLevel.NONE
        val multiplier = proficiency.multiplier

        val bonus = when {
            proficiency != ProficiencyLevel.NONE -> {
                floor(getProfBonus() * multiplier).toInt()
            }

            hasJackOfAllTrades -> {
                floor(getProfBonus() * ProficiencyLevel.HALF.multiplier).toInt()
            }

            else -> 0
        }

        return abilityMod + bonus
    }

    fun getSpellSaveDC(): Int {
        val ability = spellSettings.spellCastingAbility ?: return 0
        val abilityMod = abilityBlock.getModifier(ability)

        return 8 + getProfBonus() + abilityMod + spellSettings.dcMiscBonus
    }

    fun getSpellAttackBonus(): Int {
        if (spellSettings.spellCastingAbility == Ability.NONE) return 0

        val ability = spellSettings.spellCastingAbility ?: return 0
        val abilityMod = abilityBlock.getModifier(ability)

        return getProfBonus() + abilityMod + spellSettings.attackMiscBonus
    }

    fun getSavingThrowMod(ability: Ability): Int {
        val baseMod = abilityBlock.getModifier(ability)

        return if (savingThrowProficiencies.contains(ability)) {
            baseMod + getProfBonus()
        } else {
            baseMod
        }
    }

    val initiativeBonus: Int = getAbilityMod(Ability.DEX) + initiativeMiscBonus

    val passivePerception: Int =
        10 + getSkillMod(Skill.PERCEPTION) + passivePerceptionBonus
}
