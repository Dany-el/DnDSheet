package com.yablonskyi.character.presentation.sheet

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.junit4.createComposeRule
import com.yablonskyi.character.presentation.sheet.components.CharacterTopAppBar
import com.yablonskyi.ui.theme.DnDSheetTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CharacterTopAppBarTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun givenCharacterSheet_whenDiceHistoryMenuClicked_thenInvokesHistoryCallback() {
        var historyClicks = 0
        compose.setContent {
            DnDSheetTheme {
                CharacterTopAppBar(
                    name = "Aria",
                    race = "Elf",
                    charClass = "Wizard",
                    imagePath = null,
                    nameModifier = Modifier,
                    classRaceModifier = Modifier,
                    imageModifier = Modifier,
                    onNavigateBack = {},
                    onSettingsNavigate = {},
                    onDiceHistoryNavigate = { historyClicks++ },
                    lessDetails = false,
                    onLessDetails = {},
                )
            }
        }

        compose.onNodeWithContentDescription("More Options").performClick()
        compose.onNodeWithText("Dice history").performClick()
        assertEquals(1, historyClicks)
    }
}
