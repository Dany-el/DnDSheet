package com.yablonskyi.ui.spell

import androidx.compose.runtime.Immutable
import com.yablonskyi.model.character.MagicSchool
import com.yablonskyi.model.character.Spell
import com.yablonskyi.model.character.SpellCastTime
import com.yablonskyi.model.character.SpellDuration
import com.yablonskyi.model.character.SpellLevel

fun filterAndSearchSpells(
    spells: List<Spell>,
    query: String,
    filters: SpellFilterState
): List<Spell> = spells.filter { spell ->
    val matchesSearch = query.isEmpty() || spell.name.contains(query, ignoreCase = true)
    val matchesLevel = filters.levels.isEmpty() || spell.level in filters.levels
    val matchesSchool = filters.schools.isEmpty() || spell.school in filters.schools
    val matchesTime = filters.castTimes.isEmpty() || spell.castTime in filters.castTimes
    val matchesDuration = filters.durations.isEmpty() || spell.duration in filters.durations
    val matchesConcentration = !filters.onlyConcentration || spell.isConcentration
    val matchesRitual = !filters.onlyRitual || spell.isRitual
    matchesSearch && matchesLevel && matchesSchool && matchesTime &&
        matchesDuration && matchesConcentration && matchesRitual
}

@Immutable
data class SpellLibraryItem(
    val spell: Spell,
    val isLearned: Boolean
)

@Immutable
data class SpellFilterState(
    val levels: Set<SpellLevel> = emptySet(),
    val schools: Set<MagicSchool> = emptySet(),
    val castTimes: Set<SpellCastTime> = emptySet(),
    val durations: Set<SpellDuration> = emptySet(),
    val onlyConcentration: Boolean = false,
    val onlyRitual: Boolean = false,
) {
    val isActive: Boolean
        get() = levels.isNotEmpty() || schools.isNotEmpty() ||
            durations.isNotEmpty() || castTimes.isNotEmpty() ||
            onlyConcentration || onlyRitual
}