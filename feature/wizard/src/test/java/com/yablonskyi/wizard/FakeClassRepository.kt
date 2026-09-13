package com.yablonskyi.wizard

import com.yablonskyi.domain.repository.ClassRepository
import com.yablonskyi.model.rulebook.CharacterClass
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeClassRepository : ClassRepository {

    private val _classes = MutableStateFlow<List<CharacterClass>>(emptyList())
    val deleted = mutableListOf<CharacterClass>()
    val insertedAll = mutableListOf<List<CharacterClass>>()
    val updated = mutableListOf<CharacterClass>()

    fun setClasses(classes: List<CharacterClass>) {
        _classes.value = classes
    }

    fun classes(): List<CharacterClass> = _classes.value

    override fun getAllClasses(): Flow<List<CharacterClass>> = _classes

    override fun getClassById(classId: String): Flow<CharacterClass?> =
        MutableStateFlow(_classes.value.firstOrNull { it.id == classId })

    override suspend fun insert(cls: CharacterClass) {
        _classes.value = _classes.value + cls
    }

    override suspend fun insertAll(classes: List<CharacterClass>) {
        insertedAll += classes
        _classes.value = _classes.value + classes
    }

    override suspend fun update(cls: CharacterClass) {
        updated += cls
        _classes.value = _classes.value.map { if (it.id == cls.id) cls else it }
    }

    override suspend fun delete(cls: CharacterClass) {
        deleted += cls
        _classes.value = _classes.value - cls
    }

    override suspend fun deleteClasses(classes: List<CharacterClass>) {
        deleted += classes
        _classes.value = _classes.value - classes
    }
}
