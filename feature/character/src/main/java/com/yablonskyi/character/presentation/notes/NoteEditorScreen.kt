package com.yablonskyi.character.presentation.notes

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatClear
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.yablonskyi.character.R
import com.yablonskyi.character.presentation.notes.editor.RichTextEditor
import com.yablonskyi.character.presentation.notes.editor.RichTextInput
import com.yablonskyi.character.presentation.notes.editor.rememberRichTextInputState
import com.yablonskyi.model.character.TextFormat
import com.yablonskyi.ui.components.SimpleTopAppBar
import com.yablonskyi.ui.R as UiR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    state: NoteEditorState,
    onBack: () -> Unit,
    onTopicChanged: (String) -> Unit,
    onBodyChanged: (RichTextEditor) -> Unit,
    onSave: () -> Unit,
    onDeleteRequested: () -> Unit,
    onDeleteDismissed: () -> Unit,
    onDeleteConfirmed: () -> Unit,
    onDiscardDismissed: () -> Unit,
    onDiscardConfirmed: () -> Unit,
) {
    val draft = state.draft
    val input = rememberRichTextInputState(
        draft?.let {
            RichTextEditor(it.current.text, it.selectionStart, it.selectionEnd, it.typingFormats)
        } ?: RichTextEditor()
    )
    val editable = state.status == NoteEditorStatus.READY && draft != null && !state.saving
    fun format(format: TextFormat?) {
        onBodyChanged(input.format(format))
        input.focusRequester.requestFocus()
    }
    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            SimpleTopAppBar(
                title = stringResource(R.string.note_editor_title),
                onNavigateBack = onBack
            ) {
                if (draft?.original != null && state.status == NoteEditorStatus.READY) {
                    IconButton(
                        onClick = onDeleteRequested,
                        enabled = !state.saving,
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(UiR.string.delete),
                            tint = if (editable) MaterialTheme.colorScheme.error else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                        )
                    }
                }
            }
        },
        bottomBar = {
            BottomAppBar(
                actions = {
                    Row(
                        Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState())
                    ) {
                        TextFormat.entries.forEach { format ->
                            val label = stringResource(
                                when (format) {
                                    TextFormat.BOLD -> R.string.note_format_bold
                                    TextFormat.ITALIC -> R.string.note_format_italic
                                    TextFormat.UNDERLINE -> R.string.note_format_underline
                                    TextFormat.STRIKETHROUGH -> R.string.note_format_strikethrough
                                }
                            )
                            IconToggleButton(
                                checked = input.isActive(format),
                                onCheckedChange = { format(format) },
                                enabled = editable,
                                modifier = Modifier.semantics { contentDescription = label },
                            ) {
                                Text(
                                    text = when (format) {
                                        TextFormat.BOLD -> "B"
                                        TextFormat.ITALIC -> "I"
                                        TextFormat.UNDERLINE -> "U"
                                        TextFormat.STRIKETHROUGH -> "S"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = if (format == TextFormat.BOLD) FontWeight.Bold else FontWeight.Normal,
                                    fontStyle = if (format == TextFormat.ITALIC) FontStyle.Italic else FontStyle.Normal,
                                    textDecoration = when (format) {
                                        TextFormat.UNDERLINE -> TextDecoration.Underline
                                        TextFormat.STRIKETHROUGH -> TextDecoration.LineThrough
                                        else -> TextDecoration.None
                                    },
                                )
                            }
                        }
                        OutlinedButton(
                            onClick = { format(null) },
                            enabled = editable,
                            shape = CircleShape
                        ) {
                            Icon(
                                Icons.Default.FormatClear,
                                stringResource(R.string.note_format_clear)
                            )
                        }
                    }
                },
                floatingActionButton = {
                    val containerColor =
                        if (!editable) ButtonDefaults.buttonColors().disabledContainerColor else
                            FloatingActionButtonDefaults.containerColor

                    val contentColor =
                        if (!editable) ButtonDefaults.buttonColors().disabledContentColor else
                            contentColorFor(containerColor)

                    Surface(
                        onClick = onSave,
                        enabled = editable,
                        modifier = Modifier.size(56.dp),
                        shape = FloatingActionButtonDefaults.shape,
                        color = containerColor,
                        contentColor = contentColor,
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = stringResource(UiR.string.save),
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when (state.status) {
                NoteEditorStatus.LOADING -> CircularProgressIndicator()
                NoteEditorStatus.MISSING_CHARACTER -> Text(stringResource(R.string.note_character_missing))
                NoteEditorStatus.MISSING_NOTE -> Text(stringResource(R.string.note_missing))
                NoteEditorStatus.ERROR -> Text(
                    text = state.error?.let { errorText(it) }
                        ?: stringResource(R.string.note_draft_unavailable),
                    color = MaterialTheme.colorScheme.error,
                )

                NoteEditorStatus.READY -> {
                    val draft = state.draft ?: return@Column
                    OutlinedTextField(
                        value = draft.current.topic,
                        onValueChange = onTopicChanged,
                        enabled = editable,
                        label = { Text(stringResource(R.string.note_topic)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = state.error == NoteEditorError.TOPIC_REQUIRED,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            capitalization = KeyboardCapitalization.Sentences,
                        ),
                    )
                    RichTextInput(
                        state = input,
                        enabled = editable,
                        onChange = onBodyChanged,
                        modifier = Modifier.fillMaxSize(),
                    )
                    state.error?.let {
                        Text(
                            errorText(it),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
    if (state.confirmDiscard) {
        AlertDialog(
            onDismissRequest = onDiscardDismissed,
            title = { Text(stringResource(R.string.note_discard_title)) },
            text = { Text(stringResource(R.string.note_discard_message)) },
            confirmButton = { TextButton(onClick = onDiscardConfirmed) { Text(stringResource(R.string.note_discard)) } },
            dismissButton = { TextButton(onClick = onDiscardDismissed) { Text(stringResource(UiR.string.cancel)) } },
        )
    }
    if (state.confirmDelete) {
        AlertDialog(
            onDismissRequest = onDeleteDismissed,
            title = { Text(stringResource(R.string.note_delete_title)) },
            text = { Text(stringResource(R.string.note_delete_message)) },
            confirmButton = { TextButton(onClick = onDeleteConfirmed) { Text(stringResource(UiR.string.delete)) } },
            dismissButton = { TextButton(onClick = onDeleteDismissed) { Text(stringResource(UiR.string.cancel)) } },
        )
    }
}

@Composable
private fun errorText(error: NoteEditorError): String = stringResource(
    when (error) {
        NoteEditorError.LOAD -> R.string.note_load_error
        NoteEditorError.DRAFT -> R.string.note_draft_error
        NoteEditorError.SAVE -> R.string.note_save_error
        NoteEditorError.CONFLICT -> R.string.note_conflict_error
        NoteEditorError.TOPIC_REQUIRED -> R.string.note_topic_required
        NoteEditorError.STALE_CHARACTER -> R.string.note_stale_character
    }
)