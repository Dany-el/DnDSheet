package com.yablonskyi.data.mapper

import com.yablonskyi.data.entity.AbilityBlockEntity
import com.yablonskyi.model.character.Ability
import com.yablonskyi.model.character.AbilityBlock

fun AbilityBlockEntity.toModel() = AbilityBlock(
    strength = strength,
    dexterity = dexterity,
    constitution = constitution,
    intelligence = intelligence,
    wisdom = wisdom,
    charisma = charisma
)

fun AbilityBlock.toEntity() = AbilityBlockEntity(
    strength = strength,
    dexterity = dexterity,
    constitution = constitution,
    intelligence = intelligence,
    wisdom = wisdom,
    charisma = charisma
)
