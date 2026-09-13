package com.yablonskyi.domain.repository

import com.yablonskyi.model.character.CharacterSheet

interface CharacterFileRepository {
    suspend fun read(uri: String): List<CharacterSheet>
    suspend fun import(sheets: List<CharacterSheet>)
    suspend fun export(uri: String, characterIds: List<Long>)
}
