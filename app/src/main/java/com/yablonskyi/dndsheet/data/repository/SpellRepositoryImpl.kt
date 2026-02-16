package com.yablonskyi.dndsheet.data.repository

import com.yablonskyi.dndsheet.data.dao.SpellDao
import com.yablonskyi.dndsheet.data.model.character.CharacterSpellCrossRef
import com.yablonskyi.dndsheet.data.model.character.Spell
import com.yablonskyi.dndsheet.domain.repository.SpellRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SpellRepositoryImpl @Inject constructor(
    private val spellDao: SpellDao
): SpellRepository {
    override suspend fun insertSpell(spell: Spell) : Long {
        return spellDao.insertSpell(spell)
    }

    override suspend fun updateSpell(spell: Spell) {
        spellDao.updateSpell(spell)
    }

    override suspend fun insertSpells(spells: List<Spell>) {
        spellDao.insertSpells(spells)
    }

    override suspend fun deleteSpell(spell: Spell) {
        spellDao.deleteSpell(spell)
    }

    override suspend fun deleteSpells(spells: List<Spell>) {
        spellDao.deleteSpells(spells)
    }

    override fun getAllSpellsInLibrary(): Flow<List<Spell>> {
        return spellDao.getAllSpellsInLibrary()
    }

    override suspend fun assignSpellToCharacter(crossRef: CharacterSpellCrossRef) {
        spellDao.assignSpellToCharacter(crossRef)
    }

    override suspend fun removeSpellFromCharacter(charId: Long, spellId: Long) {
        spellDao.removeSpellFromCharacter(charId, spellId)
    }

    override fun getCharacterSpells(charId: Long): Flow<List<Spell>> {
        return spellDao.getCharacterSpells(charId)
    }

    override fun getSpellById(id: Long): Flow<Spell?> {
        return spellDao.getSpellById(id)
    }
}