package com.yablonskyi.dndsheet.fake_repository

import com.yablonskyi.dndsheet.data.model.character.Character
import com.yablonskyi.dndsheet.data.model.character.CharacterSheet
import com.yablonskyi.dndsheet.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeCharacterRepository : CharacterRepository {
    private val _characters = MutableStateFlow<List<Character>>(emptyList())
    val insertedCharacters = mutableListOf<Character>()
    val deletedCharacters = mutableListOf<Character>()
    val updatedCharacters = mutableListOf<Character>()
    var lastInsertedId = 0L

    fun setCharacters(characters: List<Character>) {
        _characters.value = characters
    }

    override fun getAllCharacters(): Flow<List<Character>> = _characters

    override fun getCharacterById(id: Long): Flow<Character?> =
        MutableStateFlow(_characters.value.find { it.id == id })

    override suspend fun insertCharacter(character: Character): Long {
        insertedCharacters.add(character)
        lastInsertedId++
        val withId = character.copy(id = lastInsertedId)
        _characters.value += withId
        return lastInsertedId
    }

    override suspend fun insertCharacters(sheets: List<CharacterSheet>) {
        TODO("Not yet implemented")
    }

    override suspend fun updateCharacter(character: Character) {
        updatedCharacters.add(character)
        _characters.value = _characters.value.map { if (it.id == character.id) character else it }
    }

    override suspend fun deleteCharacter(character: Character) {
        deletedCharacters.add(character)
        _characters.value = _characters.value.filter { it.id != character.id }
    }

    override suspend fun deleteCharacters(characters: List<Character>) {
        deletedCharacters.addAll(characters)
        val ids = characters.map { it.id }.toSet()
        _characters.value = _characters.value.filter { it.id !in ids }
    }

    override suspend fun getCharacterSheetsByIds(characterIds: List<Long>): List<CharacterSheet> =
        _characters.value
            .filter { it.id in characterIds }
            .map { CharacterSheet(character = it, spells = emptyList(), attacks = emptyList()) }

    override suspend fun getCharacterSheetById(characterId: Long): CharacterSheet {
        val character = _characters.value.first { it.id == characterId }
        return CharacterSheet(character = character, spells = emptyList(), attacks = emptyList())
    }

    override suspend fun getAllCharacterSheets(): List<CharacterSheet> =
        _characters.value.map {
            CharacterSheet(character = it, spells = emptyList(), attacks = emptyList())
        }
}