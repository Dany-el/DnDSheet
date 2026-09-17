package com.yablonskyi.character.presentation.sheet

import com.yablonskyi.dice.DiceIntent
import com.yablonskyi.domain.character.CharacterChange
import com.yablonskyi.model.character.Spell
import com.yablonskyi.model.character.SpellLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SpellCastingTest {
    private val changes = mutableListOf<CharacterSheetIntent>()
    private val rolls = mutableListOf<DiceIntent>()

    @Test
    fun givenCantrip_whenCast_thenRollsWithoutUsingSlot() {
        val spell = Spell(damageDice = "1d10")

        dispatchSpellCast(spell, changes::add, rolls::add)

        assertEquals(listOf(DiceIntent.SpellDamageRoll(spell, "1d10")), rolls)
        assertTrue(changes.isEmpty())
    }

    @Test
    fun givenLeveledSpell_whenCast_thenRollsAndUsesOneSlot() {
        val level = SpellLevel.entries.first { !it.isCantrip }
        val spell = Spell(level = level, damageDice = "2d6")

        dispatchSpellCast(spell, changes::add, rolls::add)

        assertEquals(listOf(DiceIntent.SpellDamageRoll(spell, "2d6")), rolls)
        assertEquals(listOf(CharacterSheetIntent.Change(CharacterChange.SlotUsed(level, 1))), changes)
    }

    @Test
    fun givenMissingOrBlankDice_whenCast_thenUsesSlotWithoutRolling() {
        val level = SpellLevel.entries.first { !it.isCantrip }
        listOf(null, "", "   ").forEach { dice ->
            dispatchSpellCast(Spell(level = level, damageDice = dice), changes::add, rolls::add)
        }

        assertTrue(rolls.isEmpty())
        assertEquals(List(3) { CharacterSheetIntent.Change(CharacterChange.SlotUsed(level, 1)) }, changes)
    }
}
