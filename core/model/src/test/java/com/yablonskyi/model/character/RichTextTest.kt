package com.yablonskyi.model.character

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class RichTextTest {
    @Test
    fun overlappingFormatsRoundTripWithoutChangingText() {
        val note = Note(
            id = "15412a7e-37e6-4e8a-92cb-af49e0759032",
            topic = "Travel",
            text = RichText(
                plainText = "A😀\nB",
                spans = listOf(
                    TextSpan(0, 3, TextFormat.BOLD),
                    TextSpan(1, 5, TextFormat.ITALIC),
                ),
            ),
        )

        val decoded = Json.decodeFromString<Note>(Json.encodeToString(note))

        assertEquals(note, decoded)
        listOf(decoded).validateNotes()
    }

    @Test
    fun adjacentAndOverlappingSpansOfSameFormatMerge() {
        val text = RichText(
            plainText = "abcdef",
            spans = listOf(
                TextSpan(2, 4, TextFormat.BOLD),
                TextSpan(0, 2, TextFormat.BOLD),
                TextSpan(3, 6, TextFormat.BOLD),
                TextSpan(4, 4, TextFormat.ITALIC),
            ),
        )

        assertEquals(listOf(TextSpan(0, 6, TextFormat.BOLD)), text.normalized().spans)
    }

    @Test
    fun invalidStoredRangesAndVersionsAreRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            RichText(plainText = "a", spans = listOf(TextSpan(0, 2, TextFormat.BOLD))).validate()
        }
        assertThrows(IllegalArgumentException::class.java) {
            RichText(version = 2).validate()
        }
        assertThrows(IllegalArgumentException::class.java) {
            RichText(plainText = "😀", spans = listOf(TextSpan(1, 2, TextFormat.BOLD))).validate()
        }
    }

    @Test
    fun invalidAndDuplicateNoteIdsAreRejected() {
        val note = Note("15412a7e-37e6-4e8a-92cb-af49e0759032", "Topic")
        assertThrows(IllegalArgumentException::class.java) { listOf(note, note).validateNotes() }
        assertThrows(IllegalArgumentException::class.java) { listOf(note.copy(id = "bad")).validateNotes() }
        assertThrows(IllegalArgumentException::class.java) { listOf(note.copy(topic = " \n")).validateNotes() }
    }
}
