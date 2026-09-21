package com.yablonskyi.data.converters

import androidx.room.TypeConverter
import com.yablonskyi.model.dice.DiceGroup
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class DiceRollConverters {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @TypeConverter
    fun fromNumbers(numbers: List<Int>): String = json.encodeToString(numbers)

    @TypeConverter
    fun toNumbers(value: String): List<Int> {
        return json.decodeFromString(value)
    }

    @TypeConverter
    fun fromDiceGroups(dices: List<DiceGroup>): String = json.encodeToString(dices)

    @TypeConverter
    fun toDiceGroups(value: String): List<DiceGroup> {
        return json.decodeFromString(value)
    }
}
