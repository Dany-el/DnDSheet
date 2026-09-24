package com.yablonskyi.data.mapper

import com.yablonskyi.data.entity.AttackEntity
import com.yablonskyi.model.character.Attack

fun AttackEntity.toModel() = Attack(
    attackId = attackId,
    characterId = characterId,
    name = name,
    attackType = attackType,
    ability = ability,
    isProficient = isProficient,
    bonusToHit = bonusToHit,
    bonusToDamage = bonusToDamage,
    damageDice = damageDice,
    damageType = damageType,
    range = range,
    notes = notes,
    damageMode = damageMode,
    fixedDamage = fixedDamage,
    usages = usages,
    damageAbilityModifier = damageAbilityModifier,
)

fun Attack.toEntity() = AttackEntity(
    attackId = attackId,
    characterId = characterId,
    name = name,
    attackType = attackType,
    ability = ability,
    isProficient = isProficient,
    bonusToHit = bonusToHit,
    bonusToDamage = bonusToDamage,
    damageDice = damageDice,
    damageType = damageType,
    range = range,
    notes = notes,
    damageMode = damageMode,
    fixedDamage = fixedDamage,
    usages = usages,
    damageAbilityModifier = damageAbilityModifier,
)
