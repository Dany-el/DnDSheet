package com.yablonskyi.dndsheet.data.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yablonskyi.dndsheet.data.model.character.Ability
import com.yablonskyi.dndsheet.data.model.character.Component
import com.yablonskyi.dndsheet.data.model.character.ProficiencyLevel
import com.yablonskyi.dndsheet.data.model.character.Skill
import com.yablonskyi.dndsheet.data.model.character.SpellCastTime
import com.yablonskyi.dndsheet.data.model.character.SpellDuration
import com.yablonskyi.dndsheet.data.model.character.SpellLevel
import com.yablonskyi.dndsheet.data.model.character.SpellRangeType
import com.yablonskyi.dndsheet.data.model.character.SpellSlot

class Converters {
    @TypeConverter
    fun fromSkillMap(value: Map<Skill, ProficiencyLevel>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toSkillMap(value: String): Map<Skill, ProficiencyLevel> {
        val mapType = object : TypeToken<Map<Skill, ProficiencyLevel>>() {}.type
        return Gson().fromJson(value, mapType) ?: emptyMap()
    }

    @TypeConverter
    fun fromComponents(components: List<Component>): String {
        return components.joinToString(",") { it.name }
    }

    @TypeConverter
    fun toComponents(data: String): List<Component> {
        return if (data.isEmpty()) emptyList()
        else data.split(",").map { Component.valueOf(it) }
    }

    @TypeConverter
    fun fromAbilitySet(abilities: Set<Ability>): String {
        return abilities.joinToString(",") { it.name }
    }

    @TypeConverter
    fun toAbilitySet(data: String): Set<Ability> {
        return if (data.isEmpty()) {
            emptySet()
        } else {
            data.split(",")
                .map { Ability.valueOf(it) }
                .toSet()
        }
    }

    @TypeConverter
    fun fromSpellLevel(level: SpellLevel): String = level.name

    @TypeConverter
    fun toSpellLevel(value: String): SpellLevel = SpellLevel.valueOf(value)

    @TypeConverter
    fun fromCastTime(time: SpellCastTime): String = time.name

    @TypeConverter
    fun toCastTime(value: String): SpellCastTime = SpellCastTime.valueOf(value)

    @TypeConverter
    fun fromDuration(duration: SpellDuration): String = duration.name

    @TypeConverter
    fun toDuration(value: String): SpellDuration = SpellDuration.valueOf(value)

    @TypeConverter
    fun fromSpellSlotsMap(map: Map<SpellLevel, SpellSlot>): String {
        return Gson().toJson(map)
    }

    @TypeConverter
    fun toSpellSlotsMap(jsonString: String): Map<SpellLevel, SpellSlot> {
        val type = object : TypeToken<Map<SpellLevel, SpellSlot>>() {}.type
        return try {
            Gson().fromJson(jsonString, type)
        } catch (_: Exception) {
            emptyMap()
        }
    }

    @TypeConverter
    fun fromRange(rangeType: SpellRangeType): String = rangeType.name

    @TypeConverter
    fun toRange(value: String): SpellRangeType = SpellRangeType.valueOf(value)
}