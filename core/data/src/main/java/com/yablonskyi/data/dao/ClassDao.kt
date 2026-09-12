package com.yablonskyi.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.yablonskyi.data.entity.CharacterClassEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(classes: List<CharacterClassEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cls: CharacterClassEntity)

    @Update
    suspend fun update(cls: CharacterClassEntity)

    @Delete
    suspend fun delete(cls: CharacterClassEntity)

    @Delete
    suspend fun deleteClasses(classes: List<CharacterClassEntity>)

    @Query("SELECT * FROM classes WHERE id=:classId")
    fun getClassById(classId: String): Flow<CharacterClassEntity?>

    @Query("SELECT * FROM classes ORDER BY isHomebrew ASC, name ASC")
    fun getAllClasses(): Flow<List<CharacterClassEntity>>

    @Query("SELECT * FROM classes WHERE isHomebrew = 1 ORDER BY name ASC")
    fun getHomebrew(): Flow<List<CharacterClassEntity>>
}
