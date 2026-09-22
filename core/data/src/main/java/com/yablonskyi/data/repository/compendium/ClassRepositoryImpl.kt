package com.yablonskyi.data.repository.compendium

import com.yablonskyi.data.dao.ClassDao
import com.yablonskyi.data.mapper.toEntity
import com.yablonskyi.data.mapper.toModel
import com.yablonskyi.data.rulebook.BuiltInRulebookLoader
import com.yablonskyi.domain.backup.BackupAccessGate
import com.yablonskyi.domain.repository.ClassRepository
import com.yablonskyi.model.rulebook.CharacterClass
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ClassRepositoryImpl @Inject constructor(
    private val loader: BuiltInRulebookLoader,
    private val dao: ClassDao,
    private val backupGate: BackupAccessGate,
) : ClassRepository {
    override suspend fun insert(cls: CharacterClass) = backupGate.access { dao.insert(cls.toEntity()) }

    override suspend fun insertAll(classes: List<CharacterClass>) = backupGate.access {
        dao.insertAll(classes.map { it.toEntity() })
    }
    override suspend fun update(cls: CharacterClass) = backupGate.access { dao.update(cls.toEntity()) }

    override suspend fun delete(cls: CharacterClass) = backupGate.access { dao.delete(cls.toEntity()) }

    override suspend fun deleteClasses(classes: List<CharacterClass>) = backupGate.access {
        dao.deleteClasses(classes.map { it.toEntity() })
    }
    override fun getClassById(classId: String): Flow<CharacterClass?> = combine(
        flow { emit(loader.getClasses().map { it.toModel() }) },
        dao.getClassById(classId).map { it?.toModel() }
    ) { builtIn, homebrew ->
        homebrew ?: builtIn.firstOrNull { it.id == classId }
    }

    override fun getAllClasses(): Flow<List<CharacterClass>> = combine(
        flow { emit(loader.getClasses().map { it.toModel() }) },
        dao.getHomebrew().map { entities -> entities.map { it.toModel() } }
    ) { builtIn, homebrew ->
        builtIn + homebrew
    }
}
