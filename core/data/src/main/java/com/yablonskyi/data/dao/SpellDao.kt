package com.yablonskyi.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.yablonskyi.data.entity.CharacterSpellCrossRefEntity
import com.yablonskyi.data.entity.SpellEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SpellDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSpell(spell: SpellEntity): Long

    @Update
    suspend fun updateSpell(spell: SpellEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpells(spells: List<SpellEntity>)

    @Delete
    suspend fun deleteSpell(spell: SpellEntity)

    @Delete
    suspend fun deleteSpells(spells: List<SpellEntity>)

    @Query("SELECT * FROM spells ORDER BY level ASC, name ASC")
    fun getAllSpellsInLibrary(): Flow<List<SpellEntity>>

    @Query("SELECT spellId FROM spells WHERE name = :name LIMIT 1")
    suspend fun getSpellIdByName(name: String): Long?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun assignSpellToCharacter(crossRef: CharacterSpellCrossRefEntity)

    @Query("DELETE FROM character_spell_cross_ref WHERE characterId = :charId AND spellId = :spellId")
    suspend fun removeSpellFromCharacter(charId: Long, spellId: Long)

    @Query("""
        SELECT spells.* FROM spells
        INNER JOIN character_spell_cross_ref ON spells.spellId = character_spell_cross_ref.spellId
        WHERE character_spell_cross_ref.characterId = :charId
        ORDER BY spells.level ASC, spells.name ASC
    """)
    fun getCharacterSpells(charId: Long): Flow<List<SpellEntity>>

    @Query("""
        SELECT spells.* FROM spells
        INNER JOIN character_spell_cross_ref ON spells.spellId = character_spell_cross_ref.spellId
        WHERE character_spell_cross_ref.characterId = :charId AND spells.level = :level
        ORDER BY spells.name ASC
    """)
    fun getSpellsByLevel(charId: Long, level: Int): Flow<List<SpellEntity>>

    @Query("""
        SELECT spells.* FROM spells
        INNER JOIN character_spell_cross_ref ON spells.spellId = character_spell_cross_ref.spellId
        WHERE character_spell_cross_ref.characterId = :charId AND spells.isConcentration = 1
        ORDER BY spells.level ASC
    """)
    fun getSpellsByConcentration(charId: Long): Flow<List<SpellEntity>>

    @Query("""
        SELECT spells.* FROM spells
        INNER JOIN character_spell_cross_ref ON spells.spellId = character_spell_cross_ref.spellId
        WHERE character_spell_cross_ref.characterId = :charId AND spells.isRitual = 1
        ORDER BY spells.level ASC
    """)
    fun getSpellsByRitual(charId: Long): Flow<List<SpellEntity>>

    @Query("SELECT * FROM spells WHERE spellId = :id")
    fun getSpellById(id: Long): Flow<SpellEntity?>
}
