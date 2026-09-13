package com.yablonskyi.compendium.fake

import com.yablonskyi.domain.repository.RaceRepository
import com.yablonskyi.model.rulebook.Race
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeRaceRepository : RaceRepository {

    private val _races = MutableStateFlow<List<Race>>(emptyList())
    val deleted = mutableListOf<Race>()
    val insertedAll = mutableListOf<List<Race>>()
    val updated = mutableListOf<Race>()

    fun setRaces(races: List<Race>) {
        _races.value = races
    }

    fun races(): List<Race> = _races.value

    override fun getAllRaces(): Flow<List<Race>> = _races

    override fun getRaceById(raceId: String): Flow<Race?> =
        MutableStateFlow(_races.value.firstOrNull { it.id == raceId })

    override suspend fun insert(race: Race) {
        _races.value += race
    }

    override suspend fun insertAll(races: List<Race>) {
        insertedAll += races
        _races.value += races
    }

    override suspend fun update(race: Race) {
        updated += race
        _races.value = _races.value.map { if (it.id == race.id) race else it }
    }

    override suspend fun delete(race: Race) {
        deleted += race
        _races.value = _races.value - race
    }

    override suspend fun deleteRaces(races: List<Race>) {
        deleted += races
        _races.value = _races.value - races
    }
}