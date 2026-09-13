package com.yablonskyi.character.testutil

import com.yablonskyi.domain.repository.SpellRepository
import com.yablonskyi.model.character.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeSpellRepository : SpellRepository {
    val spells = MutableStateFlow(emptyList<Spell>())
    override fun getCharacterSpells(charId: Long) = spells
    override fun getAllSpellsInLibrary() = spells
    override fun getSpellById(id: Long) = spells.map { it.firstOrNull { spell -> spell.spellId == id } }
    override suspend fun insertSpell(spell: Spell): Long { spells.value += spell; return spell.spellId }
    override suspend fun insertSpells(spells: List<Spell>) { this.spells.value += spells }
    override suspend fun updateSpell(spell: Spell) { spells.value = spells.value.map { if (it.spellId == spell.spellId) spell else it } }
    override suspend fun deleteSpell(spell: Spell) { spells.value -= spell }
    override suspend fun deleteSpells(spells: List<Spell>) { this.spells.value -= spells.toSet() }
    override suspend fun assignSpellToCharacter(crossRef: CharacterSpellCrossRef) = Unit
    override suspend fun removeSpellFromCharacter(charId: Long, spellId: Long) = Unit
}
