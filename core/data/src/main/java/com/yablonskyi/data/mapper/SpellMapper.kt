package com.yablonskyi.data.mapper

import com.yablonskyi.data.entity.SpellEntity
import com.yablonskyi.model.character.Spell

fun SpellEntity.toModel() = Spell(
    spellId = spellId,
    name = name,
    school = school,
    level = level,
    castTime = castTime,
    rangeType = rangeType,
    rangeValue = rangeValue,
    components = components,
    material = material,
    isRitual = isRitual,
    duration = duration,
    isConcentration = isConcentration,
    attackType = attackType,
    saveStat = saveStat,
    damageType = damageType,
    damageDice = damageDice,
    description = description,
    higherLevels = higherLevels,
)

fun Spell.toEntity() = SpellEntity(
    spellId = spellId,
    name = name,
    school = school,
    level = level,
    castTime = castTime,
    rangeType = rangeType,
    rangeValue = rangeValue,
    components = components,
    material = material,
    isRitual = isRitual,
    duration = duration,
    isConcentration = isConcentration,
    attackType = attackType,
    saveStat = saveStat,
    damageType = damageType,
    damageDice = damageDice,
    description = description,
    higherLevels = higherLevels,
)
