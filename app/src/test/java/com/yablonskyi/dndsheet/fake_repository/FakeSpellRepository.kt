package com.yablonskyi.dndsheet.fake_repository

import com.yablonskyi.dndsheet.data.model.character.CharacterSpellCrossRef
import com.yablonskyi.dndsheet.data.model.character.Spell
import com.yablonskyi.dndsheet.domain.repository.SpellRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSpellRepository : SpellRepository {

    private val _librarySpells = MutableStateFlow<List<Spell>>(emptyList())
    private val _characterSpells = MutableStateFlow<List<Spell>>(emptyList())
    val assignedCrossRefs = mutableListOf<CharacterSpellCrossRef>()
    val removedPairs = mutableListOf<Pair<Long, Long>>()

    fun setLibrarySpells(spells: List<Spell>) { _librarySpells.value = spells }
    fun setCharacterSpells(spells: List<Spell>) { _characterSpells.value = spells }

    override fun getAllSpellsInLibrary(): Flow<List<Spell>> = _librarySpells
    override fun getCharacterSpells(charId: Long): Flow<List<Spell>> = _characterSpells
    override fun getSpellById(id: Long): Flow<Spell?> =
        MutableStateFlow(_librarySpells.value.find { it.spellId == id })

    override suspend fun insertSpell(spell: Spell): Long { _librarySpells.value += spell; return spell.spellId }
    override suspend fun updateSpell(spell: Spell) { _librarySpells.value = _librarySpells.value.map { if (it.spellId == spell.spellId) spell else it } }
    override suspend fun insertSpells(spells: List<Spell>) { _librarySpells.value += spells }
    override suspend fun deleteSpell(spell: Spell) { _librarySpells.value -= spell }
    override suspend fun deleteSpells(spells: List<Spell>) { _librarySpells.value -= spells.toSet() }

    override suspend fun assignSpellToCharacter(crossRef: CharacterSpellCrossRef) {
        assignedCrossRefs.add(crossRef)
    }

    override suspend fun removeSpellFromCharacter(charId: Long, spellId: Long) {
        removedPairs.add(charId to spellId)
    }
}