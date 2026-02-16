package com.yablonskyi.dndsheet.domain.repository

import com.yablonskyi.dndsheet.data.model.character.CharacterSpellCrossRef
import com.yablonskyi.dndsheet.data.model.character.Spell
import kotlinx.coroutines.flow.Flow

interface SpellRepository {
    suspend fun insertSpell(spell: Spell): Long

    suspend fun updateSpell(spell: Spell)

    suspend fun insertSpells(spells: List<Spell>)

    suspend fun deleteSpell(spell: Spell)

    suspend fun deleteSpells(spells: List<Spell>)

    fun getAllSpellsInLibrary(): Flow<List<Spell>>

    suspend fun assignSpellToCharacter(crossRef: CharacterSpellCrossRef)

    suspend fun removeSpellFromCharacter(charId: Long, spellId: Long)

    /**
     * Returns Flow with List sorted by level and name
     */
    fun getCharacterSpells(charId: Long): Flow<List<Spell>>

    fun getSpellById(id: Long): Flow<Spell?>
}