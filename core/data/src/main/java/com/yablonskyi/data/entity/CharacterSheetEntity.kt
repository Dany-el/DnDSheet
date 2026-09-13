package com.yablonskyi.data.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class CharacterSheetEntity(
    @Embedded val character: CharacterEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "spellId",
        associateBy = Junction(
            value = CharacterSpellCrossRefEntity::class,
            parentColumn = "characterId",
            entityColumn = "spellId"
        )
    )
    val spells: List<SpellEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "characterId"
    )
    val attacks: List<AttackEntity>
)
