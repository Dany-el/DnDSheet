package com.yablonskyi.domain

import com.yablonskyi.model.character.CharacterSheet

fun interface CharacterSheetHtmlRenderer {
    suspend fun render(sheet: CharacterSheet, languageCode: String): Result<RenderedCharacterSheet>
}

data class RenderedCharacterSheet(val html: String, val jobName: String)
