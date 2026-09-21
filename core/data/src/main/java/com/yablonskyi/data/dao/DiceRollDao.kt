package com.yablonskyi.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.yablonskyi.data.entity.DiceRollEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DiceRollDao {
    @Query(
        """
        SELECT * FROM dice_rolls
        WHERE characterId = :characterId
        ORDER BY timestamp DESC, id DESC
        """,
    )
    fun observeDiceRolls(characterId: Long): Flow<List<DiceRollEntity>>

    @Insert
    suspend fun insert(diceRoll: DiceRollEntity): Long

    @Query("DELETE FROM dice_rolls WHERE characterId = :characterId")
    suspend fun clear(characterId: Long)
}
