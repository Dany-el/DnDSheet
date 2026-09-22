package com.yablonskyi.data.repository.character

import com.yablonskyi.data.dao.SpellDao
import com.yablonskyi.data.mapper.toEntity
import com.yablonskyi.data.mapper.toModel
import com.yablonskyi.domain.backup.BackupAccessGate
import com.yablonskyi.domain.repository.SpellRepository
import com.yablonskyi.model.character.CharacterSpellCrossRef
import com.yablonskyi.model.character.Spell
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SpellRepositoryImpl @Inject constructor(
    private val spellDao: SpellDao,
    private val backupGate: BackupAccessGate,
) : SpellRepository {
    override suspend fun insertSpell(spell: Spell): Long {
        return backupGate.access { spellDao.insertSpell(spell.toEntity()) }
    }

    override suspend fun updateSpell(spell: Spell) {
        backupGate.access { spellDao.updateSpell(spell.toEntity()) }
    }

    override suspend fun insertSpells(spells: List<Spell>) {
        backupGate.access { spellDao.insertSpells(spells.map { it.toEntity() }) }
    }

    override suspend fun deleteSpell(spell: Spell) {
        backupGate.access { spellDao.deleteSpell(spell.toEntity()) }
    }

    override suspend fun deleteSpells(spells: List<Spell>) {
        backupGate.access { spellDao.deleteSpells(spells.map { it.toEntity() }) }
    }

    override fun getAllSpellsInLibrary(): Flow<List<Spell>> {
        return spellDao.getAllSpellsInLibrary().map { entities -> entities.map { it.toModel() } }
    }

    override suspend fun assignSpellToCharacter(crossRef: CharacterSpellCrossRef) {
        backupGate.access { spellDao.assignSpellToCharacter(crossRef.toEntity()) }
    }

    override suspend fun removeSpellFromCharacter(charId: Long, spellId: Long) {
        backupGate.access { spellDao.removeSpellFromCharacter(charId, spellId) }
    }

    override fun getCharacterSpells(charId: Long): Flow<List<Spell>> {
        return spellDao.getCharacterSpells(charId).map { entities -> entities.map { it.toModel() } }
    }

    override fun getSpellById(id: Long): Flow<Spell?> {
        return spellDao.getSpellById(id).map { it?.toModel() }
    }
}
