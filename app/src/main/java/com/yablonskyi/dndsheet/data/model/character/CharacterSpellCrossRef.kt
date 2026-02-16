package com.yablonskyi.dndsheet.data.model.character

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "character_spell_cross_ref",
    primaryKeys = ["characterId","spellId"],
    foreignKeys = [
        ForeignKey(
            entity = Character::class,
            parentColumns = ["id"],
            childColumns = ["characterId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Spell::class,
            parentColumns = ["spellId"],
            childColumns = ["spellId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("characterId"), Index("spellId")]
)
data class CharacterSpellCrossRef(
    val characterId: Long,
    val spellId: Long
)