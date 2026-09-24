package com.yablonskyi.domain.character

import com.yablonskyi.model.character.*

/** A field change applied against the latest persisted character, never a UI snapshot. */
sealed interface CharacterChange {
    data class Text(val field: CharacterTextField, val value: String) : CharacterChange
    data class Number(val field: CharacterNumberField, val value: Int) : CharacterChange
    data class Health(val current: Int, val max: Int, val temp: Int) : CharacterChange
    data class Coins(val value: Money) : CharacterChange
    data class AbilityScore(val ability: Ability, val score: Int) : CharacterChange
    data class SkillProficiency(val skill: Skill, val level: ProficiencyLevel) : CharacterChange
    data class SavingThrow(val ability: Ability, val proficient: Boolean) : CharacterChange
    data class CastingAbility(val ability: Ability?) : CharacterChange
    data class SlotMaximum(val level: SpellLevel, val maximum: Int) : CharacterChange
    data class SlotUsed(val level: SpellLevel, val delta: Int) : CharacterChange
    data class JackOfAllTrades(val enabled: Boolean) : CharacterChange
    data class Image(val path: String?) : CharacterChange
    data class AddNote(val note: Note) : CharacterChange
    data class UpdateNote(val original: Note, val note: Note) : CharacterChange
    data class DeleteNote(val noteId: String) : CharacterChange
    data object LongRest : CharacterChange
}

enum class CharacterTextField { NAME, RACE, CHAR_CLASS, SUB_CLASS, HIT_DICE, PROFICIENCIES, TRAITS, FEATS, INVENTORY, BACKSTORY }
enum class CharacterNumberField { LEVEL, CURRENT_HP, MAX_HP, TEMP_HP, SPEED, ARMOR_CLASS, SHIELD, INITIATIVE_MISC_BONUS, PASSIVE_PERCEPTION_BONUS, DC_MISC_BONUS, ATTACK_MISC_BONUS }
