package com.yablonskyi.character.presentation.sheet.mapper

import com.yablonskyi.model.character.Spell
import com.yablonskyi.character.presentation.sheet.model.SpellFilter

internal fun availableSpellFilters(spells: List<Spell>): List<SpellFilter> = buildList {
    add(SpellFilter.All)
    spells.map { it.level.ordinal }.distinct().sorted().forEach { add(SpellFilter.ByLevel(it)) }
    if (spells.any { it.isConcentration }) add(SpellFilter.Concentration)
    if (spells.any { it.isRitual }) add(SpellFilter.Ritual)
}

internal fun filterCharacterSpells(spells: List<Spell>, filter: SpellFilter): List<Spell> = when (filter) {
    SpellFilter.All -> spells.sortedBy { it.level.ordinal }
    is SpellFilter.ByLevel -> spells.filter { it.level.ordinal == filter.level }
    SpellFilter.Concentration -> spells.filter { it.isConcentration }
    SpellFilter.Ritual -> spells.filter { it.isRitual }
}

internal fun SpellFilter.savedValue(): String = when (this) {
    SpellFilter.All -> "all"
    is SpellFilter.ByLevel -> "level:$level"
    SpellFilter.Concentration -> "concentration"
    SpellFilter.Ritual -> "ritual"
}

internal fun restoreSpellFilter(value: String?): SpellFilter = when {
    value == "concentration" -> SpellFilter.Concentration
    value == "ritual" -> SpellFilter.Ritual
    value?.startsWith("level:") == true -> value.substringAfter(':').toIntOrNull()?.let { SpellFilter.ByLevel(it) } ?: SpellFilter.All
    else -> SpellFilter.All
}
