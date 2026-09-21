package com.yablonskyi.data.utils

import com.yablonskyi.model.character.CharacterSheet
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

/** Stable, reflection-free codec for character backup files. */
object CharacterBackupCodec {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    fun encode(sheets: List<CharacterSheet>): String = json.encodeToString(sheets)

    fun decode(value: String): List<CharacterSheet> =
        json.decodeFromString<List<CharacterSheet>>(value)
}
