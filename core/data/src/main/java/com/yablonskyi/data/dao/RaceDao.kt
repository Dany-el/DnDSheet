package com.yablonskyi.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.yablonskyi.data.entity.RaceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RaceDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(races: List<RaceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(race: RaceEntity)

    @Update
    suspend fun update(race: RaceEntity)

    @Delete
    suspend fun delete(race: RaceEntity)

    @Delete
    suspend fun deleteRaces(races: List<RaceEntity>)

    @Query("SELECT * FROM races WHERE id = :raceId")
    fun getRaceById(raceId: String): Flow<RaceEntity?>

    @Query("SELECT * FROM races ORDER BY isHomebrew ASC, name ASC")
    fun getAllRaces(): Flow<List<RaceEntity>>

    @Query("SELECT * FROM races WHERE isHomebrew = 1 ORDER BY name ASC")
    fun getHomebrew(): Flow<List<RaceEntity>>
}
