package com.yablonskyi.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.yablonskyi.data.entity.CharacterEntity
import com.yablonskyi.data.entity.CharacterSheetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacter(character: CharacterEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacters(characters: List<CharacterEntity>)

    @Update
    suspend fun updateCharacter(character: CharacterEntity)

    @Delete
    suspend fun deleteCharacter(character: CharacterEntity)

    @Delete
    suspend fun deleteCharacters(characters: List<CharacterEntity>)

    @Transaction
    @Query("SELECT * FROM character WHERE id=:id")
    fun getCharacterById(id: Long): Flow<CharacterEntity?>

    @Query("SELECT * FROM character WHERE id = :id")
    suspend fun findCharacterById(id: Long): CharacterEntity?

    @Query("SELECT * FROM character ORDER BY sortOrder, id")
    fun getAllCharacters(): Flow<List<CharacterEntity>>

    @Query("SELECT id FROM character ORDER BY sortOrder, id")
    suspend fun getOrderedIds(): List<Long>

    @Query("SELECT COALESCE(MAX(sortOrder), -1) + 1 FROM character")
    suspend fun nextSortOrder(): Long

    @Query("UPDATE character SET sortOrder = :position WHERE id = :id")
    suspend fun updateSortOrder(id: Long, position: Long)

    @Transaction
    suspend fun reorderCharacters(orderedIds: List<Long>) {
        val current = getOrderedIds()
        val existing = current.toSet()
        val requested = orderedIds.distinct().filter { it in existing }
        val requestedSet = requested.toSet()
        (requested + current.filterNot { it in requestedSet }).forEachIndexed { index, id ->
            updateSortOrder(id, index.toLong())
        }
    }

    @Transaction
    @Query("SELECT * FROM character WHERE id IN (:characterIds)")
    suspend fun getCharacterSheetsByIds(characterIds: List<Long>): List<CharacterSheetEntity>

    @Transaction
    @Query("SELECT * FROM character WHERE id = :characterId")
    suspend fun getCharacterSheetById(characterId: Long): CharacterSheetEntity

    @Transaction
    @Query("SELECT * FROM character ORDER BY sortOrder, id")
    suspend fun getAllCharacterSheets(): List<CharacterSheetEntity>

    @Query("DELETE FROM character")
    suspend fun deleteAllCharacters()
}
