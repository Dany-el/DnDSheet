package com.yablonskyi.data.entity

import androidx.room.ColumnInfo

data class AbilityBlockEntity(
    @ColumnInfo(name = "strength") val strength: Int = 8,
    @ColumnInfo(name = "dexterity") val dexterity: Int = 8,
    @ColumnInfo(name = "constitution") val constitution: Int = 8,
    @ColumnInfo(name = "intelligence") val intelligence: Int = 8,
    @ColumnInfo(name = "wisdom") val wisdom: Int = 8,
    @ColumnInfo(name = "charisma") val charisma: Int = 8
)
