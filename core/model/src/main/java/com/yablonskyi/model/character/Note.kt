package com.yablonskyi.model.character

import java.util.UUID
import kotlinx.serialization.Serializable

@Serializable
data class Note(
    val id: String,
    val topic: String,
    val text: RichText = RichText(),
)

fun legacyNote(body: String): Note = Note(
    id = UUID.randomUUID().toString(),
    topic = "Notes",
    text = RichText(plainText = body),
)

fun validateNoteId(id: String) {
    require(runCatching { UUID.fromString(id).toString() == id }.getOrDefault(false)) {
        "Invalid note ID"
    }
}

/** Validates data at persistence and import boundaries, without rewriting the user's text. */
fun List<Note>.validateNotes() {
    val ids = HashSet<String>(size)
    for (note in this) {
        validateNoteId(note.id)
        require(ids.add(note.id)) { "Duplicate note ID: ${note.id}" }
        require(note.topic.isNotBlank()) { "Note topic must not be blank" }
        note.text.validate()
    }
}
