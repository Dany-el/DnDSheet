package com.yablonskyi.wizard

import com.yablonskyi.model.character.Ability
import com.yablonskyi.model.character.Skill
import com.yablonskyi.model.character.ProficiencyLevel
import com.yablonskyi.model.rulebook.Race
import com.yablonskyi.model.rulebook.CharacterClass
import com.yablonskyi.wizard.viewmodel.AbilityMethod
import com.yablonskyi.wizard.viewmodel.CharacterCreationWizardViewModel
import com.yablonskyi.wizard.viewmodel.WizardAbilityRules
import com.yablonskyi.wizard.viewmodel.WizardEffect
import com.yablonskyi.wizard.viewmodel.WizardIntent
import com.yablonskyi.wizard.viewmodel.WizardStep
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WizardViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    private val characters = FakeCharacterRepository()
    private val races = FakeRaceRepository()
    private val classes = FakeClassRepository()
    private fun vm() = CharacterCreationWizardViewModel(characters, races, classes)
    private val race = Race(id = "r", name = "Elf", abilityBonuses = mapOf(Ability.CON to 2), grantedSkills = listOf(Skill.ATHLETICS))
    private val cls = CharacterClass(id = "c", name = "Fighter", hitDice = "d10", skillChoiceCount = 0, savingThrows = setOf(Ability.STR))
    private fun TestScope.observe(vm: CharacterCreationWizardViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect() }
        runCurrent()
    }
    private fun complete(vm: CharacterCreationWizardViewModel) {
        vm.onIntent(WizardIntent.NameChanged(" Hero "))
        vm.onIntent(WizardIntent.RaceSelected(race))
        vm.onIntent(WizardIntent.ClassSelected(cls))
        vm.onIntent(WizardIntent.AbilityMethodChanged(AbilityMethod.POINT_BUY))
        vm.onIntent(WizardIntent.LevelChanged(3))
        repeat(5) { vm.onIntent(WizardIntent.Next) }
    }
    @Test fun `save uses current snapshot without state subscribers and prevents duplicates`() = runTest {
        val vm = vm()
        complete(vm)
        vm.onIntent(WizardIntent.Finish)
        vm.onIntent(WizardIntent.Finish)
        runCurrent()
        assertEquals(WizardEffect.CharacterCreated(42), vm.effect.first())
        assertEquals(1, characters.saved.size)
        val saved = characters.saved.single()
        assertEquals("Hero", saved.name)
        assertEquals(10, saved.abilityBlock.constitution)
        assertEquals(22, saved.maxHp)
        assertEquals(setOf(Ability.STR), saved.savingThrowProficiencies)
        assertEquals(ProficiencyLevel.PROFICIENT, saved.skillProficiencies[Skill.ATHLETICS])
    }
    @Test fun `failed save retains inputs and permits retry`() = runTest {
        val vm = vm(); observe(vm); complete(vm)
        characters.fail = true
        vm.onIntent(WizardIntent.Finish); runCurrent()
        assertTrue(vm.effect.first() is WizardEffect.ShowSnackbar)
        assertFalse(vm.uiState.value.form.isSubmitting)
        assertEquals(" Hero ", vm.uiState.value.form.name)
        characters.fail = false
        vm.onIntent(WizardIntent.Finish); runCurrent()
        assertEquals(WizardEffect.CharacterCreated(42), vm.effect.first())
    }
    @Test fun `next validates step and back exits only at name`() = runTest {
        val vm = vm(); observe(vm)
        vm.onIntent(WizardIntent.Next); runCurrent()
        assertEquals(WizardStep.NAME, vm.uiState.value.form.step)
        vm.onIntent(WizardIntent.NameChanged("Hero")); vm.onIntent(WizardIntent.Next); runCurrent()
        assertEquals(WizardStep.RACE, vm.uiState.value.form.step)
        vm.onIntent(WizardIntent.Back); vm.onIntent(WizardIntent.Back); runCurrent()
        assertEquals(WizardEffect.NavigateBack, vm.effect.first())
        vm.onIntent(WizardIntent.Close); runCurrent()
        assertEquals(WizardEffect.NavigateBack, vm.effect.first())
    }
    @Test fun `search filters both partitions without selection rebuilding lists`() = runTest {
        races.setRaces(listOf(race, race.copy(id = "h", name = "High Elf", isHomebrew = true)))
        classes.setClasses(listOf(cls))
        val vm = vm(); observe(vm)
        vm.onIntent(WizardIntent.RaceQueryChanged("HIGH")); runCurrent()
        assertTrue(vm.uiState.value.origRaces.isEmpty())
        assertEquals(1, vm.uiState.value.homebrewRaces.size)
        val content = vm.uiState.value.homebrewRaces
        vm.onIntent(WizardIntent.RaceSelected(race)); runCurrent()
        assertSame(content, vm.uiState.value.homebrewRaces)
        vm.onIntent(WizardIntent.ClassQueryChanged("absent")); runCurrent()
        assertTrue(vm.uiState.value.origClasses.isEmpty())
    }
    @Test fun `class changes reset skills and enforce choice limits`() = runTest {
        val vm = vm(); observe(vm)
        vm.onIntent(WizardIntent.ClassSelected(cls.copy(skillChoiceCount = 1)))
        vm.onIntent(WizardIntent.SkillToggled(Skill.ATHLETICS)); runCurrent()
        assertEquals(setOf(Skill.ATHLETICS), vm.uiState.value.form.selectedSkills)
        vm.onIntent(WizardIntent.ClassSelected(cls)); runCurrent()
        assertTrue(vm.uiState.value.form.selectedSkills.isEmpty())
    }
    @Test fun `point buy and level bounds are enforced`() = runTest {
        val vm = vm(); observe(vm)
        vm.onIntent(WizardIntent.AbilityMethodChanged(AbilityMethod.POINT_BUY))
        for (ability in WizardAbilityRules.CORE_ABILITIES) repeat(20) { vm.onIntent(WizardIntent.PointBuyIncremented(ability)) }
        vm.onIntent(WizardIntent.LevelChanged(99)); runCurrent()
        assertTrue(vm.uiState.value.form.pointsSpent <= 27)
        assertEquals(20, vm.uiState.value.form.level)
        assertTrue(vm.uiState.value.form.pointBuyScores.values.all { it in 8..15 })
        repeat(20) { vm.onIntent(WizardIntent.PointBuyDecremented(Ability.STR)) }
        vm.onIntent(WizardIntent.LevelChanged(-1)); runCurrent()
        assertEquals(8, vm.uiState.value.form.pointBuyScores[Ability.STR])
        assertEquals(1, vm.uiState.value.form.level)
    }
    @Test fun `givenAssignedMethods_whenSwitchingBack_thenRetainsScoresAndValidation`() = runTest {
        val vm = vm(); observe(vm)
        WizardAbilityRules.CORE_ABILITIES.zip(listOf(15,14,13,12,10,8)).forEach { (ability, value) ->
            vm.onIntent(WizardIntent.PoolValueSelected(value)); vm.onIntent(WizardIntent.AbilityAssigned(ability))
        }
        runCurrent()
        assertTrue(vm.uiState.value.form.isValid(WizardStep.ABILITIES))
        vm.onIntent(WizardIntent.AbilityUnassigned(Ability.STR)); runCurrent()
        assertFalse(vm.uiState.value.form.isValid(WizardStep.ABILITIES))
        vm.onIntent(WizardIntent.AbilityMethodChanged(AbilityMethod.ROLL)); vm.onIntent(WizardIntent.RollAll)
        WizardAbilityRules.CORE_ABILITIES.forEachIndexed { index, ability ->
            vm.onIntent(WizardIntent.RollIndexSelected(index)); vm.onIntent(WizardIntent.AbilityAssigned(ability))
        }
        runCurrent()
        val state = vm.uiState.value.form
        assertTrue(state.rolledResults.all { it in 3..18 })
        assertTrue(state.isValid(WizardStep.ABILITIES))
        assertEquals(state.rolledResults.first(), state.baseAbilityBlock.getScore(WizardAbilityRules.CORE_ABILITIES.first()))
        vm.onIntent(WizardIntent.AbilityMethodChanged(AbilityMethod.STANDARD_ARRAY)); runCurrent()
        assertEquals(state.rollIndexAssignments, vm.uiState.value.form.rollIndexAssignments)
        assertEquals(state.rolledResults, vm.uiState.value.form.rolledResults)
        assertEquals(14, vm.uiState.value.form.baseAbilityBlock.getScore(Ability.DEX))
        assertFalse(vm.uiState.value.form.isValid(WizardStep.ABILITIES))
        vm.onIntent(WizardIntent.AbilityMethodChanged(AbilityMethod.ROLL)); runCurrent()
        assertTrue(vm.uiState.value.form.isValid(WizardStep.ABILITIES))
        assertEquals(state.baseAbilityBlock, vm.uiState.value.form.baseAbilityBlock)
    }

    @Test fun `givenPendingSelection_whenSelectingCurrentMethod_thenKeepsSelection`() = runTest {
        val vm = vm(); observe(vm)
        vm.onIntent(WizardIntent.PoolValueSelected(15)); runCurrent()
        val before = vm.uiState.value.form
        vm.onIntent(WizardIntent.AbilityMethodChanged(AbilityMethod.STANDARD_ARRAY)); runCurrent()
        assertEquals(before, vm.uiState.value.form)
    }

    @Test fun `givenPendingSelections_whenChangingMethod_thenClearsOnlyPendingSelections`() = runTest {
        val vm = vm(); observe(vm)
        vm.onIntent(WizardIntent.PoolValueSelected(15))
        vm.onIntent(WizardIntent.AbilityAssigned(Ability.STR))
        vm.onIntent(WizardIntent.PoolValueSelected(14))
        vm.onIntent(WizardIntent.AbilityMethodChanged(AbilityMethod.POINT_BUY))
        vm.onIntent(WizardIntent.PointBuyIncremented(Ability.STR)); runCurrent()
        assertNull(vm.uiState.value.form.pendingPoolValue)
        assertEquals(9, vm.uiState.value.form.baseAbilityBlock.getScore(Ability.STR))
        assertTrue(vm.uiState.value.form.isValid(WizardStep.ABILITIES))
        vm.onIntent(WizardIntent.AbilityMethodChanged(AbilityMethod.ROLL))
        vm.onIntent(WizardIntent.RollAll)
        vm.onIntent(WizardIntent.RollIndexSelected(0))
        vm.onIntent(WizardIntent.AbilityMethodChanged(AbilityMethod.STANDARD_ARRAY)); runCurrent()
        assertNull(vm.uiState.value.form.pendingRollIndex)
        assertEquals(15, vm.uiState.value.form.baseAbilityBlock.getScore(Ability.STR))
        vm.onIntent(WizardIntent.AbilityMethodChanged(AbilityMethod.POINT_BUY)); runCurrent()
        assertEquals(9, vm.uiState.value.form.baseAbilityBlock.getScore(Ability.STR))
    }
}
