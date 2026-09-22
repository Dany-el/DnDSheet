package com.yablonskyi.data.repository.dice

import com.yablonskyi.data.dao.DiceRollDao
import com.yablonskyi.data.entity.DiceRollEntity
import com.yablonskyi.domain.backup.BackupAccessGate
import com.yablonskyi.model.dice.DiceGroup
import com.yablonskyi.model.dice.SavedDiceRoll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DiceRollRepositoryTest {
    @Test
    fun givenStoredRolls_whenObservedAndCleared_thenMapsAndScopesHistory() = runTest {
        val dao = FakeDiceRollDao()
        val gate = BackupAccessGate().apply { recover { } }
        val repository = DiceRollRepositoryImpl(dao, gate)
        repository.addDiceRoll(roll(1, "first", 100)).getOrThrow()
        repository.addDiceRoll(roll(2, "other", 200)).getOrThrow()

        val observed = repository.observeDiceRolls(1).first().single()
        assertEquals("first", observed.label)
        assertEquals(listOf(4), observed.numbers)
        assertEquals(listOf(DiceGroup(6, 1)), observed.dices)

        repository.clearDiceRolls(1).getOrThrow()
        assertEquals(emptyList<SavedDiceRoll>(), repository.observeDiceRolls(1).first())
        assertEquals("other", repository.observeDiceRolls(2).first().single().label)
    }

    private fun roll(characterId: Long, label: String, timestamp: Long) = SavedDiceRoll(
        characterId = characterId,
        label = label,
        numbers = listOf(4),
        modifier = null,
        result = 4,
        dices = listOf(DiceGroup(6, 1)),
        timestamp = timestamp,
    )
}

private class FakeDiceRollDao : DiceRollDao {
    private val rolls = MutableStateFlow<List<DiceRollEntity>>(emptyList())
    private var nextId = 1L

    override fun observeDiceRolls(characterId: Long): Flow<List<DiceRollEntity>> =
        rolls.map { values -> values.filter { it.characterId == characterId } }

    override suspend fun insert(diceRoll: DiceRollEntity): Long {
        val id = nextId++
        rolls.value += diceRoll.copy(id = id)
        return id
    }

    override suspend fun clear(characterId: Long) {
        rolls.value = rolls.value.filterNot { it.characterId == characterId }
    }
}
