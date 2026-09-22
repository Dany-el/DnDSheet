package com.yablonskyi.data.repository.character

import com.yablonskyi.data.dao.AttackDao
import com.yablonskyi.data.mapper.toEntity
import com.yablonskyi.data.mapper.toModel
import com.yablonskyi.domain.backup.BackupAccessGate
import com.yablonskyi.domain.repository.AttackRepository
import com.yablonskyi.model.character.Attack
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AttackRepositoryImpl @Inject constructor(
    private val attackDao: AttackDao,
    private val backupGate: BackupAccessGate,
) : AttackRepository {
    override suspend fun insertAttack(attack: Attack): Long {
        return backupGate.access { attackDao.insertAttack(attack.toEntity()) }
    }

    override suspend fun updateAttack(attack: Attack) {
        backupGate.access { attackDao.updateAttack(attack.toEntity()) }
    }

    override suspend fun deleteAttack(attack: Attack) {
        backupGate.access { attackDao.deleteAttack(attack.toEntity()) }
    }

    override fun getAttacksForCharacter(charId: Long): Flow<List<Attack>> {
        return attackDao.getAttackForCharacter(charId).map { entities -> entities.map { it.toModel() } }
    }
}
