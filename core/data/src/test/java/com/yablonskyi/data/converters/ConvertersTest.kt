package com.yablonskyi.data.converters

import com.yablonskyi.model.character.Ability
import com.yablonskyi.model.character.ProficiencyLevel
import com.yablonskyi.model.character.Skill
import com.yablonskyi.model.character.SpellLevel
import com.yablonskyi.model.character.SpellSlot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ConvertersTest {
    private val converters = Converters()

    @Test
    fun legacyStructuredValues_roundTrip() {
        val skills = mapOf(Skill.PERCEPTION to ProficiencyLevel.EXPERT)
        val slots = mapOf(SpellLevel.LEVEL_1 to SpellSlot(max = 4, current = 3))
        val abilities = mapOf(Ability.INT to 16)

        assertEquals(skills, converters.toSkillMap(converters.fromSkillMap(skills)))
        assertEquals(slots, converters.toSpellSlotsMap(converters.fromSpellSlotsMap(slots)))
        assertEquals(abilities, converters.toAbilityIntMap(converters.fromAbilityIntMap(abilities)))
    }

    @Test
    fun malformedStructuredValues_failExplicitly() {
        assertThrows(RuntimeException::class.java) { converters.toSkillMap("not-json") }
        assertThrows(RuntimeException::class.java) { converters.toSpellSlotsMap("not-json") }
        assertThrows(RuntimeException::class.java) { converters.toAbilityIntMap("not-json") }
    }

    @Test
    fun legacySpellSlotJson_isDecoded() {
        val result = converters.toSpellSlotsMap("""{"LEVEL_1":{"max":4,"current":3}}""")
        assertEquals(SpellSlot(max = 4, current = 3), result[SpellLevel.LEVEL_1])
    }
}
