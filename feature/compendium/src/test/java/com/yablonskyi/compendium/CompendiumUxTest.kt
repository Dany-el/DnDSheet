package com.yablonskyi.compendium

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.yablonskyi.ui.theme.DnDSheetTheme
import com.yablonskyi.compendium.races.ui.RacesScreen
import com.yablonskyi.compendium.races.viewmodel.RaceUiState
import com.yablonskyi.compendium.classes.ui.ClassesScreen
import com.yablonskyi.compendium.classes.viewmodel.ClassUiState
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.yablonskyi.ui.spell.FilterCheckboxRow
import com.yablonskyi.ui.spell.SpellFilterScreen
import com.yablonskyi.ui.spell.SpellFilterState
import com.yablonskyi.ui.spell.SpellLibraryScreen
import com.yablonskyi.ui.spell.SpellLibraryState
import com.yablonskyi.ui.spell.SpellsIntent
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "en")
class CompendiumUxTest {
    @get:Rule val compose = createComposeRule()

    @Test fun `filter has one toggle target and toggles once`() {
        val checked = mutableStateOf(false)
        var calls = 0
        compose.setContent {
            MaterialTheme { FilterCheckboxRow("Ritual", checked.value, { calls++; checked.value = !checked.value }) }
        }
        compose.onAllNodes(isToggleable(), useUnmergedTree = true).assertCountEquals(1)
        compose.onNodeWithText("Ritual").assertIsOff().performClick().assertIsOn()
        assertEquals(1, calls)
    }

    @Test fun `loading empty search and selection states expose appropriate actions`() {
        val state = mutableStateOf(SpellLibraryState(isLoading = true))
        val intents = mutableListOf<SpellsIntent>()
        compose.setContent { MaterialTheme { SpellLibraryScreen(state.value, remember { SnackbarHostState() }, intents::add) } }
        compose.onNodeWithText("Loading library").assertExists()
        compose.onNodeWithText("No entries yet").assertDoesNotExist()
        compose.runOnIdle { state.value = SpellLibraryState() }
        compose.onNodeWithText("No entries yet").assertExists()
        compose.onNodeWithText("Create").performClick()
        compose.onNodeWithText("Import").performClick()
        assertEquals(listOf(SpellsIntent.AddSpell, SpellsIntent.RequestFilePicker), intents)
        compose.runOnIdle { state.value = SpellLibraryState(searchQuery = "missing") }
        compose.onNodeWithText("No matching entries").assertExists()
        compose.onNodeWithText("Clear search").performClick()
        assertEquals(SpellsIntent.SearchQueryChanged(""), intents.last())
        compose.runOnIdle { state.value = SpellLibraryState(isSelectionMode = true, selectedSpellIds = setOf(1L, 2L)) }
        compose.onNodeWithText("2 selected").assertExists()
        compose.onNodeWithContentDescription("Search").assertDoesNotExist()
    }

    @Test fun `filter close dispatches dismissal`() {
        val intents = mutableListOf<SpellsIntent>()
        compose.setContent { MaterialTheme { SpellFilterScreen(SpellFilterState(), intents::add, true) } }
        compose.onNodeWithContentDescription("Close").performClick()
        assertEquals(listOf(SpellsIntent.ToggleFiltersExpanded), intents)
    }
    @Test
    @Config(qualifiers = "en-w360dp-h800dp")
    fun `compact library states remain usable across themes and font scales`() = checkLayouts()

    @Test
    @Config(qualifiers = "en-w800dp-h1280dp")
    fun `expanded library states remain usable across themes and font scales`() = checkLayouts()

    private fun checkLayouts() {
        val screen = mutableStateOf(0)
        val dark = mutableStateOf(false)
        val dynamic = mutableStateOf(false)
        val scale = mutableStateOf(1f)
        compose.setContent {
            DnDSheetTheme(darkTheme = dark.value, dynamicColor = dynamic.value) {
                CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, scale.value)) {
                    val snackbar = remember { SnackbarHostState() }
                    when (screen.value) {
                        0 -> RacesScreen(RaceUiState(isLoading = false), snackbar, {})
                        1 -> ClassesScreen(ClassUiState(isLoading = false), snackbar, {})
                        else -> SpellLibraryScreen(SpellLibraryState(), snackbar, {})
                    }
                }
            }
        }
        for (index in 0..2) for (isDark in listOf(false, true)) for (isDynamic in listOf(false, true)) for (fontScale in listOf(1f, 2f)) {
            compose.runOnIdle { screen.value = index; dark.value = isDark; dynamic.value = isDynamic; scale.value = fontScale }
            compose.onNodeWithText("No entries yet").assertIsDisplayed()
            compose.onNodeWithText("Create").assertIsDisplayed().assertHeightIsAtLeast(48.dp)
            compose.onNodeWithText("Import").assertIsDisplayed()
        }
    }

}