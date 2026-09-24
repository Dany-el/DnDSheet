package com.yablonskyi.domain.character

import com.yablonskyi.model.character.*
import org.junit.Assert.*
import org.junit.Test

class CharacterChangeApplierTest {
    private val level = SpellLevel.entries[1]
    private val note = Note("15412a7e-37e6-4e8a-92cb-af49e0759032", "Notes")

    @Test fun givenSuccessiveFieldEdits_whenApplied_thenPreservesEarlierEdit() {
        val originalNote = Note("15412a7e-37e6-4e8a-92cb-af49e0759032", "Notes", RichText(plainText = "Old notes"))
        val initial = Character(name = "Old", notes = listOf(originalNote))
        val renamed = applyCharacterChange(initial, CharacterChange.Text(CharacterTextField.NAME, "Hero"))
        val updatedNote = originalNote.copy(text = RichText(plainText = "New notes"))
        val updated = applyCharacterChange(renamed, CharacterChange.UpdateNote(originalNote, updatedNote))
        assertEquals("Hero", updated.name)
        assertEquals(listOf(updatedNote), updated.notes)
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

    @Test fun givenExistingNote_whenAddedAgain_thenIsIdempotentButConflictingIdFails() {
        val character = Character(notes = listOf(note))
        assertEquals(character, applyCharacterChange(character, CharacterChange.AddNote(note)))
        assertThrows(IllegalStateException::class.java) {
            applyCharacterChange(character, CharacterChange.AddNote(note.copy(topic = "Other")))
        }
    }

    @Test fun givenAnotherNoteChanged_whenUpdatingThenDeleting_thenPreservesOrderAndOtherContent() {
        val second = Note("2daf9b4b-c786-4fc8-a023-c417d3327373", "Second")
        val updated = note.copy(text = RichText(plainText = "Changed"))
        val current = Character(notes = listOf(note, second), name = "Hero")
        val saved = applyCharacterChange(current, CharacterChange.UpdateNote(note, updated))
        assertEquals(listOf(updated, second), saved.notes)
        assertEquals("Hero", saved.name)
        assertEquals(listOf(second), applyCharacterChange(saved, CharacterChange.DeleteNote(note.id)).notes)
        assertEquals(listOf(second), applyCharacterChange(saved.copy(notes = listOf(second)), CharacterChange.DeleteNote(note.id)).notes)
    }

    @Test fun givenStaleOrMissingNote_whenUpdated_thenFails() {
        val changed = note.copy(topic = "Someone else's edit")
        val draft = note.copy(topic = "My edit")
        assertThrows(IllegalStateException::class.java) {
            applyCharacterChange(Character(notes = listOf(changed)), CharacterChange.UpdateNote(note, draft))
        }
        assertThrows(IllegalStateException::class.java) {
            applyCharacterChange(Character(), CharacterChange.UpdateNote(note, draft))
        }
    }
}
