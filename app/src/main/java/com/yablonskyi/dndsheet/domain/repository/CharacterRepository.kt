package com.yablonskyi.dndsheet.domain.repository

import com.yablonskyi.dndsheet.data.model.character.Character
import com.yablonskyi.dndsheet.data.model.character.CharacterSheet
import kotlinx.coroutines.flow.Flow

interface CharacterRepository {
    suspend fun insertCharacter(character: Character): Long

    suspend fun insertCharacters(sheets: List<CharacterSheet>)

    suspend fun updateCharacter(character: Character)

    suspend fun deleteCharacter(character: Character)

    suspend fun deleteCharacters(characters: List<Character>)

    fun getCharacterById(id: Long): Flow<Character?>

    fun getAllCharacters(): Flow<List<Character>>

    suspend fun getCharacterSheetsByIds(characterIds: List<Long>): List<CharacterSheet>
}