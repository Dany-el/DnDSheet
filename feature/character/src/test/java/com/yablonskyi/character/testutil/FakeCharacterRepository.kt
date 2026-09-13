package com.yablonskyi.character.testutil

import com.yablonskyi.domain.character.*
import com.yablonskyi.domain.repository.CharacterRepository
import com.yablonskyi.model.character.Character
import com.yablonskyi.model.character.CharacterSheet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeCharacterRepository : CharacterRepository {
    val characters = MutableStateFlow(listOf(Character(id = 7, name = "Hero")))
    var failure: Exception? = null
    override fun getAllCharacters() = characters
    override fun getCharacterById(id: Long) = characters.map { list -> list.firstOrNull { it.id == id } }
    override suspend fun applyChange(id: Long, change: CharacterChange): Character {
        failure?.let { throw it }
        val updated = applyCharacterChange(characters.value.first { it.id == id }, change)
        updateCharacter(updated)
        return updated
    }
    override suspend fun updateCharacter(character: Character) { characters.value = characters.value.map { if (it.id == character.id) character else it } }
    override suspend fun insertCharacter(character: Character): Long { characters.value += character; return character.id }
    override suspend fun insertCharacters(sheets: List<CharacterSheet>) { failure?.let { throw it }; characters.value += sheets.map { it.character } }
    override suspend fun restoreCharacters(sheets: List<CharacterSheet>) { characters.value = sheets.map { it.character } }
    override suspend fun deleteCharacter(character: Character) { characters.value = characters.value.filterNot { it.id == character.id } }
    override suspend fun deleteCharacters(characters: List<Character>) { failure?.let { throw it }; characters.forEach { deleteCharacter(it) } }
    override suspend fun getCharacterSheetById(characterId: Long) = CharacterSheet(characters.value.first { it.id == characterId }, emptyList(), emptyList())
    override suspend fun getCharacterSheetsByIds(characterIds: List<Long>) = characterIds.map { getCharacterSheetById(it) }
    override suspend fun getAllCharacterSheets() = getCharacterSheetsByIds(characters.value.map { it.id })
}
