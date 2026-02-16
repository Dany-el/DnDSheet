package com.yablonskyi.dndsheet.data.model.character

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.math.floor

@Entity(tableName = "character")
data class Character(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "",
    val level: Int = 1,
    val imagePath: String? = null,
    // HP
    @ColumnInfo(name = "current_hp") val currentHp: Int = 0,
    @ColumnInfo(name = "max_hp") val maxHp: Int = 0,
    @ColumnInfo(name = "temp_hp") val tempHp: Int = 0,
    @ColumnInfo(name = "hit_dice") val hitDice: String = "",
    // Class
    @ColumnInfo(name = "class") val charClass: String = "",
    @ColumnInfo(name = "subclass") val subClass: String = "",
    // Other
    val race: String = "",
    val speed: Int = 30,
    @ColumnInfo(name = "armor_class") val armorClass: Int = 8,
    val shield: Int = 0,
    @Embedded(prefix = "money_")
    val coins: Money = Money(),
    @ColumnInfo(name = "initiative_bonus") val initiativeMiscBonus: Int = 0,
    val proficiencies: String = "",
    val traits: String = "",
    val feats: String = "",
    val inventory: String = "",
    val backstory: String = "",
    val notes: String = "",
    // Spells
    @Embedded(prefix = "spell_settings_")
    val spellSettings: SpellSettings = SpellSettings(),
    // Abilities
    @Embedded(prefix = "abilities_")
    val abilityBlock: AbilityBlock = AbilityBlock(),
    @ColumnInfo(name = "skill_proficiencies")
    val skillProficiencies: Map<Skill, ProficiencyLevel> = emptyMap(),
    val savingThrowProficiencies: Set<Ability> = emptySet(),
    val passivePerceptionBonus: Int = 0,
    val hasJackOfAllTrades: Boolean = false,
) {
    /**
     * Returns a Proficiency Bonus depending on level
     */
    fun getProfBonus(): Int {
        return ((level - 1) / 4) + 2
    }

    /**
     * Returns an Ability Modifier
     */
    fun getAbilityMod(ability: Ability): Int {
        return abilityBlock.getModifier(ability)
    }

    /**
     * Returns the total Skill Modifier
     */
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

    /**
     * Returns the total Spell Save DC
     */
    fun getSpellSaveDC(): Int {
        val ability = spellSettings.spellCastingAbility ?: return 0
        val abilityMod = abilityBlock.getModifier(ability)

        return 8 + getProfBonus() + abilityMod + spellSettings.dcMiscBonus
    }

    /**
     * Returns the total Spell Attack Bonus
     */
    fun getSpellAttackBonus(): Int {
        if (spellSettings.spellCastingAbility == Ability.NONE) return 0

        val ability = spellSettings.spellCastingAbility ?: return 0
        val abilityMod = abilityBlock.getModifier(ability)

        return getProfBonus() + abilityMod + spellSettings.attackMiscBonus
    }

    /**
     * Calculates the final Saving Throw modifier
     */
    fun getSavingThrowMod(ability: Ability): Int {
        val baseMod = abilityBlock.getModifier(ability)

        return if (savingThrowProficiencies.contains(ability)) {
            baseMod + getProfBonus()
        } else {
            baseMod
        }
    }

    /**
     * Calculates the total initiative modifier
     */
    fun getInitiativeBonus(): Int {
        val dexMod = getAbilityMod(Ability.DEX)
        val bonus =
            if (hasJackOfAllTrades) floor(getProfBonus() * ProficiencyLevel.HALF.multiplier).toInt() else 0
        return dexMod + bonus + initiativeMiscBonus
    }

    /**
     * Returns the passive perception
     */
    fun getPassivePerception(): Int {
        val perMod = getSkillMod(Skill.PERCEPTION)
        return 10 + perMod + passivePerceptionBonus
    }

    fun getTotalAc(): Int {
        return armorClass + shield
    }
}