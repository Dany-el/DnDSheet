package com.yablonskyi.compendium

data class CompendiumNavActions(
    val openRaces: () -> Unit,
    val openClasses: () -> Unit,
    val openSpellsLibrary: () -> Unit,
    val openRaceDetails: (raceId: String) -> Unit,
    val editRace: (raceId: String?) -> Unit,
    val createRace: () -> Unit,
    val openClassDetails: (classId: String) -> Unit,
    val editClass: (classId: String?) -> Unit,
    val createClass: () -> Unit,
    val openSpellDetails: (spellId: Long) -> Unit,
    val createSpell: () -> Unit,
    val back: () -> Unit,
)