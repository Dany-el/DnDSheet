package com.yablonskyi.data.mapper

import com.yablonskyi.data.entity.CharacterClassEntity
import com.yablonskyi.model.rulebook.CharacterClass

fun CharacterClassEntity.toModel() = CharacterClass(
    id = id,
    name = name,
    hitDice = hitDice,
    primaryAbility = primaryAbility,
    savingThrows = savingThrows,
    skillChoiceCount = skillChoiceCount,
    availableSkills = availableSkills,
    spellcastingAbility = spellcastingAbility,
    description = description,
    isHomebrew = isHomebrew,
)

fun CharacterClass.toEntity() = CharacterClassEntity(
    id = id,
    name = name,
    hitDice = hitDice,
    primaryAbility = primaryAbility,
    savingThrows = savingThrows,
    skillChoiceCount = skillChoiceCount,
    availableSkills = availableSkills,
    spellcastingAbility = spellcastingAbility,
    description = description,
    isHomebrew = isHomebrew,
)
