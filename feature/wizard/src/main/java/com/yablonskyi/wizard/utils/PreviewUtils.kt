package com.yablonskyi.wizard.utils

import com.yablonskyi.model.character.AbilityBlock
import com.yablonskyi.model.rulebook.CharacterClass
import com.yablonskyi.wizard.viewmodel.WizardFormState
import com.yablonskyi.wizard.viewmodel.AbilityMethod
import com.yablonskyi.wizard.viewmodel.WizardAbilityRules
import com.yablonskyi.model.character.Ability
import com.yablonskyi.model.character.Skill
import com.yablonskyi.model.rulebook.Race

internal object PreviewUtils {
    private val human = Race(
        id = "race-human",
        name = "Human",
        size = "Medium",
        speed = 30,
        abilityBonuses = Ability.playableAbilities.associateWith { 1 },
        grantedSkills = listOf(Skill.PERCEPTION),
        traits = listOf("Versatile", "Ambitious"),
        description = "A versatile and ambitious people who can thrive in any environment."
    )

    private val woodElf = Race(
        id = "race-wood-elf",
        name = "Wood Elf Wood Elf Wood Elf Wood Elf",
        size = "Medium",
        speed = 35,
        abilityBonuses = mapOf(Ability.DEX to 2, Ability.WIS to 1),
        grantedSkills = listOf(Skill.PERCEPTION, Skill.SURVIVAL),
        traits = listOf("Fey Ancestry", "Trance", "Mask of the Wild"),
        description = "Guardians of ancient forests who value freedom and the natural world.",
        isHomebrew = true
    )

    val origRaces = listOf(human, human.copy(id = "2"), human.copy(id = "3"))
    val homebrewRaces = listOf(woodElf, woodElf.copy(id = "4", name = "Name Real"), woodElf.copy(id = "5"))

    val characterName = "Arin Oakheart"
    val fighter = CharacterClass(
        id = "class-fighter", name = "Fighter", hitDice = "d10",
        primaryAbility = Ability.STR, savingThrows = setOf(Ability.STR, Ability.CON),
        skillChoiceCount = 2, availableSkills = listOf(Skill.ATHLETICS, Skill.PERCEPTION, Skill.SURVIVAL),
        description = "A skilled warrior trained in weapons and armor."
    )
    val origClasses = listOf(fighter)
    val homebrewClasses = listOf(fighter.copy(id = "class-warden", name = "Warden", isHomebrew = true))
    val selectedSkills = setOf(Skill.ATHLETICS)
    val abilityScores = mapOf(
        Ability.STR to 15, Ability.DEX to 14, Ability.CON to 13,
        Ability.INT to 12, Ability.WIS to 10, Ability.CHA to 8
    )
    val abilityBlock = abilityScores.entries.fold(AbilityBlock()) { block, (ability, score) -> block.update(ability, score) }
    val pointBuyScores = abilityScores
    val pointsSpent = pointBuyScores.values.sumOf { WizardAbilityRules.POINT_BUY_COSTS.getValue(it) }
    val rolledResults = listOf(16, 14, 13, 12, 10, 8)
    val levelState = WizardFormState(
        selectedRace = human, selectedClass = fighter, level = 3,
        abilityMethod = AbilityMethod.STANDARD_ARRAY, standardAssignments = abilityScores
    )
}