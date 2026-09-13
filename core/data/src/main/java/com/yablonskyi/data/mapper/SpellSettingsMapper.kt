package com.yablonskyi.data.mapper

import com.yablonskyi.data.entity.SpellSettingsEntity
import com.yablonskyi.model.character.SpellSettings

fun SpellSettingsEntity.toModel() = SpellSettings(
    spellCastingAbility = spellCastingAbility,
    dcMiscBonus = dcMiscBonus,
    attackMiscBonus = attackMiscBonus,
    spellSlots = spellSlots
)

fun SpellSettings.toEntity() = SpellSettingsEntity(
    spellCastingAbility = spellCastingAbility,
    dcMiscBonus = dcMiscBonus,
    attackMiscBonus = attackMiscBonus,
    spellSlots = spellSlots
)
