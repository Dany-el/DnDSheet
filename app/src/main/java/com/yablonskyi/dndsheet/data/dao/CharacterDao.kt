package com.yablonskyi.dndsheet.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.yablonskyi.dndsheet.data.model.character.Character
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacter(character: Character): Long

    @Update
    suspend fun updateCharacter(character: Character)

    @Delete
    suspend fun deleteCharacter(character: Character)

    @Transaction
    @Query("SELECT * FROM character WHERE id=:id")
    fun getCharacterById(id: Long): Flow<Character?>

    @Query("SELECT * FROM character ORDER BY name ASC")
    fun getAllCharacters(): Flow<List<Character>>
}