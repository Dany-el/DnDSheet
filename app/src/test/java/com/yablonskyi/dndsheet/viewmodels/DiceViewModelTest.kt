package com.yablonskyi.dndsheet.viewmodels

import com.yablonskyi.dndsheet.ui.dice.DiceViewModel
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DiceViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: DiceViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = DiceViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `початковий стан не показує результат`() {
        assertFalse(viewModel.diceRollState.value.showResult)
    }

    @Test
    fun `rollDice відображає результат після кидка`() = runTest {
        viewModel.rollDice(mapOf(6 to 1))
        advanceTimeBy(100)
        assertTrue(viewModel.diceRollState.value.showResult)
    }

    @Test
    fun `результат кидку d6 знаходиться у межах 1 до 6`() = runTest {
        repeat(20) {
            viewModel.rollDice(mapOf(6 to 1))
            advanceTimeBy(100)
            val result = viewModel.diceRollState.value.result
            assertTrue(result in 1..6)
        }
    }

    @Test
    fun `результат кидку 2d6 знаходиться у межах 2 до 12`() = runTest {
        repeat(20) {
            viewModel.rollDice(mapOf(6 to 2))
            advanceTimeBy(100)
            val result = viewModel.diceRollState.value.result
            assertTrue(result in 2..12)
        }
    }

    @Test
    fun `модифікатор враховується у підсумку`() = runTest {
        viewModel.rollDice(mapOf(1 to 1), modifier = 5)
        advanceTimeBy(100)
        assertEquals(6, viewModel.diceRollState.value.result)
    }

    @Test
    fun `результат автоматично зникає після 5 секунд`() = runTest {
        viewModel.rollDice(mapOf(6 to 1))
        advanceTimeBy(100)
        assertTrue(viewModel.diceRollState.value.showResult)
        // Пропускаємо повні 5 секунд — таймер спливає
        advanceTimeBy(5_000)
        assertFalse(viewModel.diceRollState.value.showResult)
    }

    @Test
    fun `pinResult закріплює результат та скасовує автозникнення`() = runTest {
        viewModel.rollDice(mapOf(20 to 1))
        advanceTimeBy(100)
        viewModel.pinResult()
        advanceTimeBy(100)
        assertTrue(viewModel.diceRollState.value.isPinned)
        assertTrue(viewModel.diceRollState.value.showResult)
    }

    @Test
    fun `повторний pinResult знімає закріплення та приховує результат`() = runTest {
        viewModel.rollDice(mapOf(20 to 1))
        advanceTimeBy(100)
        viewModel.pinResult()
        advanceTimeBy(100)
        viewModel.pinResult()
        advanceTimeBy(100)
        assertFalse(viewModel.diceRollState.value.isPinned)
        assertFalse(viewModel.diceRollState.value.showResult)
    }

    @Test
    fun `dismissResult приховує результат`() = runTest {
        viewModel.rollDice(mapOf(6 to 1))
        advanceTimeBy(100)
        viewModel.dismissResult()
        advanceTimeBy(100)
        assertFalse(viewModel.diceRollState.value.showResult)
    }

    @Test
    fun `rollDiceFromString розбирає нотацію 1d20`() = runTest {
        viewModel.rollDiceFromString("1d20")
        advanceTimeBy(100)
        val state = viewModel.diceRollState.value
        assertTrue(state.showResult)
        assertTrue(state.result in 1..20)
    }

    @Test
    fun `rollDiceFromString розбирає нотацію 2d6 плюс 3`() = runTest {
        viewModel.rollDiceFromString("2d6+3")
        advanceTimeBy(100)
        val result = viewModel.diceRollState.value.result
        assertTrue(result in 5..15)
    }

    @Test
    fun `rollDiceFromString розбирає кириличну нотацію 1к4`() = runTest {
        viewModel.rollDiceFromString("1к4")
        advanceTimeBy(100)
        val result = viewModel.diceRollState.value.result
        assertTrue(result in 1..4)
    }

    @Test
    fun `rollDiceFromString з некоректним рядком не змінює стан`() = runTest {
        val before = viewModel.diceRollState.value
        viewModel.rollDiceFromString("некоректно")
        advanceTimeBy(100)
        assertEquals(before, viewModel.diceRollState.value)
    }
}