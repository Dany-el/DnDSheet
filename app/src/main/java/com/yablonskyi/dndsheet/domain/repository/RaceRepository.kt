package com.yablonskyi.dndsheet.domain.repository

import com.yablonskyi.dndsheet.data.model.rulebook.Race
import kotlinx.coroutines.flow.Flow

interface RaceRepository {
    suspend fun insert(race: Race)
    suspend fun insertAll(race: List<Race>)
    suspend fun update(race: Race)
    suspend fun delete(race: Race)
    suspend fun deleteRaces(races: List<Race>)
    fun getRaceById(raceId: String): Flow<Race?>
    fun getAllRaces(): Flow<List<Race>>
}