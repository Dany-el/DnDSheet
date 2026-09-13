package com.yablonskyi.wizard

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.yablonskyi.wizard.viewmodel.WizardIntent
import com.yablonskyi.wizard.viewmodel.WizardStep
import com.yablonskyi.wizard.viewmodel.WizardUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "en")
class WizardScreenTest {
    @get:Rule val compose = createComposeRule()
    @Test fun `screen dispatches intents and disables navigation during save`() {
        val state = mutableStateOf(WizardUiState(isLoading = false))
        val intents = mutableListOf<WizardIntent>()
        compose.setContent { MaterialTheme {
            CharacterCreationWizardScreen(state.value, intents::add, remember { SnackbarHostState() })
        } }
        compose.onNodeWithText("Next").assertIsNotEnabled()
        compose.onNode(hasSetTextAction()).performTextInput("Hero")
        assertEquals(WizardIntent.NameChanged("Hero"), intents.last())
        compose.runOnIdle { state.value = state.value.copy(form = state.value.form.copy(name = "Hero")) }
        compose.onNodeWithText("Next").assertIsEnabled().performClick()
        assertEquals(WizardIntent.Next, intents.last())
        compose.runOnIdle { state.value = state.value.copy(form = state.value.form.copy(step = WizardStep.RACE, isSubmitting = true)) }
        compose.onNodeWithText("Next").assertIsNotEnabled()
        compose.onNodeWithText("Back").assertIsNotEnabled()
    }
}