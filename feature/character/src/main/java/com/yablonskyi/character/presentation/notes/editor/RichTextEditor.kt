package com.yablonskyi.character.presentation.notes.editor

import com.yablonskyi.model.character.RichText
import com.yablonskyi.model.character.TextFormat
import com.yablonskyi.model.character.TextSpan

/** Pure editing state. Offsets are UTF-16, matching Compose text selection indices. */
data class RichTextEditor(
    val content: RichText = RichText(),
    val selectionStart: Int = 0,
    val selectionEnd: Int = 0,
    val typingFormats: Set<TextFormat> = emptySet(),
) {
    init {
        content.validate()
        require(selectionStart in 0..content.plainText.length && selectionEnd in 0..content.plainText.length)
        require(content.isBoundary(selectionStart) && content.isBoundary(selectionEnd))
    }

    val selectedStart: Int get() = minOf(selectionStart, selectionEnd)
    val selectedEnd: Int get() = maxOf(selectionStart, selectionEnd)

    fun select(start: Int, end: Int): RichTextEditor {
        require(start in 0..content.plainText.length && end in 0..content.plainText.length)
        require(content.isBoundary(start) && content.isBoundary(end))
        return copy(
            selectionStart = start,
            selectionEnd = end,
            typingFormats = if (start == end) formatsAtCursor(start) else emptySet(),
        )
    }

    /** Toolbar and selection-menu actions use the same command. */
    fun toggle(format: TextFormat): RichTextEditor {
        if (selectedStart == selectedEnd) {
            return copy(typingFormats = typingFormats.toggle(format))
        }
        val fullyCovered = content.spans.filter { it.format == format }
            .sortedBy { it.start }
            .fold(selectedStart) { coveredTo, span ->
                if (span.start <= coveredTo) maxOf(coveredTo, span.endExclusive) else coveredTo
            } >= selectedEnd
        val retained = if (fullyCovered) {
            content.spans.flatMap { span ->
                if (span.format != format || span.endExclusive <= selectedStart || span.start >= selectedEnd) {
                    listOf(span)
                } else {
                    listOfNotNull(
                        span.takeIf { it.start < selectedStart }?.copy(endExclusive = selectedStart),
                        span.takeIf { it.endExclusive > selectedEnd }?.copy(start = selectedEnd),
                    )
                }
            }
        } else {
            content.spans + TextSpan(selectedStart, selectedEnd, format)
        }
        return copy(content = content.copy(spans = retained).normalized())
    }

    fun clearFormatting(): RichTextEditor {
        if (selectedStart == selectedEnd) return copy(typingFormats = emptySet())
        val retained = content.spans.flatMap { span ->
            if (span.endExclusive <= selectedStart || span.start >= selectedEnd) {
                listOf(span)
            } else {
                listOfNotNull(
                    span.takeIf { it.start < selectedStart }?.copy(endExclusive = selectedStart),
                    span.takeIf { it.endExclusive > selectedEnd }?.copy(start = selectedEnd),
                )
            }
        }
        return copy(content = content.copy(spans = retained).normalized())
    }

    /** Plain text insertion, paste, deletion, and selection replacement. */
    fun replace(insertedText: String): RichTextEditor {
        val start = selectedStart
        val end = selectedEnd
        val delta = insertedText.length - (end - start)
        val newSpans = buildList {
            for (span in content.spans) {
                if (span.start < start) {
                    val leftEnd = minOf(span.endExclusive, start)
                    if (span.start < leftEnd) add(span.copy(endExclusive = leftEnd))
                }
                if (span.endExclusive > end) {
                    val rightStart = maxOf(span.start, end) + delta
                    val rightEnd = span.endExclusive + delta
                    if (rightStart < rightEnd) add(span.copy(start = rightStart, endExclusive = rightEnd))
                }
            }
            if (insertedText.isNotEmpty()) {
                for (format in typingFormats) add(TextSpan(start, start + insertedText.length, format))
            }
        }
        val updated = RichText(
            plainText = content.plainText.replaceRange(start, end, insertedText),
            spans = newSpans,
        ).normalized()
        val cursor = start + insertedText.length
        return copy(content = updated, selectionStart = cursor, selectionEnd = cursor)
    }

    private fun formatsAtCursor(cursor: Int): Set<TextFormat> {
        // At a boundary, typing continues the style on the left. At offset zero it uses the right.
        return content.spans.filter { span ->
            if (cursor == 0) span.start == 0 else span.start < cursor && span.endExclusive >= cursor
        }.mapTo(mutableSetOf()) { it.format }
    }
}

private fun <T> Set<T>.toggle(value: T): Set<T> =
    if (value in this) this - value else this + value
