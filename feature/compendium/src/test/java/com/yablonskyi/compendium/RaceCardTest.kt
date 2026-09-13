package com.yablonskyi.compendium

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.yablonskyi.compendium.races.ui.RaceCard
import com.yablonskyi.model.rulebook.Race
import com.yablonskyi.model.character.Ability
import com.yablonskyi.model.character.Skill
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "en")
class RaceCardTest {
    @get:Rule val compose = createComposeRule()

    @Test fun `card renders wizard fields and omits empty optional sections`() {
        val race = mutableStateOf(Race(name = "Test race", size = "Small", speed = 25,
            abilityBonuses = mapOf(Ability.STR to 2), grantedSkills = listOf(Skill.ATHLETICS),
            traits = listOf("Darkvision", "Resilience"), isHomebrew = true))
        compose.setContent { MaterialTheme {
            RaceCard(race.value, false, false, true, {}, {}, {}, {}, {}, {})
        } }
        compose.onNodeWithText("Small", useUnmergedTree = true).assertIsDisplayed()
        compose.onNodeWithText("25", substring = true, useUnmergedTree = true).assertIsDisplayed()
        compose.onNodeWithText("+2", substring = true, useUnmergedTree = true).assertExists()
        compose.onNodeWithText("Athletics", substring = true, useUnmergedTree = true).assertExists()
        compose.onNodeWithText("Darkvision - Resilience", useUnmergedTree = true).assertExists()
        compose.runOnIdle { race.value = race.value.copy(abilityBonuses = emptyMap(), grantedSkills = emptyList(), traits = emptyList()) }
        compose.onNodeWithText("+2", substring = true).assertDoesNotExist()
        compose.onNodeWithText("Athletics", substring = true).assertDoesNotExist()
        compose.onNodeWithText("Darkvision - Resilience").assertDoesNotExist()
    }

    @Test fun `card retains click long press and checkbox actions`() {
        val selection = mutableStateOf(false)
        val calls = mutableListOf<String>()
        compose.setContent { MaterialTheme {
            RaceCard(Race(name = "Test race", isHomebrew = true), selection.value, selection.value, true,
                { calls += "details" }, { calls += "select" }, {}, {}, {}, { calls += "toggle" })
        } }
        compose.onNodeWithText("Test race").performClick()
        compose.onNodeWithText("Test race").performTouchInput { longClick() }
        compose.runOnIdle { selection.value = true }
        compose.onNode(isToggleable()).assertIsOn().performClick()
        assertEquals(listOf("details", "select", "toggle"), calls)
    }
}
