package com.yablonskyi.compendium.fake

import com.yablonskyi.domain.repository.SpellRepository
import com.yablonskyi.model.character.CharacterSpellCrossRef
import com.yablonskyi.model.character.Spell
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSpellRepository : SpellRepository {

    private val library = MutableStateFlow<List<Spell>>(emptyList())

    fun setLibrary(spells: List<Spell>) { library.value = spells }

    private val _spellById = MutableStateFlow<Spell?>(null)
    val inserted = mutableListOf<Spell>()
    val updated = mutableListOf<Spell>()

    fun setSpellById(spell: Spell) {
        _spellById.value = spell
    }

    override suspend fun insertSpell(spell: Spell): Long {
        inserted += spell
        return spell.spellId
    }

    override suspend fun updateSpell(spell: Spell) {
        updated += spell
        _spellById.value = spell
    }

    override suspend fun insertSpells(spells: List<Spell>) = Unit

    override suspend fun deleteSpell(spell: Spell) = Unit

    override suspend fun deleteSpells(spells: List<Spell>) = Unit

    override fun getAllSpellsInLibrary(): Flow<List<Spell>> = library

    override suspend fun assignSpellToCharacter(crossRef: CharacterSpellCrossRef) = Unit

    override suspend fun removeSpellFromCharacter(charId: Long, spellId: Long) = Unit

    override fun getCharacterSpells(charId: Long): Flow<List<Spell>> =
        MutableStateFlow(emptyList())

    override fun getSpellById(id: Long): Flow<Spell?> = _spellById
}
