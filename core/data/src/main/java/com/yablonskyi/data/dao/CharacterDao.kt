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

    @Query("SELECT * FROM character")
    fun getAllCharacters(): Flow<List<CharacterEntity>>

    @Transaction
    @Query("SELECT * FROM character WHERE id IN (:characterIds)")
    suspend fun getCharacterSheetsByIds(characterIds: List<Long>): List<CharacterSheetEntity>

    @Transaction
    @Query("SELECT * FROM character WHERE id = :characterId")
    suspend fun getCharacterSheetById(characterId: Long): CharacterSheetEntity

    @Transaction
    @Query("SELECT * FROM character")
    suspend fun getAllCharacterSheets(): List<CharacterSheetEntity>

    @Query("DELETE FROM character")
    suspend fun deleteAllCharacters()
}
