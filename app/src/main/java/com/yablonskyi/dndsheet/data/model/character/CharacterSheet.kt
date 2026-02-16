package com.yablonskyi.dndsheet.data.model.character

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class CharacterSheet(
    @Embedded val character: Character,
    @Relation(
        parentColumn = "id",
        entityColumn = "spellId",
        associateBy = Junction(
            value = CharacterSpellCrossRef::class,
            parentColumn = "characterId",
            entityColumn = "spellId"
        )
    )
    val spells: List<Spell>,
    @Relation(
        parentColumn = "id",
        entityColumn = "characterId"
    )
    val attacks: List<Attack>
)