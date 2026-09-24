package com.yablonskyi.character.presentation.notes

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.yablonskyi.model.character.Note
import com.yablonskyi.model.character.RichText
import com.yablonskyi.model.character.TextFormat
import com.yablonskyi.model.character.TextSpan
import com.yablonskyi.ui.theme.DnDSheetTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "en-rUS-w400dp-h800dp")
class NoteEditorScreenTest {
    @get:Rule val compose = createComposeRule()

    private val state = mutableStateOf(NoteEditorState(
        status = NoteEditorStatus.READY,
        draft = NoteDraft("session", 1, "note", "fingerprint", null,
            Note("note", "Topic", RichText(plainText = "abc")), 0, 3),
    ))

    private fun render() {
        compose.setContent {
            DnDSheetTheme {
                NoteEditorScreen(
                    state = state.value,
                    onBack = {},
                    onTopicChanged = {},
                    onBodyChanged = { editor ->
                        val draft = state.value.draft!!
                        state.value = state.value.copy(draft = draft.copy(
                            current = draft.current.copy(text = editor.content),
                            selectionStart = editor.selectionStart,
                            selectionEnd = editor.selectionEnd,
                            typingFormats = editor.typingFormats,
                        ))
                    },
                    onSave = {},
                    onDeleteRequested = {},
                    onDeleteDismissed = {},
                    onDeleteConfirmed = {},
                    onDiscardDismissed = {},
                    onDiscardConfirmed = {},
                )
            }
        }
    }

    @Test
    fun givenSelectedText_whenToolbarFormatsAndClears_thenContentAndToggleStateUpdate() {
        render()
        compose.onNodeWithContentDescription("Bold").performClick().assertIsOn()
        compose.runOnIdle {
            assertEquals(listOf(TextSpan(0, 3, TextFormat.BOLD)), state.value.draft!!.current.text.spans)
        }
        compose.onNodeWithContentDescription("More formatting options").performClick()
        compose.onNodeWithText("Clear formatting").performClick()
        compose.onNodeWithContentDescription("Bold").assertIsOff()
        compose.runOnIdle {
            assertEquals(emptyList<TextSpan>(), state.value.draft!!.current.text.spans)
        }
    }

    @Test
    fun givenSaveInProgress_whenRendered_thenFormattingIsDisabled() {
        state.value = state.value.copy(saving = true)
        render()
        listOf("Bold", "Italic", "Underline", "Strikethrough", "More formatting options").forEach {
            compose.onNodeWithContentDescription(it).assertIsNotEnabled()
        }
    }
}
