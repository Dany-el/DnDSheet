package com.yablonskyi.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "character_spell_cross_ref",
    primaryKeys = ["characterId", "spellId"],
    foreignKeys = [
        ForeignKey(
            entity = CharacterEntity::class,
            parentColumns = ["id"],
            childColumns = ["characterId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SpellEntity::class,
            parentColumns = ["spellId"],
            childColumns = ["spellId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("characterId"), Index("spellId")]
)
data class CharacterSpellCrossRefEntity(
    val characterId: Long,
    val spellId: Long
)
