package com.yablonskyi.data.repository.compendium

import com.yablonskyi.data.dao.RaceDao
import com.yablonskyi.data.mapper.toEntity
import com.yablonskyi.data.mapper.toModel
import com.yablonskyi.data.rulebook.BuiltInRulebookLoader
import com.yablonskyi.domain.backup.BackupAccessGate
import com.yablonskyi.domain.repository.RaceRepository
import com.yablonskyi.model.rulebook.Race
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RaceRepositoryImpl @Inject constructor(
    private val loader: BuiltInRulebookLoader,
    private val dao: RaceDao,
    private val backupGate: BackupAccessGate,
) : RaceRepository {
    override suspend fun insert(race: Race) = backupGate.access { dao.insert(race.toEntity()) }

    override suspend fun insertAll(races: List<Race>) = backupGate.access { dao.insertAll(races.map { it.toEntity() }) }
    override suspend fun update(race: Race) = backupGate.access { dao.update(race.toEntity()) }

    override suspend fun delete(race: Race) = backupGate.access { dao.delete(race.toEntity()) }

    override suspend fun deleteRaces(races: List<Race>) = backupGate.access { dao.deleteRaces(races.map { it.toEntity() }) }
    override fun getRaceById(raceId: String): Flow<Race?> = combine(
        flow { emit(loader.getRaces().map { it.toModel() }) },
        dao.getRaceById(raceId).map { it?.toModel() }
    ) { builtIn, homebrew ->
        homebrew ?: builtIn.firstOrNull { it.id == raceId }
    }

    override fun getAllRaces(): Flow<List<Race>> = combine(
        flow { emit(loader.getRaces().map { it.toModel() }) },
        dao.getHomebrew().map { entities -> entities.map { it.toModel() } }
    ) { builtIn, homebrew ->
        builtIn + homebrew
    }
}
