package com.yablonskyi.wizard

import com.yablonskyi.domain.repository.CharacterRepository
import com.yablonskyi.model.character.Character
import com.yablonskyi.model.character.CharacterSheet
import kotlinx.coroutines.flow.flowOf

class FakeCharacterRepository : CharacterRepository {
    override suspend fun reorderCharacters(orderedIds: List<Long>) = error("Unused")
    val saved = mutableListOf<Character>()
    var fail = false
    override suspend fun insertCharacter(character: Character): Long {
        if (fail) error("Save failed")
        saved += character
        return 42L
    }
    override fun getAllCharacters() = flowOf(saved.toList())
    override fun getCharacterById(id: Long) = flowOf(saved.firstOrNull())
    override suspend fun insertCharacters(sheets: List<CharacterSheet>) = error("Unused")
    override suspend fun restoreCharacters(sheets: List<CharacterSheet>) = error("Unused")
    override suspend fun applyChange(id: Long, change: com.yablonskyi.domain.character.CharacterChange): Character {
            val current = getCharacterSheetById(id).character
            val updated = com.yablonskyi.domain.character.applyCharacterChange(current, change)
            updateCharacter(updated)
            return updated
        }
        override suspend fun updateCharacter(character: Character) = error("Unused")
    override suspend fun deleteCharacter(character: Character) = error("Unused")
    override suspend fun deleteCharacters(characters: List<Character>) = error("Unused")
    override suspend fun getCharacterSheetsByIds(characterIds: List<Long>): List<CharacterSheet> = error("Unused")
    override suspend fun getCharacterSheetById(characterId: Long): CharacterSheet = error("Unused")
    override suspend fun getAllCharacterSheets(): List<CharacterSheet> = error("Unused")
}
