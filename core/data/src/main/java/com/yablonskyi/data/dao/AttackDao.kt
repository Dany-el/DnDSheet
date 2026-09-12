package com.yablonskyi.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.yablonskyi.data.entity.AttackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttackDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttack(attack: AttackEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttacks(attack: List<AttackEntity>)

    @Update
    suspend fun updateAttack(attack: AttackEntity)

    @Delete
    suspend fun deleteAttack(attack: AttackEntity)

    @Query("SELECT * FROM attacks WHERE characterId = :charId")
    fun getAttackForCharacter(charId: Long): Flow<List<AttackEntity>>
}
