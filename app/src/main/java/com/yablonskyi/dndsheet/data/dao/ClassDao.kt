package com.yablonskyi.dndsheet.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.yablonskyi.dndsheet.data.model.rulebook.CharacterClass
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(classes: List<CharacterClass>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cls: CharacterClass)

    @Update
    suspend fun update(cls: CharacterClass)

    @Delete
    suspend fun delete(cls: CharacterClass)

    @Delete
    suspend fun deleteClasses(classes: List<CharacterClass>)

    @Query("SELECT * FROM classes WHERE id=:classId")
    fun getClassById(classId: String): Flow<CharacterClass?>

    @Query("SELECT * FROM classes ORDER BY isHomebrew ASC, name ASC")
    fun getAllClasses(): Flow<List<CharacterClass>>

    @Query("SELECT * FROM classes WHERE isHomebrew = 1 ORDER BY name ASC")
    fun getHomebrew(): Flow<List<CharacterClass>>
}