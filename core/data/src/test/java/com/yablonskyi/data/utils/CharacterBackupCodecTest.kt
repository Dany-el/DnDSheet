package com.yablonskyi.data.utils

import com.yablonskyi.model.character.Attack
import com.yablonskyi.model.character.Character
import com.yablonskyi.model.character.CharacterSheet
import com.yablonskyi.model.character.Spell
import com.yablonskyi.model.character.Note
import com.yablonskyi.model.character.RichText
import com.yablonskyi.model.character.TextFormat
import com.yablonskyi.model.character.TextSpan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class CharacterBackupCodecTest {
    @Test
    fun roundTrip_preservesCharacterSheet() {
        val sheet = CharacterSheet(
            character = Character(id = 7, name = "Mira"),
            spells = listOf(Spell(name = "Light")),
            attacks = listOf(Attack(name = "Staff")),
        )

        val decoded = CharacterBackupCodec.decode(CharacterBackupCodec.encode(listOf(sheet)))

        assertEquals(listOf(sheet), decoded)
    }

    @Test
    fun decode_ignoresUnknownFields() {
        val decoded = CharacterBackupCodec.decode(
            """[{"character":{"name":"Mira","futureField":true},"spells":[],"attacks":[],"futureSheetField":"ignored"}]"""
        )

        assertEquals("Mira", decoded.single().character.name)
    }

    @Test fun legacyStringsIncludingEmptyBecomeOneNoteAndMissingNotesStayEmpty() {
        val decoded = CharacterBackupCodec.decode("""[
            {"character":{"notes":""},"spells":[],"attacks":[]},
            {"character":{"notes":"🐉\ntext"},"spells":[],"attacks":[]},
            {"character":{},"spells":[],"attacks":[]}
        ]""".trimIndent())
        assertEquals(listOf("Notes"), decoded[0].character.notes.map { it.topic })
        assertEquals("", decoded[0].character.notes.single().text.plainText)
        assertEquals("🐉\ntext", decoded[1].character.notes.single().text.plainText)
        assertEquals(emptyList<Note>(), decoded[2].character.notes)
    }

    @Test fun formattedNotesRoundTripAndInvalidNotesAreRejected() {
        val note = Note("15412a7e-37e6-4e8a-92cb-af49e0759032", "Travel",
            RichText(plainText = "Forest", spans = listOf(TextSpan(0, 6, TextFormat.BOLD))))
        val sheet = CharacterSheet(Character(notes = listOf(note)), emptyList(), emptyList())
        assertEquals(sheet, CharacterBackupCodec.decode(CharacterBackupCodec.encode(listOf(sheet))).single())
        val corrupt = CharacterBackupCodec.encode(listOf(sheet)).replace("\"endExclusive\":6", "\"endExclusive\":99")
        assertThrows(IllegalArgumentException::class.java) { CharacterBackupCodec.decode(corrupt) }
    }

    @Test
    fun publishedGsonFixture_decodesCompleteNestedData() {
        val content = requireNotNull(javaClass.getResourceAsStream("/character-backups/published-gson-synthetic.json"))
            .bufferedReader().use { it.readText() }

        val sheet = CharacterBackupCodec.decode(content).single()

        assertEquals(42, sheet.character.id)
        assertEquals(4, sheet.character.spellSettings.spellSlots[com.yablonskyi.model.character.SpellLevel.LEVEL_1]?.max)
        assertEquals("Staff", sheet.attacks.single().name)
    }
}
