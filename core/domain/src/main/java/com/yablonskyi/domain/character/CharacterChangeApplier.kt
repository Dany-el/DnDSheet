package com.yablonskyi.domain.character

import com.yablonskyi.model.character.Character
import com.yablonskyi.model.character.SpellSlot
import com.yablonskyi.model.character.validateNotes
import com.yablonskyi.model.character.validateNoteId

fun applyCharacterChange(character: Character, change: CharacterChange): Character = when (change) {
    is CharacterChange.Text -> when (change.field) {
        CharacterTextField.NAME -> character.copy(name = change.value)
        CharacterTextField.RACE -> character.copy(race = change.value)
        CharacterTextField.CHAR_CLASS -> character.copy(charClass = change.value)
        CharacterTextField.SUB_CLASS -> character.copy(subClass = change.value)
        CharacterTextField.HIT_DICE -> character.copy(hitDice = change.value)
        CharacterTextField.PROFICIENCIES -> character.copy(proficiencies = change.value)
        CharacterTextField.TRAITS -> character.copy(traits = change.value)
        CharacterTextField.FEATS -> character.copy(feats = change.value)
        CharacterTextField.INVENTORY -> character.copy(inventory = change.value)
        CharacterTextField.BACKSTORY -> character.copy(backstory = change.value)
    }
    is CharacterChange.Number -> when (change.field) {
        CharacterNumberField.LEVEL -> character.copy(level = change.value)
        CharacterNumberField.CURRENT_HP -> character.copy(currentHp = change.value)
        CharacterNumberField.MAX_HP -> character.copy(maxHp = change.value)
        CharacterNumberField.TEMP_HP -> character.copy(tempHp = change.value)
        CharacterNumberField.SPEED -> character.copy(speed = change.value)
        CharacterNumberField.ARMOR_CLASS -> character.copy(armorClass = change.value)
        CharacterNumberField.SHIELD -> character.copy(shield = change.value)
        CharacterNumberField.INITIATIVE_MISC_BONUS -> character.copy(initiativeMiscBonus = change.value)
        CharacterNumberField.PASSIVE_PERCEPTION_BONUS -> character.copy(passivePerceptionBonus = change.value)
        CharacterNumberField.DC_MISC_BONUS -> character.copy(spellSettings = character.spellSettings.copy(dcMiscBonus = change.value))
        CharacterNumberField.ATTACK_MISC_BONUS -> character.copy(spellSettings = character.spellSettings.copy(attackMiscBonus = change.value))
    }
    is CharacterChange.Health -> character.copy(currentHp = change.current, maxHp = change.max, tempHp = change.temp)
    is CharacterChange.Coins -> character.copy(coins = change.value)
    is CharacterChange.AbilityScore -> character.copy(abilityBlock = character.abilityBlock.update(change.ability, change.score))
    is CharacterChange.SkillProficiency -> character.copy(skillProficiencies = character.skillProficiencies + (change.skill to change.level))
    is CharacterChange.SavingThrow -> character.copy(savingThrowProficiencies = if (change.proficient)
        character.savingThrowProficiencies + change.ability else character.savingThrowProficiencies - change.ability)
    is CharacterChange.CastingAbility -> character.copy(spellSettings = character.spellSettings.copy(spellCastingAbility = change.ability))
    is CharacterChange.SlotMaximum -> {
        val slot = character.spellSettings.spellSlots[change.level] ?: SpellSlot()
        val max = change.maximum.coerceAtLeast(0)
        character.copy(spellSettings = character.spellSettings.copy(spellSlots = character.spellSettings.spellSlots +
            (change.level to slot.copy(max = max, current = slot.current.coerceIn(0, max)))))
    }
    is CharacterChange.SlotUsed -> {
        val slot = character.spellSettings.spellSlots[change.level] ?: SpellSlot()
        val used = (slot.current.toLong() + change.delta).coerceIn(0L, slot.max.coerceAtLeast(0).toLong()).toInt()
        character.copy(spellSettings = character.spellSettings.copy(spellSlots = character.spellSettings.spellSlots +
            (change.level to slot.copy(current = used))))
    }
    is CharacterChange.JackOfAllTrades -> character.copy(hasJackOfAllTrades = change.enabled)
    is CharacterChange.Image -> character.copy(imagePath = change.path)
    is CharacterChange.AddNote -> {
        listOf(change.note).validateNotes()
        val existing = character.notes.find { it.id == change.note.id }
        when {
            existing == change.note -> character
            existing != null -> throw IllegalStateException("Note ID already exists")
            else -> character.copy(notes = character.notes + change.note)
        }
    }
    is CharacterChange.UpdateNote -> {
        require(change.original.id == change.note.id) { "Note ID cannot change" }
        listOf(change.note).validateNotes()
        val index = character.notes.indexOfFirst { it.id == change.note.id }
        check(index >= 0) { "Note no longer exists" }
        val current = character.notes[index]
        check(current == change.original || current == change.note) { "Note changed while editing" }
        character.copy(notes = character.notes.toMutableList().apply { set(index, change.note) })
    }
    is CharacterChange.DeleteNote -> {
        validateNoteId(change.noteId)
        character.copy(notes = character.notes.filterNot { it.id == change.noteId })
    }
    CharacterChange.LongRest -> character.copy(currentHp = character.maxHp, tempHp = 0,
        spellSettings = character.spellSettings.copy(spellSlots = character.spellSettings.spellSlots.mapValues { (_, slot) -> slot.copy(current = 0) }))
}
