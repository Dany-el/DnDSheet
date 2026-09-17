package com.yablonskyi.character.presentation.dicehistory

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.yablonskyi.character.testutil.MainDispatcherRule
import com.yablonskyi.dice.DiceIntent
import com.yablonskyi.domain.repository.DiceRollRepository
import com.yablonskyi.model.dice.DiceGroup
import com.yablonskyi.model.dice.SavedDiceRoll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

class DiceHistoryViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    @Test
    fun givenSavedRolls_whenHistoryLoads_thenShowsOnlyRouteCharacterNewestFirst() {
        val repository = FakeDiceHistoryRepository().apply {
            rolls.value = listOf(roll(2, 3, 300), roll(1, 2, 200), roll(1, 1, 100))
        }
        val viewModel = viewModel(repository, characterId = 1)
        main.dispatcher.scheduler.runCurrent()

        assertEquals(listOf(2L, 1L), viewModel.state.value.rolls.map { it.id })
        assertFalse(viewModel.state.value.isLoading)
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun givenTwoCharacters_whenHistoryCleared_thenOtherCharacterRemains() {
        val repository = FakeDiceHistoryRepository().apply {
            rolls.value = listOf(roll(1, 1, 100), roll(2, 2, 200))
        }
        val viewModel = viewModel(repository, characterId = 1)
        main.dispatcher.scheduler.runCurrent()

        viewModel.onIntent(DiceIntent.ClearDiceRolls)
        main.dispatcher.scheduler.runCurrent()

        assertEquals(listOf(2L), repository.rolls.value.map { it.characterId })
        assertEquals(emptyList<SavedDiceRoll>(), viewModel.state.value.rolls)
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun givenLoadFailure_whenRetried_thenLoadsOriginalCharacterHistory() {
        val repository = FakeDiceHistoryRepository().apply { observeFailure = true }
        val viewModel = viewModel(repository, characterId = 7)
        main.dispatcher.scheduler.runCurrent()
        assertNotNull(viewModel.state.value.error)

        repository.observeFailure = false
        repository.rolls.value = listOf(roll(7, 4, 400), roll(8, 5, 500))
        viewModel.onIntent(DiceIntent.RetryPersistence)
        main.dispatcher.scheduler.runCurrent()

        assertEquals(listOf(4L), viewModel.state.value.rolls.map { it.id })
        assertEquals(null, viewModel.state.value.error)
        viewModel.viewModelScope.cancel()
    }

    private fun viewModel(repository: DiceRollRepository, characterId: Long) = DiceHistoryViewModel(
        repository,
        SavedStateHandle(mapOf("characterId" to characterId)),
    )

    private fun roll(characterId: Long, id: Long, timestamp: Long) = SavedDiceRoll(
        id = id,
        characterId = characterId,
        label = "Roll $id",
        numbers = listOf(id.toInt()),
        modifier = null,
        result = id.toInt(),
        dices = listOf(DiceGroup(20, 1)),
        timestamp = timestamp,
    )
}

private class FakeDiceHistoryRepository : DiceRollRepository {
    val rolls = MutableStateFlow<List<SavedDiceRoll>>(emptyList())
    var observeFailure = false

    override fun observeDiceRolls(characterId: Long): Flow<List<SavedDiceRoll>> = flow {
        if (observeFailure) error("load failed")
        emitAll(rolls)
    }.filterCharacter(characterId)

    override suspend fun addDiceRoll(diceRoll: SavedDiceRoll): Result<Long> =
        Result.failure(UnsupportedOperationException())

    override suspend fun clearDiceRolls(characterId: Long): Result<Unit> {
        rolls.value = rolls.value.filterNot { it.characterId == characterId }
        return Result.success(Unit)
    }
}

private fun Flow<List<SavedDiceRoll>>.filterCharacter(characterId: Long): Flow<List<SavedDiceRoll>> =
    map { rolls ->
        rolls.filter { it.characterId == characterId }
            .sortedWith(compareByDescending<SavedDiceRoll> { it.timestamp }.thenByDescending { it.id })
    }
