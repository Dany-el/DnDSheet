package com.yablonskyi.domain.repository

import com.yablonskyi.model.rulebook.Race
import kotlinx.coroutines.flow.Flow

interface RaceRepository {
    suspend fun insert(race: Race)
    suspend fun insertAll(races: List<Race>)
    suspend fun update(race: Race)
    suspend fun delete(race: Race)
    suspend fun deleteRaces(races: List<Race>)
    fun getRaceById(raceId: String): Flow<Race?>
    fun getAllRaces(): Flow<List<Race>>
}