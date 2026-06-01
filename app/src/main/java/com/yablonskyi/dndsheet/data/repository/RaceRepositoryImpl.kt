package com.yablonskyi.dndsheet.data.repository

import com.yablonskyi.dndsheet.data.dao.RaceDao
import com.yablonskyi.dndsheet.data.model.rulebook.Race
import com.yablonskyi.dndsheet.data.rulebook.BuiltInRulebookLoader
import com.yablonskyi.dndsheet.domain.repository.RaceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class RaceRepositoryImpl @Inject constructor(
    private val loader: BuiltInRulebookLoader,
    private val dao: RaceDao
) : RaceRepository {
    override suspend fun insert(race: Race) = dao.insert(race)

    override suspend fun insertAll(race: List<Race>) = dao.insertAll(race)
    override suspend fun update(race: Race) = dao.update(race)

    override suspend fun delete(race: Race) = dao.delete(race)

    override suspend fun deleteRaces(races: List<Race>) = dao.deleteRaces(races)
    override fun getRaceById(raceId: String): Flow<Race?> = dao.getRaceById(raceId)

    override fun getAllRaces(): Flow<List<Race>> = combine(
        flow { emit(loader.getRaces()) },
        dao.getHomebrew()
    ) { builtIn, homebrew ->
        builtIn + homebrew
    }
}