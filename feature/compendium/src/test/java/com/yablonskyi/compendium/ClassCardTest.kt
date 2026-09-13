package com.yablonskyi.compendium

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.yablonskyi.compendium.classes.ui.ClassCard
import com.yablonskyi.compendium.classes.ui.collapsibleClassesList
import com.yablonskyi.model.rulebook.CharacterClass
import com.yablonskyi.model.character.Ability
import com.yablonskyi.ui.R
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "en")
class ClassCardTest {
    @get:Rule val compose = createComposeRule()

    @Test fun `card shows only requested fields with large fonts`() {
        val cls = mutableStateOf(CharacterClass(id = "c", name = "Test class", hitDice = "d12",
            skillChoiceCount = 3, savingThrows = setOf(Ability.STR),
            spellcastingAbility = Ability.INT, description = "Hidden description", isHomebrew = true))
        compose.setContent { MaterialTheme {
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, 2f)) {
                ClassCard(cls.value, false, false, true, {}, {}, {}, {}, {}, {})
            }
        } }
        compose.onNodeWithText("d12", useUnmergedTree = true).assertIsDisplayed()
        compose.onNodeWithText("3", substring = true, useUnmergedTree = true).assertIsDisplayed()
        compose.onNodeWithText("STR", substring = true, useUnmergedTree = true).assertExists()
        compose.onNodeWithText("Hidden description", substring = true).assertDoesNotExist()
        compose.onNodeWithText("Homebrew").assertDoesNotExist()
        compose.onNodeWithText("Spellcaster").assertDoesNotExist()
        compose.runOnIdle { cls.value = cls.value.copy(savingThrows = emptySet()) }
        compose.onNodeWithText("STR", substring = true).assertDoesNotExist()
    }

    @Test fun `card retains click long press and checkbox actions`() {
        val selection = mutableStateOf(false)
        val calls = mutableListOf<String>()
        compose.setContent { MaterialTheme {
            ClassCard(CharacterClass(id = "c", name = "Test class", isHomebrew = true), selection.value, selection.value, true,
                { calls += "details" }, { calls += "select" }, {}, {}, {}, { calls += "toggle" })
        } }
        compose.onNodeWithText("Test class").performClick()
        compose.onNodeWithText("Test class").performTouchInput { longClick() }
        compose.runOnIdle { selection.value = true }
        compose.onNode(isToggleable()).assertIsOn().performClick()
        assertEquals(listOf("details", "select", "toggle"), calls)
    }

    @Test fun `section header remains visible after scrolling and can collapse`() {
        val expanded = mutableStateOf(true)
        var scroll: () -> Unit = {}
        compose.setContent { MaterialTheme {
            val state = rememberLazyListState()
            val scope = rememberCoroutineScope()
            scroll = { scope.launch { state.scrollToItem(6) } }
            LazyColumn(state = state, modifier = Modifier.height(300.dp)) {
                collapsibleClassesList(
                    header = R.string.homebrew,
                    classes = List(12) { CharacterClass(id = "$it", name = "Class $it", isHomebrew = true) },
                    selectedClassesIds = emptySet(), isSelectionMode = false, isHomebrew = true,
                    isExpanded = expanded.value, onDetails = {}, onEdit = {}, onShare = {}, onDelete = {},
                    onToggleSelection = {}, onCollapse = { expanded.value = !expanded.value }
                )
            }
        } }
        compose.runOnIdle { scroll() }
        compose.waitForIdle()
        compose.onNodeWithText("HOMEBREW").assertIsDisplayed().performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Class 5").assertDoesNotExist()
        compose.onNodeWithText("HOMEBREW").assertIsDisplayed().performClick()
        compose.onNodeWithText("Class 0").assertExists()
    }
}
