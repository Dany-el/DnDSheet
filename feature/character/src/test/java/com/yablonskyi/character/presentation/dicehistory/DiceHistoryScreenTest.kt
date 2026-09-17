package com.yablonskyi.character.presentation.dicehistory

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.junit4.createComposeRule
import com.yablonskyi.dice.DiceIntent
import com.yablonskyi.model.dice.DiceGroup
import com.yablonskyi.model.dice.SavedDiceRoll
import com.yablonskyi.ui.theme.DnDSheetTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DiceHistoryScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun givenEmptyHistory_whenRendered_thenShowsEmptyState() {
        compose.setContent {
            DnDSheetTheme { DiceHistoryScreen(DiceHistoryState(isLoading = false), {}, {}) }
        }
        compose.onNodeWithTag(DICE_HISTORY_EMPTY_TAG).assertIsDisplayed()
    }

    @Test
    fun givenSavedRoll_whenRendered_thenShowsRollDetails() {
        compose.setContent {
            DnDSheetTheme {
                DiceHistoryScreen(
                    DiceHistoryState(rolls = listOf(roll()), isLoading = false),
                    {},
                    {},
                )
            }
        }
        compose.onNodeWithTag(DICE_HISTORY_LIST_TAG).assertIsDisplayed()
        compose.onNodeWithText("ABILITY CHECK").assertIsDisplayed()
        compose.onNodeWithText("17").assertIsDisplayed()
    }

    @Test
    fun givenRollsOnDifferentDays_whenRendered_thenShowsAHeaderForEachDay() {
        compose.setContent {
            DnDSheetTheme {
                DiceHistoryScreen(
                    DiceHistoryState(
                        rolls = listOf(
                            roll(id = 1, timestamp = timestamp(2026, Calendar.SEPTEMBER, 15)),
                            roll(id = 2, timestamp = timestamp(2026, Calendar.SEPTEMBER, 14)),
                        ),
                        isLoading = false,
                    ),
                    {},
                    {},
                )
            }
        }

        compose.onNodeWithText("15.09.2026").assertIsDisplayed()
        compose.onNodeWithText("14.09.2026").assertIsDisplayed()
    }

    @Test
    fun givenHistory_whenClearConfirmed_thenDispatchesClearIntent() {
        val intents = mutableListOf<DiceIntent>()
        compose.setContent {
            DnDSheetTheme {
                DiceHistoryScreen(
                    DiceHistoryState(rolls = listOf(roll()), isLoading = false),
                    intents::add,
                    {},
                )
            }
        }
        compose.onNodeWithContentDescription("Clear dice history").performClick()
        compose.onNodeWithText("Clear all saved dice rolls for this character?").assertIsDisplayed()
        compose.onNodeWithText("Clear").performClick()
        assertEquals(listOf(DiceIntent.ClearDiceRolls), intents)
    }

    private fun roll(
        id: Long = 1,
        timestamp: Long = 1_000,
    ) = SavedDiceRoll(
        id = id,
        characterId = 7,
        label = "ABILITY CHECK",
        numbers = listOf(15),
        modifier = 2,
        result = 17,
        dices = listOf(DiceGroup(20, 1)),
        timestamp = timestamp,
    )

    private fun timestamp(year: Int, month: Int, day: Int): Long =
        Calendar.getInstance().apply {
            clear()
            set(year, month, day, 12, 0)
        }.timeInMillis
}
