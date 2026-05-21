package com.yablonskyi.dndsheet.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yablonskyi.dndsheet.data.model.rulebook.Race
import kotlinx.coroutines.flow.Flow

@Dao
interface RaceDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(races: List<Race>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(race: Race)

    @Delete
    suspend fun delete(race: Race)

    @Delete
    suspend fun deleteRaces(races: List<Race>)

    @Query("SELECT * FROM races ORDER BY isHomebrew ASC, name ASC")
    fun getAllRaces(): Flow<List<Race>>

    @Query("SELECT * FROM races WHERE isHomebrew = 1 ORDER BY name ASC")
    fun getHomebrew(): Flow<List<Race>>
}