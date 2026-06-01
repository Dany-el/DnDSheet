package com.yablonskyi.dndsheet.viewmodels

import com.yablonskyi.dndsheet.data.model.character.Character
import com.yablonskyi.dndsheet.data.model.character.SpellLevel
import com.yablonskyi.dndsheet.data.model.character.SpellSettings
import com.yablonskyi.dndsheet.data.model.character.SpellSlot
import junit.framework.TestCase.assertEquals
import org.junit.Test

class SpellSlotLogicTest {

    private fun simulateUseSpellSlot(
        character: Character,
        spellLevel: SpellLevel,
        delta: Int
    ): Character {
        val updatedMap = character.spellSettings.spellSlots.toMutableMap()
        val slotData = updatedMap[spellLevel] ?: SpellSlot()
        val newCurrentValue = (slotData.current + delta).coerceIn(0, slotData.max)
        updatedMap[spellLevel] = slotData.copy(current = newCurrentValue)
        return character.copy(
            spellSettings = character.spellSettings.copy(spellSlots = updatedMap)
        )
    }

    private val baseCharacter = Character(
        name = "Wizard",
        spellSettings = SpellSettings(
            spellSlots = mapOf(SpellLevel.LEVEL_1 to SpellSlot(max = 4, current = 0))
        )
    )

    @Test
    fun `витрата слоту збільшує current на 1`() {
        val result = simulateUseSpellSlot(baseCharacter, SpellLevel.LEVEL_1, +1)
        assertEquals(1, result.spellSettings.spellSlots[SpellLevel.LEVEL_1]?.current)
    }

    @Test
    fun `скасування витрати зменшує current на 1`() {
        val used = simulateUseSpellSlot(baseCharacter, SpellLevel.LEVEL_1, +1)
        val undone = simulateUseSpellSlot(used, SpellLevel.LEVEL_1, -1)
        assertEquals(0, undone.spellSettings.spellSlots[SpellLevel.LEVEL_1]?.current)
    }

    @Test
    fun `current не може перевищити max`() {
        val fullSlot = Character(
            name = "Test",
            spellSettings = SpellSettings(
                spellSlots = mapOf(SpellLevel.LEVEL_1 to SpellSlot(max = 2, current = 2))
            )
        )
        val result = simulateUseSpellSlot(fullSlot, SpellLevel.LEVEL_1, +1)
        assertEquals(2, result.spellSettings.spellSlots[SpellLevel.LEVEL_1]?.current)
    }

    @Test
    fun `current не може бути меншим за 0`() {
        val result = simulateUseSpellSlot(baseCharacter, SpellLevel.LEVEL_1, -1)
        assertEquals(0, result.spellSettings.spellSlots[SpellLevel.LEVEL_1]?.current)
    }
}