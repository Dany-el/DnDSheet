package com.yablonskyi.compendium

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.performClick
import com.yablonskyi.compendium.classes.ui.ClassesScreen
import com.yablonskyi.compendium.classes.viewmodel.ClassUiState
import com.yablonskyi.compendium.classes.viewmodel.ClassesIntent
import com.yablonskyi.compendium.races.ui.RacesScreen
import com.yablonskyi.compendium.races.viewmodel.RaceUiState
import com.yablonskyi.compendium.races.viewmodel.RacesIntent
import com.yablonskyi.model.character.Spell
import com.yablonskyi.model.rulebook.CharacterClass
import com.yablonskyi.model.rulebook.Race
import com.yablonskyi.ui.spell.SpellLibraryItem
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
class LibraryFabTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun `all libraries wire import create and selection export and hide fab in selection`() {
        val screen = mutableStateOf(0)
        val selection = mutableStateOf(false)
        val actions = mutableListOf<String>()
        compose.setContent {
            val snackbar = remember { SnackbarHostState() }
            MaterialTheme {
                when (screen.value) {
                    0 -> RacesScreen(
                        RaceUiState(homebrewRaces = listOf(Race(id = "r", name = "Race", isHomebrew = true)), isSelectionMode = selection.value),
                        snackbar,
                        { when (it) {
                            RacesIntent.RequestFilePicker -> actions += "import"
                            RacesIntent.CreateRace -> actions += "create"
                            RacesIntent.EnterSelectionMode -> { actions += "export"; selection.value = true }
                            else -> Unit
                        } }
                    )
                    1 -> ClassesScreen(
                        ClassUiState(homebrewClasses = listOf(CharacterClass(id = "c", name = "Class", isHomebrew = true)), isSelectionMode = selection.value),
                        snackbar,
                        { when (it) {
                            ClassesIntent.RequestFilePicker -> actions += "import"
                            ClassesIntent.CreateClass -> actions += "create"
                            ClassesIntent.EnterSelectionMode -> { actions += "export"; selection.value = true }
                            else -> Unit
                        } }
                    )
                    else -> SpellLibraryScreen(
                        SpellLibraryState(spells = listOf(SpellLibraryItem(Spell(spellId = 1, name = "Spell"), false)), isSelectionMode = selection.value),
                        snackbar,
                        { when (it) {
                            SpellsIntent.RequestFilePicker -> actions += "import"
                            SpellsIntent.AddSpell -> actions += "create"
                            SpellsIntent.EnterSelectionMode -> { actions += "export"; selection.value = true }
                            else -> Unit
                        } }
                    )
                }
            }
        }
        for (index in 0..2) {
            compose.runOnIdle { screen.value = index; selection.value = false }
            for (label in listOf("Import", "Create", "Export")) {
                compose.onNodeWithTag("library_fab").performClick()
                compose.onNodeWithText(label).performClick()
                compose.waitForIdle()
            }
            compose.onNodeWithTag("library_fab").assertDoesNotExist()
        }
        assertEquals(List(3) { listOf("import", "create", "export") }.flatten(), actions)
    }

    @Test
    fun `character spell learning has no management fab`() {
        compose.setContent {
            MaterialTheme {
                SpellLibraryScreen(SpellLibraryState(isLearnMode = true), remember { SnackbarHostState() }, {})
            }
        }
        compose.onNodeWithTag("library_fab").assertDoesNotExist()
    }

    @Test
    fun `selection mode keeps homebrew races and classes visible`() {
        val screen = mutableStateOf(0)
        compose.setContent {
            val snackbar = remember { SnackbarHostState() }
            MaterialTheme {
                when (screen.value) {
                    0 -> RacesScreen(
                        uiState = RaceUiState(
                            homebrewRaces = listOf(
                                Race(id = "race", name = "Ratfolk", isHomebrew = true),
                            ),
                            selectedRaceIds = setOf("race"),
                            isSelectionMode = true,
                            isLoading = false,
                        ),
                        snackbarHostState = snackbar,
                        onIntent = {},
                    )

                    else -> ClassesScreen(
                        uiState = ClassUiState(
                            homebrewClasses = listOf(
                                CharacterClass(id = "class", name = "Runesmith", isHomebrew = true),
                            ),
                            selectedClassesIds = setOf("class"),
                            isSelectionMode = true,
                            isLoading = false,
                        ),
                        snackbarHostState = snackbar,
                        onIntent = {},
                    )
                }
            }
        }

        compose.onNodeWithText("Ratfolk").assertIsDisplayed()
        compose.runOnIdle { screen.value = 1 }
        compose.onNodeWithText("Runesmith").assertIsDisplayed()
    }
}
