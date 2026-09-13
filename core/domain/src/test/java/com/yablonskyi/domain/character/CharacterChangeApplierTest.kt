package com.yablonskyi.domain.character

import com.yablonskyi.model.character.*
import org.junit.Assert.*
import org.junit.Test

class CharacterChangeApplierTest {
    private val level = SpellLevel.entries[1]

    @Test fun givenSuccessiveFieldEdits_whenApplied_thenPreservesEarlierEdit() {
        val initial = Character(name = "Old", notes = "Old notes")
        val renamed = applyCharacterChange(initial, CharacterChange.Text(CharacterTextField.NAME, "Hero"))
        val updated = applyCharacterChange(renamed, CharacterChange.Text(CharacterTextField.NOTES, "New notes"))
        assertEquals("Hero", updated.name)
        assertEquals("New notes", updated.notes)
    }

    @Test fun givenUsedSlots_whenMaximumReduced_thenClampsUsedSlots() {
        val initial = Character(spellSettings = SpellSettings(spellSlots = mapOf(level to SpellSlot(current = 4, max = 5))))
        val updated = applyCharacterChange(initial, CharacterChange.SlotMaximum(level, 2))
        assertEquals(SpellSlot(current = 2, max = 2), updated.spellSettings.spellSlots[level])
        val negative = applyCharacterChange(updated, CharacterChange.SlotMaximum(level, -3))
        assertEquals(SpellSlot(current = 0, max = 0), negative.spellSettings.spellSlots[level])
    }

    @Test fun givenExhaustedSlots_whenUsedOrUndone_thenStaysWithinBounds() {
        val initial = Character(spellSettings = SpellSettings(spellSlots = mapOf(level to SpellSlot(current = 2, max = 2))))
        assertEquals(2, applyCharacterChange(initial, CharacterChange.SlotUsed(level, 1)).spellSettings.spellSlots[level]?.current)
        assertEquals(0, applyCharacterChange(initial, CharacterChange.SlotUsed(level, -4)).spellSettings.spellSlots[level]?.current)
    }

    @Test fun givenWoundedCharacter_whenLongRest_thenRestoresHealthAndSlots() {
        val initial = Character(currentHp = 1, maxHp = 30, tempHp = 5,
            spellSettings = SpellSettings(spellSlots = mapOf(level to SpellSlot(current = 2, max = 3))))
        val updated = applyCharacterChange(initial, CharacterChange.LongRest)
        assertEquals(30, updated.currentHp)
        assertEquals(0, updated.tempHp)
        assertEquals(SpellSlot(current = 0, max = 3), updated.spellSettings.spellSlots[level])
    }
}
