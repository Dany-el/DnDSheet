package com.yablonskyi.data.converters

import androidx.room.TypeConverter
import com.yablonskyi.model.character.Ability
import com.yablonskyi.model.character.AttackType
import com.yablonskyi.model.character.Component
import com.yablonskyi.model.character.DamageType
import com.yablonskyi.model.character.MagicSchool
import com.yablonskyi.model.character.Note
import com.yablonskyi.model.character.ProficiencyLevel
import com.yablonskyi.model.character.Skill
import com.yablonskyi.model.character.SpellCastTime
import com.yablonskyi.model.character.SpellDuration
import com.yablonskyi.model.character.SpellLevel
import com.yablonskyi.model.character.SpellRangeType
import com.yablonskyi.model.character.SpellSlot
import kotlinx.serialization.json.Json

private val appJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

class Converters {
    @TypeConverter
    fun fromNotes(notes: List<Note>): String = NotesCodec.encode(notes)

    @TypeConverter
    fun toNotes(value: String): List<Note> = NotesCodec.decode(value)

    @TypeConverter
    fun fromSkillMap(value: Map<Skill, ProficiencyLevel>): String {
        return appJson.encodeToString(value)
    }

    @TypeConverter
    fun toSkillMap(value: String): Map<Skill, ProficiencyLevel> {
        return appJson.decodeFromString(value)
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
    fun fromAbility(ability: Ability): String = ability.name

    @TypeConverter
    fun toAbility(value: String): Ability = Ability.valueOf(value)

    @TypeConverter
    fun fromAttackType(type: AttackType): String = type.name

    @TypeConverter
    fun toAttackType(value: String): AttackType = AttackType.valueOf(value)

    @TypeConverter
    fun fromDamageType(type: DamageType): String = type.name

    @TypeConverter
    fun toDamageType(value: String): DamageType = DamageType.valueOf(value)

    @TypeConverter
    fun fromMagicSchool(school: MagicSchool): String = school.name

    @TypeConverter
    fun toMagicSchool(value: String): MagicSchool = MagicSchool.valueOf(value)

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
        return appJson.encodeToString(map)
    }

    @TypeConverter
    fun toSpellSlotsMap(jsonString: String): Map<SpellLevel, SpellSlot> {
        return appJson.decodeFromString(jsonString)
    }

    @TypeConverter
    fun fromRange(rangeType: SpellRangeType): String = rangeType.name

    @TypeConverter
    fun toRange(value: String): SpellRangeType = SpellRangeType.valueOf(value)

    @TypeConverter
    fun fromAbilityIntMap(map: Map<Ability, Int>): String = appJson.encodeToString(map)

    @TypeConverter
    fun toAbilityIntMap(json: String): Map<Ability, Int> {
        return appJson.decodeFromString(json)
    }

    @TypeConverter
    fun fromSkillList(list: List<Skill>): String =
        list.joinToString(",") { it.name }

    @TypeConverter
    fun toSkillList(data: String): List<Skill> =
        if (data.isBlank()) emptyList()
        else data.split(",").mapNotNull { runCatching { Skill.valueOf(it) }.getOrNull() }

    @TypeConverter
    fun fromStringList(list: List<String>): String =
        list.joinToString("|||")

    @TypeConverter
    fun toStringList(data: String): List<String> =
        if (data.isBlank()) emptyList() else data.split("|||")

    @TypeConverter fun damageModeToString(value: com.yablonskyi.model.character.DamageMode): String = value.name
    @TypeConverter fun stringToDamageMode(value: String): com.yablonskyi.model.character.DamageMode = com.yablonskyi.model.character.DamageMode.valueOf(value)
    @TypeConverter fun damageModifierToString(value: com.yablonskyi.model.character.DamageAbilityModifier): String = value.name
    @TypeConverter fun stringToDamageModifier(value: String): com.yablonskyi.model.character.DamageAbilityModifier = com.yablonskyi.model.character.DamageAbilityModifier.valueOf(value)
    @TypeConverter fun attackUsagesToString(value: Set<com.yablonskyi.model.character.AttackUsage>): String = value.sortedBy { it.ordinal }.joinToString(",") { it.name }
    @TypeConverter fun stringToAttackUsages(value: String): Set<com.yablonskyi.model.character.AttackUsage> = value.split(",").filter { it.isNotEmpty() }.map { com.yablonskyi.model.character.AttackUsage.valueOf(it) }.toSet()
}