package com.yablonskyi.dndsheet.data.repository

import com.yablonskyi.dndsheet.data.dao.AttackDao
import com.yablonskyi.dndsheet.data.model.character.Attack
import com.yablonskyi.dndsheet.domain.repository.AttackRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AttackRepositoryImpl @Inject constructor(
    private val attackDao: AttackDao
): AttackRepository {
    override suspend fun insertAttack(attack: Attack) : Long {
        return attackDao.insertAttack(attack)
    }

    override suspend fun updateAttack(attack: Attack) {
        attackDao.updateAttack(attack)
    }

    override suspend fun deleteAttack(attack: Attack) {
        attackDao.deleteAttack(attack)
    }

    override fun getAttacksForCharacter(charId: Long): Flow<List<Attack>> {
        return attackDao.getAttackForCharacter(charId)
    }
}