package com.yablonskyi.dndsheet.domain.repository

import com.yablonskyi.dndsheet.data.model.rulebook.CharacterClass
import kotlinx.coroutines.flow.Flow

interface ClassRepository {
    suspend fun insert(cls: CharacterClass)
    suspend fun insertAll(classes: List<CharacterClass>)
    suspend fun update(cls: CharacterClass)
    suspend fun delete(cls: CharacterClass)
    suspend fun deleteClasses(classes: List<CharacterClass>)
    fun getClassById(classId: String): Flow<CharacterClass?>
    fun getAllClasses(): Flow<List<CharacterClass>>
}