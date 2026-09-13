package com.yablonskyi.data.mapper

import com.yablonskyi.data.entity.CharacterSpellCrossRefEntity
import com.yablonskyi.model.character.CharacterSpellCrossRef

fun CharacterSpellCrossRefEntity.toModel() = CharacterSpellCrossRef(
    characterId = characterId,
    spellId = spellId
)

fun CharacterSpellCrossRef.toEntity() = CharacterSpellCrossRefEntity(
    characterId = characterId,
    spellId = spellId
)
