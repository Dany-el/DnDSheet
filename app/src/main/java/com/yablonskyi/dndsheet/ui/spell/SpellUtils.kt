package com.yablonskyi.dndsheet.ui.spell

import com.yablonskyi.dndsheet.data.model.character.MagicSchool
import com.yablonskyi.dndsheet.data.model.character.Spell
import com.yablonskyi.dndsheet.data.model.character.SpellCastTime
import com.yablonskyi.dndsheet.data.model.character.SpellDuration
import com.yablonskyi.dndsheet.data.model.character.SpellLevel

fun filterAndSearchSpells(
    spells: List<Spell>,
    query: String,
    filters: SpellFilterState
): List<Spell> {
    return spells.filter { spell ->
        val matchesSearch = query.isEmpty() || spell.name.contains(query, ignoreCase = true)

        val matchesLevel = filters.levels.isEmpty() || spell.level in filters.levels
        val matchesSchool = filters.schools.isEmpty() || spell.school in filters.schools
        val matchesTime = filters.castTimes.isEmpty() || spell.castTime in filters.castTimes
        val matchesDuration = filters.durations.isEmpty() || spell.duration in filters.durations

        val matchesConc = !filters.onlyConcentration || spell.isConcentration
        val matchesRitual = !filters.onlyRitual || spell.isRitual

        matchesSearch && matchesLevel && matchesSchool && matchesTime &&
                matchesDuration && matchesConc && matchesRitual
    }
}

data class SpellLibraryItem(
    val spell: Spell,
    val isLearned: Boolean
)

data class SpellFilterState(
    val levels: Set<SpellLevel> = emptySet(),
    val schools: Set<MagicSchool> = emptySet(),
    val castTimes: Set<SpellCastTime> = emptySet(),
    val durations: Set<SpellDuration> = emptySet(),

//    val ranges: Set<String> = emptySet(),

    val onlyConcentration: Boolean = false,
    val onlyRitual: Boolean = false
)