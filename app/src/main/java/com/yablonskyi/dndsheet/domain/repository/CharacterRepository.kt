package com.yablonskyi.dndsheet.domain.repository

import com.yablonskyi.dndsheet.data.model.character.Character
import kotlinx.coroutines.flow.Flow

interface CharacterRepository {
    suspend fun insertCharacter(character: Character): Long

    suspend fun updateCharacter(character: Character)

    suspend fun deleteCharacter(character: Character)

    fun getCharacterById(id: Long): Flow<Character?>

    fun getAllCharacters(): Flow<List<Character>>
}