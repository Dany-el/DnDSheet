package com.yablonskyi.character.presentation.notes.editor

import com.yablonskyi.model.character.RichText
import com.yablonskyi.model.character.TextFormat
import com.yablonskyi.model.character.TextSpan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class RichTextEditorTest {
    @Test
    fun selectionToggleAddsThenRemovesOnlySelectedStyle() {
        val initial = RichTextEditor(RichText(plainText = "abcdef"))
            .select(1, 5).toggle(TextFormat.BOLD)
            .select(2, 4).toggle(TextFormat.ITALIC)

        assertEquals(
            listOf(TextSpan(1, 5, TextFormat.BOLD), TextSpan(2, 4, TextFormat.ITALIC)),
            initial.content.spans,
        )
        assertEquals(
            listOf(
                TextSpan(1, 2, TextFormat.BOLD),
                TextSpan(2, 4, TextFormat.ITALIC),
                TextSpan(4, 5, TextFormat.BOLD),
            ),
            initial.select(2, 4).toggle(TextFormat.BOLD).content.spans,
        )
    }

    @Test
    fun replacingAcrossSpansPreservesSurroundingStyles() {
        val edited = RichTextEditor(
            RichText(plainText = "abcdef", spans = listOf(
                TextSpan(0, 3, TextFormat.BOLD),
                TextSpan(3, 6, TextFormat.ITALIC),
            )),
        ).select(2, 4).replace("XY")

        assertEquals("abXYef", edited.content.plainText)
        assertEquals(
            listOf(TextSpan(0, 2, TextFormat.BOLD), TextSpan(4, 6, TextFormat.ITALIC)),
            edited.content.spans,
        )
    }

    @Test
    fun collapsedFormattingAppliesToTypedTextAndNotToPlainPasteAfterClearing() {
        val editor = RichTextEditor(RichText(plainText = "ab"))
            .select(1, 1).toggle(TextFormat.UNDERLINE).replace("😀\n")
        assertEquals("a😀\nb", editor.content.plainText)
        assertEquals(listOf(TextSpan(1, 4, TextFormat.UNDERLINE)), editor.content.spans)
        val pasted = editor.clearFormatting().replace("plain")
        assertEquals(listOf(TextSpan(1, 4, TextFormat.UNDERLINE)), pasted.content.spans)
    }

    @Test
    fun cursorCannotSplitEmoji() {
        assertThrows(IllegalArgumentException::class.java) {
            RichTextEditor(RichText(plainText = "😀")).select(1, 1)
        }
    }

    @Test
    fun multipleTypingStylesApplyTogetherAndDeletionKeepsStylesOutsideRange() {
        val editor = RichTextEditor(RichText(plainText = "abcd"))
            .select(2, 2)
            .toggle(TextFormat.BOLD)
            .toggle(TextFormat.ITALIC)
            .replace("😀")
        assertEquals(
            listOf(TextSpan(2, 4, TextFormat.BOLD), TextSpan(2, 4, TextFormat.ITALIC)),
            editor.content.spans,
        )
        val deleted = editor.select(1, 5).replace("")
        assertEquals("ad", deleted.content.plainText)
        assertEquals(emptyList<TextSpan>(), deleted.content.spans)
    }

    @Test
    fun valueCallbackReplacementKeepsEmojiBoundaries() {
        assertEquals(
            Replacement(1, 3, "🙂"),
            singleReplacement("a😀z", "a🙂z"),
        )
    }
}
