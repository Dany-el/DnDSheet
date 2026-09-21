package com.yablonskyi.data.repository.dice

import com.yablonskyi.data.dao.DiceRollDao
import com.yablonskyi.data.entity.DiceRollEntity
import com.yablonskyi.domain.repository.DiceRollRepository
import com.yablonskyi.model.dice.SavedDiceRoll
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DiceRollRepositoryImpl @Inject constructor(
    private val dao: DiceRollDao,
) : DiceRollRepository {
    override fun observeDiceRolls(characterId: Long): Flow<List<SavedDiceRoll>> =
        dao.observeDiceRolls(characterId).map { rolls -> rolls.map(DiceRollEntity::toModel) }

    override suspend fun addDiceRoll(diceRoll: SavedDiceRoll): Result<Long> = resultOf {
        dao.insert(diceRoll.toEntity())
    }

    override suspend fun clearDiceRolls(characterId: Long): Result<Unit> = resultOf {
        dao.clear(characterId)
    }
}

private inline fun <T> resultOf(block: () -> T): Result<T> = try {
    Result.success(block())
} catch (cancelled: CancellationException) {
    throw cancelled
} catch (error: Exception) {
    Result.failure(DiceRollStorageException(error))
}

class DiceRollStorageException(cause: Throwable) : RuntimeException(
    "Unable to access saved dice rolls",
    cause,
)

private fun SavedDiceRoll.toEntity() = DiceRollEntity(
    id = id,
    characterId = characterId,
    label = label,
    numbers = numbers,
    modifier = modifier,
    result = result,
    dices = dices,
    timestamp = timestamp,
)

private fun DiceRollEntity.toModel() = SavedDiceRoll(
    id = id,
    characterId = characterId,
    label = label,
    numbers = numbers,
    modifier = modifier,
    result = result,
    dices = dices,
    timestamp = timestamp,
)
