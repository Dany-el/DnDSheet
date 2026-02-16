package com.yablonskyi.dndsheet.data.repository

import com.yablonskyi.dndsheet.data.dao.CharacterDao
import com.yablonskyi.dndsheet.data.model.character.Character
import com.yablonskyi.dndsheet.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CharacterRepositoryImpl @Inject constructor(
    private val characterDao: CharacterDao
) : CharacterRepository {

    override suspend fun insertCharacter(character: Character): Long {
        return characterDao.insertCharacter(character)
    }

    override suspend fun updateCharacter(character: Character) {
        characterDao.updateCharacter(character)
    }

    override suspend fun deleteCharacter(character: Character) {
        characterDao.deleteCharacter(character)
    }

    override fun getCharacterById(id: Long): Flow<Character?> {
        return characterDao.getCharacterById(id)
    }

    override fun getAllCharacters(): Flow<List<Character>> {
        return characterDao.getAllCharacters()
    }
}