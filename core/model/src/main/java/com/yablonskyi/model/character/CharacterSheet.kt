package com.yablonskyi.model.character

import kotlinx.serialization.Serializable

@Serializable
data class CharacterSheet(
    val character: Character,
    val spells: List<Spell>,
    val attacks: List<Attack>
)
