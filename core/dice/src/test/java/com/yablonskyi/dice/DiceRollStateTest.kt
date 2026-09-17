package com.yablonskyi.dice

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DiceRollStateTest {
    @Test
    fun givenD20_whenNaturalTwenty_thenCriticalSuccess() {
        val state = DiceRollState(numbers = listOf(20), hasRegularDice = true)
        assertTrue(state.hasCritSuccess)
        assertFalse(state.hasCritFailure)
    }

    @Test
    fun givenD20_whenNaturalOne_thenCriticalFailure() {
        val state = DiceRollState(numbers = listOf(1), hasRegularDice = true)
        assertFalse(state.hasCritSuccess)
        assertTrue(state.hasCritFailure)
    }

    @Test
    fun givenNoD20_whenOneOrTwenty_thenNeitherCriticalFlag() {
        val state = DiceRollState(numbers = listOf(1, 20))
        assertFalse(state.hasCritSuccess)
        assertFalse(state.hasCritFailure)
    }

    @Test
    fun givenD20_whenBothExtremes_thenBothCriticalFlags() {
        val state = DiceRollState(numbers = listOf(1, 20), hasRegularDice = true)
        assertTrue(state.hasCritSuccess)
        assertTrue(state.hasCritFailure)
    }

    @Test
    fun givenD20_whenModifierMakesTotalTwenty_thenNoCriticalSuccess() {
        val state = DiceRollState(
            numbers = listOf(15), modifier = 5, result = 20, hasRegularDice = true,
        )
        assertFalse(state.hasCritSuccess)
        assertFalse(state.hasCritFailure)
        assertTrue(state.copy(numbers = listOf(20)).hasCritSuccess)
    }
}
