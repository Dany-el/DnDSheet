package com.yablonskyi.compendium

import kotlinx.serialization.Serializable

@Serializable
internal object CompendiumRacesRoute

@Serializable
internal data class CompendiumRaceDetailsRoute(val raceId: String)

@Serializable
internal object CompendiumRaceCreateRoute

@Serializable
internal data class CompendiumRaceUpdateRoute(val raceId: String? = null)

@Serializable
internal object CompendiumClassesRoute

@Serializable
internal data class CompendiumClassDetailsRoute(val classId: String)

@Serializable
internal object CompendiumClassCreateRoute

@Serializable
internal data class CompendiumClassUpdateRoute(val classId: String? = null)

@Serializable
internal object CompendiumSpellsLibraryRoute

@Serializable
internal data class CompendiumSpellUpdateRoute(val spellId: Long = 0L)