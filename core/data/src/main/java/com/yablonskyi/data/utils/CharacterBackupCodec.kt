package com.yablonskyi.data.utils

import com.yablonskyi.model.character.CharacterSheet
import com.yablonskyi.model.character.legacyNote
import com.yablonskyi.model.character.validateNotes
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

/** Stable, reflection-free codec for character backup files. */
object CharacterBackupCodec {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    fun encode(sheets: List<CharacterSheet>): String {
        sheets.forEach {
            it.character.notes.validateNotes()
            require(it.attacks.all { attack -> attack.usages.isNotEmpty() && attack.fixedDamage >= 0 }) { "Invalid attack damage or usage" }
        }
        return json.encodeToString(sheets)
    }

    fun decode(value: String): List<CharacterSheet> {
        val source = json.parseToJsonElement(value) as? JsonArray
            ?: throw IllegalArgumentException("Character document must be an array")
        val normalized = JsonArray(source.map { element ->
            val sheet = element as? JsonObject ?: throw IllegalArgumentException("Invalid character record")
            val character = sheet["character"] as? JsonObject
                ?: throw IllegalArgumentException("Missing character")
            val oldNotes = character["notes"]
            val notes = when {
                oldNotes == null -> null
                oldNotes is JsonPrimitive && oldNotes.isString ->
                    json.encodeToJsonElement(listOf(legacyNote(oldNotes.content)))
                oldNotes is JsonArray -> oldNotes
                else -> throw IllegalArgumentException("Invalid character notes")
            }
            JsonObject(sheet + ("character" to JsonObject(character + listOfNotNull(
                notes?.let { "notes" to it },
            ))))
        })
        return json.decodeFromJsonElement<List<CharacterSheet>>(normalized).also { sheets ->
            sheets.forEach {
                it.character.notes.validateNotes()
                require(it.attacks.all { attack -> attack.usages.isNotEmpty() && attack.fixedDamage >= 0 }) { "Invalid attack damage or usage" }
            }
        }
    }
}