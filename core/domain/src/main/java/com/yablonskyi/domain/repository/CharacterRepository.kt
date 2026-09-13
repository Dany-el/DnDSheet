package com.yablonskyi.domain.repository

import com.yablonskyi.model.character.CharacterSheet
import com.yablonskyi.model.character.Character
import kotlinx.coroutines.flow.Flow

interface CharacterRepository {
    suspend fun insertCharacter(character: Character): Long

    suspend fun insertCharacters(sheets: List<CharacterSheet>)

    suspend fun restoreCharacters(sheets: List<CharacterSheet>)

    /** Atomically applies a field change to the latest row. Fails if the character no longer exists. */
    suspend fun applyChange(id: Long, change: com.yablonskyi.domain.character.CharacterChange): Character

    suspend fun updateCharacter(character: Character)

    suspend fun deleteCharacter(character: Character)

    suspend fun deleteCharacters(characters: List<Character>)

    fun getCharacterById(id: Long): Flow<Character?>

    fun getAllCharacters(): Flow<List<Character>>

    suspend fun getCharacterSheetsByIds(characterIds: List<Long>): List<CharacterSheet>

    suspend fun getCharacterSheetById(characterId: Long): CharacterSheet

    suspend fun getAllCharacterSheets(): List<CharacterSheet>
}