package com.yablonskyi.ui.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "en")
class ExpandableFabTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `actions close the menu before dispatch and fire once`() {
        val actions = mutableListOf<String>()
        compose.setContent {
            var expanded by remember { mutableStateOf(false) }
            fun action(name: String) {
                check(!expanded)
                actions += name
            }
            MaterialTheme {
                ExpandableFab(expanded, { expanded = it }, { action("Import") },
                    { action("Export") }, { action("Create") }, saveEnabled = true)
            }
        }
        for (label in listOf("Import", "Export", "Create")) {
            compose.onNodeWithTag("library_fab").performClick()
            compose.onNodeWithText(label).assertIsDisplayed().performClick()
            compose.waitForIdle()
            compose.onNodeWithText(label).assertDoesNotExist()
        }
        assertEquals(listOf("Import", "Export", "Create"), actions)
    }

    @Test
    fun `export is disabled for empty results and close dismisses the menu`() {
        compose.setContent {
            var expanded by remember { mutableStateOf(false) }
            MaterialTheme {
                ExpandableFab(expanded, { expanded = it }, {}, {}, {}, saveEnabled = false)
            }
        }
        compose.onNodeWithTag("library_fab").performClick()
        compose.onNodeWithText("Export").assertIsNotEnabled()
        compose.onNodeWithContentDescription("Close").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Import").assertDoesNotExist()
    }

    @Test
    fun `back dismisses the menu without invoking an action`() {
        var actions = 0
        compose.setContent {
            var expanded by remember { mutableStateOf(false) }
            MaterialTheme {
                ExpandableFab(expanded, { expanded = it }, { actions++ }, { actions++ },
                    { actions++ }, saveEnabled = true)
            }
        }
        compose.onNodeWithTag("library_fab").performClick()
        compose.runOnIdle { compose.activity.onBackPressedDispatcher.onBackPressed() }
        compose.waitForIdle()
        compose.onNodeWithText("Import").assertDoesNotExist()
        assertEquals(0, actions)
    }

    @Test
    fun `empty selection disables export and delete`() {
        compose.setContent {
            MaterialTheme {
                SelectionBottomBar(true, false, {}, {}, {}, actionsEnabled = false)
            }
        }
        compose.onNodeWithContentDescription("Export").assertIsNotEnabled()
        compose.onNodeWithContentDescription("Delete").assertIsNotEnabled()
    }
}
