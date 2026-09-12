package com.yablonskyi.model.character

data class CharacterSheet(
    val character: Character,
    val spells: List<Spell>,
    val attacks: List<Attack>
)
