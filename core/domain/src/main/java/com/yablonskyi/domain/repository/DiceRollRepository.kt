package com.yablonskyi.domain.repository

import com.yablonskyi.model.dice.SavedDiceRoll
import kotlinx.coroutines.flow.Flow

interface DiceRollRepository {
    fun observeDiceRolls(characterId: Long): Flow<List<SavedDiceRoll>>

    suspend fun addDiceRoll(diceRoll: SavedDiceRoll): Result<Long>

    suspend fun clearDiceRolls(characterId: Long): Result<Unit>
}
