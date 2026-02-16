package com.yablonskyi.dndsheet.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.yablonskyi.dndsheet.data.model.character.Attack
import kotlinx.coroutines.flow.Flow

@Dao
interface AttackDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttack(attack: Attack) : Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttacks(attack: List<Attack>)

    @Update
    suspend fun updateAttack(attack: Attack)

    @Delete
    suspend fun deleteAttack(attack: Attack)

    @Query("SELECT * FROM attacks WHERE characterId = :charId")
    fun getAttackForCharacter(charId: Long): Flow<List<Attack>>
}