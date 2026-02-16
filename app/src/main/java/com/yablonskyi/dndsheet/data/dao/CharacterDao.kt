package com.yablonskyi.dndsheet.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.yablonskyi.dndsheet.data.model.character.Character
import com.yablonskyi.dndsheet.data.model.character.CharacterSheet
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacter(character: Character): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacters(characters: List<Character>)

    @Update
    suspend fun updateCharacter(character: Character)

    @Delete
    suspend fun deleteCharacter(character: Character)

    @Delete
    suspend fun deleteCharacters(characters: List<Character>)

    @Transaction
    @Query("SELECT * FROM character WHERE id=:id")
    fun getCharacterById(id: Long): Flow<Character?>

    @Query("SELECT * FROM character")
    fun getAllCharacters(): Flow<List<Character>>

    @Transaction
    @Query("SELECT * FROM character WHERE id IN (:characterIds)")
    suspend fun getCharacterSheetsByIds(characterIds: List<Long>): List<CharacterSheet>
}