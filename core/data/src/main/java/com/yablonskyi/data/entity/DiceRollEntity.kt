package com.yablonskyi.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.yablonskyi.model.dice.DiceGroup

@Entity(
    tableName = "dice_rolls",
    foreignKeys = [
        ForeignKey(
            entity = CharacterEntity::class,
            parentColumns = ["id"],
            childColumns = ["characterId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["characterId", "timestamp", "id"])],
)
data class DiceRollEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val characterId: Long,
    val label: String,
    val numbers: List<Int>,
    val modifier: Int?,
    val result: Int,
    val dices: List<DiceGroup>,
    val timestamp: Long,
)
