package com.yablonskyi.data.mapper

import com.yablonskyi.data.entity.RaceEntity
import com.yablonskyi.model.rulebook.Race

fun RaceEntity.toModel() = Race(
    id = id,
    name = name,
    size = size,
    speed = speed,
    abilityBonuses = abilityBonuses,
    grantedSkills = grantedSkills,
    traits = traits,
    description = description,
    isHomebrew = isHomebrew,
)

fun Race.toEntity() = RaceEntity(
    id = id,
    name = name,
    size = size,
    speed = speed,
    abilityBonuses = abilityBonuses,
    grantedSkills = grantedSkills,
    traits = traits,
    description = description,
    isHomebrew = isHomebrew,
)
