package com.yablonskyi.character.presentation.notes.editor

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.yablonskyi.character.R
import com.yablonskyi.character.presentation.notes.toAnnotatedString
import com.yablonskyi.model.character.TextFormat

/** Shared by the body input and its formatting toolbar. Updates synchronously per IME event. */
internal class RichTextInputState(initial: RichTextEditor) {
    var editor by mutableStateOf(initial)
        private set
    var field by mutableStateOf(TextFieldValue(initial.content.plainText,
        TextRange(initial.selectionStart, initial.selectionEnd)))
        private set
    val focusRequester = FocusRequester()

    fun sync(value: RichTextEditor) {
        editor = value
        if (field.text != value.content.plainText ||
            field.selection != TextRange(value.selectionStart, value.selectionEnd)
        ) {
            field = TextFieldValue(value.content.plainText,
                TextRange(value.selectionStart, value.selectionEnd))
        }
    }

    fun update(updated: TextFieldValue): RichTextEditor {
        if (updated.text != field.text) {
            val edit = inputReplacement(field, updated)
            val selected = if (editor.selectedStart == edit.start && editor.selectedEnd == edit.endExclusive) {
                editor
            } else {
                editor.select(edit.start, edit.endExclusive)
            }
            editor = selected.replace(edit.inserted)
                .select(updated.selection.start, updated.selection.end)
        } else if (field.selection != updated.selection) {
            editor = editor.select(updated.selection.start, updated.selection.end)
        }
        field = updated
        return editor
    }

    fun format(format: TextFormat?): RichTextEditor {
        editor = if (format == null) editor.clearFormatting() else editor.toggle(format)
        return editor
    }

    fun isActive(format: TextFormat): Boolean {
        if (editor.selectedStart == editor.selectedEnd) return format in editor.typingFormats
        return editor.content.spans.filter { it.format == format }.sortedBy { it.start }
            .fold(editor.selectedStart) { end, span ->
                if (span.start <= end) maxOf(end, span.endExclusive) else end
            } >= editor.selectedEnd
    }
}

@Composable
internal fun rememberRichTextInputState(editor: RichTextEditor): RichTextInputState {
    val state = remember { RichTextInputState(editor) }
    LaunchedEffect(editor) { state.sync(editor) }
    return state
}

@Composable
internal fun RichTextInput(
    state: RichTextInputState,
    onChange: (RichTextEditor) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedTextField(
        value = state.field,
        label = { Text(stringResource(R.string.note_body)) },
        onValueChange = { onChange(state.update(it)) },
        enabled = enabled,
        modifier = modifier.focusRequester(state.focusRequester),
        visualTransformation = remember(state.editor.content) {
            VisualTransformation { value ->
                val styled = if (value.text == state.editor.content.plainText) {
                    state.editor.content.toAnnotatedString()
                } else {
                    AnnotatedString(value.text)
                }
                TransformedText(styled, OffsetMapping.Identity)
            }
        },
        keyboardOptions = KeyboardOptions.Default.copy(
            capitalization = KeyboardCapitalization.Sentences,
        ),
    )
}

/** Prefer the actual selection/composition over an ambiguous diff of repeated characters. */
internal fun inputReplacement(old: TextFieldValue, new: TextFieldValue): Replacement {
    val delta = new.text.length - old.text.length
    fun replacement(start: Int, end: Int): Replacement? {
        val insertedEnd = end + delta
        if (start < 0 || end > old.text.length || insertedEnd < start || insertedEnd > new.text.length) return null
        if (!old.text.startsWith(new.text.substring(0, start)) ||
            old.text.substring(end) != new.text.substring(insertedEnd)) return null
        return Replacement(start, end, new.text.substring(start, insertedEnd))
    }
    // An active composing region is what commitText/setComposingText normally replaces.
    old.composition?.let { replacement(it.min, it.max)?.let { edit -> return edit } }
    replacement(old.selection.min, old.selection.max)?.let { return it }
    if (delta < 0 && old.selection.collapsed && new.selection.collapsed) {
        replacement(new.selection.start, new.selection.start - delta)?.let { return it }
    }
    return singleReplacement(old.text, new.text)
}

internal data class Replacement(val start: Int, val endExclusive: Int, val inserted: String)

/** Compose's value callback supplies the new text; find its smallest contiguous replacement. */
internal fun singleReplacement(old: String, new: String): Replacement {
    var prefix = 0
    while (prefix < minOf(old.length, new.length) && old[prefix] == new[prefix]) prefix++
    if (prefix > 0 && prefix < old.length && old[prefix - 1].isHighSurrogate() && old[prefix].isLowSurrogate()) prefix--
    var oldEnd = old.length
    var newEnd = new.length
    while (oldEnd > prefix && newEnd > prefix && old[oldEnd - 1] == new[newEnd - 1]) {
        oldEnd--
        newEnd--
    }
    if (oldEnd > prefix && oldEnd < old.length && old[oldEnd - 1].isHighSurrogate() && old[oldEnd].isLowSurrogate()) {
        oldEnd++
        newEnd++
    }
    return Replacement(prefix, oldEnd, new.substring(prefix, newEnd))
}
