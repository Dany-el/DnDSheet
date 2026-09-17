package com.yablonskyi.data.converters

import com.yablonskyi.model.dice.DiceGroup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class DiceRollConvertersTest {
    private val converters = DiceRollConverters()

    @Test
    fun givenDiceData_whenConverted_thenPreservesOrder() {
        val numbers = listOf(6, 1, 4)
        val groups = listOf(DiceGroup(6, 2), DiceGroup(8, 1))
        assertEquals(numbers, converters.toNumbers(converters.fromNumbers(numbers)))
        assertEquals(groups, converters.toDiceGroups(converters.fromDiceGroups(groups)))
    }

    @Test
    fun givenMalformedData_whenConverted_thenFails() {
        assertThrows(RuntimeException::class.java) { converters.toNumbers("not-json") }
        assertThrows(RuntimeException::class.java) { converters.toDiceGroups("null") }
    }
}
