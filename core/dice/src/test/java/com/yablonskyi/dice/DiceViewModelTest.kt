package com.yablonskyi.dice

import com.yablonskyi.domain.repository.DiceRollRepository
import com.yablonskyi.model.character.Ability
import com.yablonskyi.model.character.Skill
import com.yablonskyi.model.dice.DiceGroup
import com.yablonskyi.model.dice.SavedDiceRoll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import androidx.lifecycle.viewModelScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DiceViewModelTest {
    @Test
    fun givenValidRoll_whenRolled_thenSavesDisplayedSnapshotForCharacter() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        lateinit var viewModel: DiceViewModel
        try {
            val repository = FakeDiceRollRepository()
            viewModel = viewModel(repository, timestamp = 1234L)
            viewModel.onIntent(7, DiceIntent.RegularRoll(linkedMapOf(6 to 2, 8 to 1), -2))
            runCurrent()

            val state = viewModel.diceRollState.value
            val saved = repository.rolls.value.single()
            assertEquals(7, saved.characterId)
            assertEquals("LABEL", saved.label)
            assertEquals(state.numbers, saved.numbers)
            assertEquals(state.result, saved.result)
            assertEquals(-2, saved.modifier)
            assertEquals(listOf(DiceGroup(6, 2), DiceGroup(8, 1)), saved.dices)
            assertEquals(1234L, saved.timestamp)
            assertEquals(saved.numbers.sum() - 2, saved.result)
        } finally { viewModel.viewModelScope.cancel(); Dispatchers.resetMain() }
    }

    @Test
    fun givenInvalidRoll_whenDispatched_thenDoesNotSaveOrShowResult() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        lateinit var viewModel: DiceViewModel
        try {
            val repository = FakeDiceRollRepository()
            viewModel = viewModel(repository)
            viewModel.onIntent(7, DiceIntent.RegularStringRoll("2d0 + 3 trailing"))
            viewModel.onIntent(0, DiceIntent.RegularRoll(mapOf(6 to 1)))
            runCurrent()
            assertTrue(repository.rolls.value.isEmpty())
            assertFalse(viewModel.diceRollState.value.showResult)
        } finally { viewModel.viewModelScope.cancel(); Dispatchers.resetMain() }
    }

    @Test
    fun givenQueuedRollClearRoll_whenProcessed_thenOnlyLastRollRemains() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        lateinit var viewModel: DiceViewModel
        try {
            val repository = FakeDiceRollRepository()
            viewModel = viewModel(repository)
            viewModel.onIntent(7, DiceIntent.RegularRoll(mapOf(6 to 1)))
            viewModel.onIntent(7, DiceIntent.ClearDiceRolls)
            viewModel.onIntent(7, DiceIntent.RegularRoll(mapOf(8 to 1)))
            runCurrent()
            assertEquals(listOf(DiceGroup(8, 1)), repository.rolls.value.single().dices)
            assertEquals(listOf("add", "clear", "add"), repository.operations)
        } finally { viewModel.viewModelScope.cancel(); Dispatchers.resetMain() }
    }

    @Test
    fun givenTypedRolls_whenRolled_thenLabelsIncludeTypeAndName() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        lateinit var viewModel: DiceViewModel
        try {
            viewModel = viewModel(FakeDiceRollRepository())
            val ability = Ability.entries.first()
            val skill = Skill.entries.first()
            val rolls = listOf(
                DiceIntent.AbilityCheckRoll(ability) to DiceRollLabel.TypeStringRes(com.yablonskyi.ui.R.string.check, ability.nameRes, true),
                DiceIntent.SkillCheckRoll(skill) to DiceRollLabel.TypeStringRes(com.yablonskyi.ui.R.string.check, skill.nameRes),
                DiceIntent.SaveThrowRoll(ability) to DiceRollLabel.TypeStringRes(R.string.roll_type_save, ability.nameRes, true),
            )
            for ((intent, expectedLabel) in rolls) {
                viewModel.onIntent(7, intent)
                runCurrent()
                assertEquals(expectedLabel, viewModel.diceRollState.value.labelRes)
            }
        } finally { viewModel.viewModelScope.cancel(); Dispatchers.resetMain() }
    }

    private fun viewModel(repository: DiceRollRepository, timestamp: Long = 1L) = DiceViewModel(
        repository,
        DiceRollClock { timestamp },
        DiceRollLabelResolver { "LABEL" },
    )
}

private class FakeDiceRollRepository : DiceRollRepository {
    val rolls = MutableStateFlow<List<SavedDiceRoll>>(emptyList())
    val operations = mutableListOf<String>()
    private var nextId = 1L

    override fun observeDiceRolls(characterId: Long): Flow<List<SavedDiceRoll>> =
        rolls.map { values -> values.filter { it.characterId == characterId } }

    override suspend fun addDiceRoll(diceRoll: SavedDiceRoll): Result<Long> {
        operations += "add"
        val id = nextId++
        rolls.value += diceRoll.copy(id = id)
        return Result.success(id)
    }

    override suspend fun clearDiceRolls(characterId: Long): Result<Unit> {
        operations += "clear"
        rolls.value = rolls.value.filterNot { it.characterId == characterId }
        return Result.success(Unit)
    }
}
