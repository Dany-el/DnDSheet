package com.yablonskyi.data.utils

import com.yablonskyi.model.character.Attack
import com.yablonskyi.model.character.Character
import com.yablonskyi.model.character.CharacterSheet
import com.yablonskyi.model.character.Spell
import org.junit.Assert.assertEquals
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
