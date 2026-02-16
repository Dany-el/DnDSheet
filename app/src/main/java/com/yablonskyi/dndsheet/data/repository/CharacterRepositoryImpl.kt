package com.yablonskyi.dndsheet.data.repository

import androidx.room.withTransaction
import com.yablonskyi.dndsheet.data.AppDatabase
import com.yablonskyi.dndsheet.data.dao.AttackDao
import com.yablonskyi.dndsheet.data.dao.CharacterDao
import com.yablonskyi.dndsheet.data.dao.SpellDao
import com.yablonskyi.dndsheet.data.model.character.Character
import com.yablonskyi.dndsheet.data.model.character.CharacterSheet
import com.yablonskyi.dndsheet.data.model.character.CharacterSpellCrossRef
import com.yablonskyi.dndsheet.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CharacterRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val characterDao: CharacterDao,
    private val attackDao: AttackDao,
    private val spellDao: SpellDao,
) : CharacterRepository {

    override suspend fun insertCharacter(character: Character): Long {
        return characterDao.insertCharacter(character)
    }

    override suspend fun insertCharacters(sheets: List<CharacterSheet>) {
        database.withTransaction {
            sheets.forEach { sheet ->

                val newCharacter = sheet.character.copy(id = 0)
                val newCharId = characterDao.insertCharacter(newCharacter)

                val newAttacks = sheet.attacks.map { oldAttack ->
                    oldAttack.copy(attackId = 0, characterId = newCharId)
                }
                attackDao.insertAttacks(newAttacks)

                sheet.spells.forEach { oldSpell ->
                    val newSpell = oldSpell.copy(spellId = 0)
                    var newSpellId = spellDao.insertSpell(newSpell)

                    if (newSpellId == -1L) {
                        newSpellId = spellDao.getSpellIdByName(newSpell.name)
                            ?: throw IllegalStateException("Spell should exist but ID not found")
                    }

                    val crossRef = CharacterSpellCrossRef(
                        characterId = newCharId,
                        spellId = newSpellId
                    )
                    spellDao.assignSpellToCharacter(crossRef)
                }
            }
        }
    }

    override suspend fun updateCharacter(character: Character) {
        characterDao.updateCharacter(character)
    }

    override suspend fun deleteCharacter(character: Character) {
        characterDao.deleteCharacter(character)
    }

    override suspend fun deleteCharacters(characters: List<Character>) {
        characterDao.deleteCharacters(characters)
    }

    override fun getCharacterById(id: Long): Flow<Character?> {
        return characterDao.getCharacterById(id)
    }

    override fun getAllCharacters(): Flow<List<Character>> {
        return characterDao.getAllCharacters()
    }

    override suspend fun getCharacterSheetsByIds(characterIds: List<Long>): List<CharacterSheet> {
        return characterDao.getCharacterSheetsByIds(characterIds)
    }
}