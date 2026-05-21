package com.yablonskyi.dndsheet.data.repository

import com.yablonskyi.dndsheet.data.dao.ClassDao
import com.yablonskyi.dndsheet.data.model.rulebook.CharacterClass
import com.yablonskyi.dndsheet.data.rulebook.BuiltInRulebookLoader
import com.yablonskyi.dndsheet.domain.repository.ClassRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ClassRepositoryImpl @Inject constructor(
    private val loader: BuiltInRulebookLoader,
    private val dao: ClassDao
): ClassRepository {
    override suspend fun insert(cls: CharacterClass) = dao.insert(cls)

    override suspend fun insertAll(classes: List<CharacterClass>) = dao.insertAll(classes)

    override suspend fun delete(cls: CharacterClass) = dao.delete(cls)

    override suspend fun deleteClasses(classes: List<CharacterClass>) = dao.deleteClasses(classes)

    override fun getAllClasses(): Flow<List<CharacterClass>> = combine(
        flow { emit(loader.getClasses()) },
        dao.getHomebrew()
    ) { builtIn, homebrew ->
        builtIn + homebrew
    }
}