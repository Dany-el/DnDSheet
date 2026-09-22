package com.yablonskyi.data.repository.character

import androidx.room.withTransaction
import com.yablonskyi.data.AppDatabase
import com.yablonskyi.data.dao.AttackDao
import com.yablonskyi.data.dao.CharacterDao
import com.yablonskyi.data.dao.SpellDao
import com.yablonskyi.data.entity.CharacterSpellCrossRefEntity
import com.yablonskyi.data.mapper.toEntity
import com.yablonskyi.data.mapper.toModel
import com.yablonskyi.domain.backup.BackupAccessGate
import com.yablonskyi.domain.repository.CharacterRepository
import com.yablonskyi.model.character.Character
import com.yablonskyi.model.character.CharacterSheet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CharacterRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val characterDao: CharacterDao,
    private val attackDao: AttackDao,
    private val spellDao: SpellDao,
    private val backupGate: BackupAccessGate,
) : CharacterRepository {
    override suspend fun reorderCharacters(orderedIds: List<Long>) {
        backupGate.access { characterDao.reorderCharacters(orderedIds) }
    }

    override suspend fun insertCharacter(character: Character): Long {
        return backupGate.access { database.withTransaction {
            val position = characterDao.findCharacterById(character.id)?.sortOrder
                ?: characterDao.nextSortOrder()
            characterDao.insertCharacter(character.toEntity().copy(sortOrder = position))
        } }
    }

    override suspend fun insertCharacters(sheets: List<CharacterSheet>) {
        backupGate.access { database.withTransaction {
            insertSheetsInternal(sheets)
        } }
    }

    override suspend fun restoreCharacters(sheets: List<CharacterSheet>) {
        backupGate.access { database.withTransaction {
            characterDao.deleteAllCharacters()
            insertSheetsInternal(sheets)
        } }
    }

    override suspend fun applyChange(id: Long, change: com.yablonskyi.domain.character.CharacterChange): Character = backupGate.access { database.withTransaction {
        val entity = characterDao.findCharacterById(id) ?: error("Character no longer exists")
        val current = entity.toModel()
        val updated = com.yablonskyi.domain.character.applyCharacterChange(current, change)
        characterDao.updateCharacter(updated.toEntity().copy(sortOrder = entity.sortOrder))
        updated
    } }

    override suspend fun updateCharacter(character: Character) {
        backupGate.access { database.withTransaction {
            val current = characterDao.findCharacterById(character.id) ?: error("Character no longer exists")
            characterDao.updateCharacter(character.toEntity().copy(sortOrder = current.sortOrder))
        } }
    }

    override suspend fun deleteCharacter(character: Character) {
        backupGate.access { characterDao.deleteCharacter(character.toEntity()) }
    }

    override suspend fun deleteCharacters(characters: List<Character>) {
        backupGate.access { characterDao.deleteCharacters(characters.map { it.toEntity() }) }
    }

    override fun getCharacterById(id: Long): Flow<Character?> {
        return characterDao.getCharacterById(id).map { it?.toModel() }
    }

    override fun getAllCharacters(): Flow<List<Character>> {
        return characterDao.getAllCharacters().map { entities -> entities.map { it.toModel() } }
    }

    override suspend fun getCharacterSheetsByIds(characterIds: List<Long>): List<CharacterSheet> {
        return characterDao.getCharacterSheetsByIds(characterIds).map { entity ->
            CharacterSheet(
                character = entity.character.toModel(),
                spells = entity.spells.map { it.toModel() },
                attacks = entity.attacks.map { it.toModel() }
            )
        }
    }

    override suspend fun getCharacterSheetById(characterId: Long): CharacterSheet {
        val entity = characterDao.getCharacterSheetById(characterId)
        return CharacterSheet(
            character = entity.character.toModel(),
            spells = entity.spells.map { it.toModel() },
            attacks = entity.attacks.map { it.toModel() }
        )
    }

    override suspend fun getAllCharacterSheets(): List<CharacterSheet> {
        return characterDao.getAllCharacterSheets().map { entity ->
            CharacterSheet(
                character = entity.character.toModel(),
                spells = entity.spells.map { it.toModel() },
                attacks = entity.attacks.map { it.toModel() }
            )
        }
    }

    private suspend fun insertSheetsInternal(sheets: List<CharacterSheet>) {
        sheets.forEach { sheet ->
            val newCharacter = sheet.character.copy(id = 0)
            val newCharId = characterDao.insertCharacter(
                newCharacter.toEntity().copy(sortOrder = characterDao.nextSortOrder())
            )

            val newAttacks = sheet.attacks.map { it.copy(attackId = 0, characterId = newCharId) }
            attackDao.insertAttacks(newAttacks.map { it.toEntity() })

            sheet.spells.forEach { oldSpell ->
                val newSpell = oldSpell.copy(spellId = 0)
                var newSpellId = spellDao.insertSpell(newSpell.toEntity())

                if (newSpellId == -1L) {
                    newSpellId = spellDao.getSpellIdByName(newSpell.name)
                        ?: throw IllegalStateException("Spell should exist but ID not found")
                }

                spellDao.assignSpellToCharacter(
                    CharacterSpellCrossRefEntity(characterId = newCharId, spellId = newSpellId)
                )
            }
        }
    }
}
