package com.yablonskyi.character.presentation.notes.editor

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.yablonskyi.model.character.RichText
import com.yablonskyi.model.character.TextFormat
import com.yablonskyi.model.character.TextSpan
import org.junit.Assert.assertEquals
import org.junit.Test

class RichTextInputStateTest {
    @Test
    fun givenEmptyInput_whenTwoCallbacksArrive_thenBothCharactersAreKept() {
        val state = RichTextInputState(RichTextEditor())
        state.update(TextFieldValue("a", TextRange(1)))
        state.update(TextFieldValue("ab", TextRange(2)))
        assertEquals("ab", state.editor.content.plainText)
        assertEquals(2, state.editor.selectionStart)
    }

    @Test
    fun givenRepeatedText_whenFirstCharacterDeleted_thenItsFormattingIsRemoved() {
        val state = repeatedText(TextRange(0, 1))
        state.update(TextFieldValue("a", TextRange(0)))
        assertEquals(emptyList<TextSpan>(), state.editor.content.spans)
    }

    @Test
    fun givenRepeatedText_whenBackspaceDeletesFirstCharacter_thenItsFormattingIsRemoved() {
        val state = repeatedText(TextRange(1))
        state.update(TextFieldValue("a", TextRange(0)))
        assertEquals(emptyList<TextSpan>(), state.editor.content.spans)
    }

    @Test
    fun givenRepeatedText_whenForwardDeleteRemovesFirstCharacter_thenItsFormattingIsRemoved() {
        val state = repeatedText(TextRange(0))
        state.update(TextFieldValue("a", TextRange(0)))
        assertEquals(emptyList<TextSpan>(), state.editor.content.spans)
    }

    @Test
    fun givenComposingInput_whenParentEchoesEditor_thenCompositionIsPreserved() {
        val state = RichTextInputState(RichTextEditor())
        val updated = TextFieldValue("word", TextRange(4), TextRange(0, 4))
        state.update(updated)
        state.sync(state.editor)
        assertEquals(updated, state.field)
    }

    @Test
    fun givenRepeatedText_whenInsertedAtStart_thenExistingSpanMoves() {
        val state = repeatedText(TextRange(0))
        state.update(TextFieldValue("aaa", TextRange(1)))
        assertEquals(listOf(TextSpan(1, 2, TextFormat.BOLD)), state.editor.content.spans)
    }

    @Test
    fun givenComposingText_whenCompositionReplaced_thenSurroundingStyleSurvives() {
        val state = RichTextInputState(RichTextEditor(RichText(plainText = "cat!",
            spans = listOf(TextSpan(3, 4, TextFormat.BOLD))), 3, 3))
        state.update(TextFieldValue("cat!", TextRange(3), TextRange(0, 3)))
        state.update(TextFieldValue("dog!", TextRange(3), TextRange(0, 3)))
        assertEquals("dog!", state.editor.content.plainText)
        assertEquals(listOf(TextSpan(3, 4, TextFormat.BOLD)), state.editor.content.spans)
    }

    @Test
    fun givenNewSelection_whenFormattingBeforeRecomposition_thenLatestSelectionIsUsed() {
        val state = RichTextInputState(RichTextEditor(RichText(plainText = "ab")))
        state.update(TextFieldValue("ab", TextRange(1, 2)))
        state.format(TextFormat.ITALIC)
        assertEquals(listOf(TextSpan(1, 2, TextFormat.ITALIC)), state.editor.content.spans)
    }

    private fun repeatedText(selection: TextRange) = RichTextInputState(RichTextEditor(
        RichText(plainText = "aa", spans = listOf(TextSpan(0, 1, TextFormat.BOLD))),
        selection.start,
        selection.end,
    ))
}
