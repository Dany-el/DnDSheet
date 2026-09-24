package com.yablonskyi.data.converters

import com.yablonskyi.model.character.Note
import com.yablonskyi.model.character.validateNotes
import kotlinx.serialization.json.Json

/** Strict codec for the character notes TEXT column. */
object NotesCodec {
    private val json = Json { encodeDefaults = true }

    fun encode(notes: List<Note>): String {
        notes.validateNotes()
        return json.encodeToString(notes)
    }

    fun decode(value: String): List<Note> = json.decodeFromString<List<Note>>(value).also {
        it.validateNotes()
    }
}