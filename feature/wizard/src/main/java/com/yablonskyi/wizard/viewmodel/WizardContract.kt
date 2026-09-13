package com.yablonskyi.wizard.viewmodel

import androidx.annotation.StringRes
import com.yablonskyi.model.character.Ability
import com.yablonskyi.model.character.AbilityBlock
import com.yablonskyi.model.character.Skill
import com.yablonskyi.model.rulebook.CharacterClass
import com.yablonskyi.model.rulebook.Race
import kotlin.math.floor

object WizardAbilityRules {
    val STANDARD_ARRAY = listOf(15, 14, 13, 12, 10, 8)
    val POINT_BUY_COSTS = mapOf(8 to 0, 9 to 1, 10 to 2, 11 to 3, 12 to 4, 13 to 5, 14 to 7, 15 to 9)
    const val POINT_BUY_BUDGET = 27
    val CORE_ABILITIES = Ability.entries.filter { it != Ability.NONE }
}

enum class WizardStep { NAME, RACE, ABILITIES, CLASS, SKILLS, LEVEL }
enum class AbilityMethod { STANDARD_ARRAY, POINT_BUY, ROLL }

data class WizardFormState(
    val step: WizardStep = WizardStep.NAME,
    val name: String = "",
    val selectedRace: Race? = null,
    val selectedClass: CharacterClass? = null,
    val selectedSkills: Set<Skill> = emptySet(),
    val raceQuery: String = "",
    val classQuery: String = "",
    val abilityMethod: AbilityMethod = AbilityMethod.STANDARD_ARRAY,
    val standardAssignments: Map<Ability, Int> = emptyMap(),
    val pendingPoolValue: Int? = null,
    val pointBuyScores: Map<Ability, Int> = WizardAbilityRules.CORE_ABILITIES.associateWith { 8 },
    val rolledResults: List<Int> = emptyList(),
    val pendingRollIndex: Int? = null,
    val rollIndexAssignments: Map<Ability, Int> = emptyMap(),
    val level: Int = 1,
    val isSubmitting: Boolean = false
) {
    val pointsSpent: Int get() = pointBuyScores.values.sumOf { WizardAbilityRules.POINT_BUY_COSTS[it] ?: 0 }
    val availableSkills: List<Skill> get() = selectedClass?.availableSkills?.ifEmpty { Skill.entries.toList() } ?: emptyList()
    val maxSkills: Int get() = selectedClass?.skillChoiceCount ?: 0
    val baseAbilityBlock: AbilityBlock get() = WizardAbilityRules.CORE_ABILITIES.fold(AbilityBlock()) { block, ability ->
        val score = when (abilityMethod) {
            AbilityMethod.STANDARD_ARRAY -> standardAssignments[ability] ?: 8
            AbilityMethod.POINT_BUY -> pointBuyScores[ability] ?: 8
            AbilityMethod.ROLL -> rollIndexAssignments[ability]?.let { rolledResults.getOrNull(it) } ?: 8
        }
        block.update(ability, score)
    }
    val calculatedHp: Int get() {
        val cls = selectedClass ?: return 0
        val sides = cls.hitDice.drop(1).toIntOrNull() ?: 8
        val con = baseAbilityBlock.constitution + (selectedRace?.abilityBonuses?.get(Ability.CON) ?: 0)
        val modifier = floor((con - 10) / 2.0).toInt()
        return maxOf(1, sides + modifier + ((sides / 2 + 1 + modifier) * (level - 1)))
    }
    fun isValid(step: WizardStep): Boolean = when (step) {
        WizardStep.NAME -> name.isNotBlank()
        WizardStep.RACE -> selectedRace != null
        WizardStep.ABILITIES -> when (abilityMethod) {
            AbilityMethod.STANDARD_ARRAY -> standardAssignments.size == 6
            AbilityMethod.POINT_BUY -> true
            AbilityMethod.ROLL -> rolledResults.size == 6 && rollIndexAssignments.size == 6
        }
        WizardStep.CLASS -> selectedClass != null
        WizardStep.SKILLS -> selectedSkills.size == maxSkills
        WizardStep.LEVEL -> true
    }
    val canProceed: Boolean get() = !isSubmitting && isValid(step)
}

data class WizardUiState(
    val form: WizardFormState = WizardFormState(),
    val origRaces: List<Race> = emptyList(),
    val homebrewRaces: List<Race> = emptyList(),
    val origClasses: List<CharacterClass> = emptyList(),
    val homebrewClasses: List<CharacterClass> = emptyList(),
    val isLoading: Boolean = true
)

sealed interface WizardIntent {
    data class NameChanged(val value: String) : WizardIntent
    data class RaceQueryChanged(val value: String) : WizardIntent
    data class ClassQueryChanged(val value: String) : WizardIntent
    data class RaceSelected(val race: Race) : WizardIntent
    data class ClassSelected(val characterClass: CharacterClass) : WizardIntent
    data class SkillToggled(val skill: Skill) : WizardIntent
    data class AbilityMethodChanged(val method: AbilityMethod) : WizardIntent
    data class PoolValueSelected(val value: Int) : WizardIntent
    data class AbilityAssigned(val ability: Ability) : WizardIntent
    data class AbilityUnassigned(val ability: Ability) : WizardIntent
    data class PointBuyIncremented(val ability: Ability) : WizardIntent
    data class PointBuyDecremented(val ability: Ability) : WizardIntent
    data class RollIndexSelected(val index: Int) : WizardIntent
    data class LevelChanged(val value: Int) : WizardIntent
    data object RollAll : WizardIntent
    data object Next : WizardIntent
    data object Back : WizardIntent
    data object Close : WizardIntent
    data object Finish : WizardIntent
}

sealed interface WizardEffect {
    data object NavigateBack : WizardEffect
    data class CharacterCreated(val id: Long) : WizardEffect
    data class ShowSnackbar(@param:StringRes val messageRes: Int) : WizardEffect
}