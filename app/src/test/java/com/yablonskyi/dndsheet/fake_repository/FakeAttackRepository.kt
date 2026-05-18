package com.yablonskyi.dndsheet.fake_repository

import com.yablonskyi.dndsheet.data.model.character.Attack
import com.yablonskyi.dndsheet.domain.repository.AttackRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAttackRepository : AttackRepository {

    private val _attacks = MutableStateFlow<List<Attack>>(emptyList())
    val insertedAttacks = mutableListOf<Attack>()
    val updatedAttacks = mutableListOf<Attack>()
    val deletedAttacks = mutableListOf<Attack>()

    fun setAttacks(attacks: List<Attack>) { _attacks.value = attacks }

    override fun getAttacksForCharacter(charId: Long): Flow<List<Attack>> =
        MutableStateFlow(_attacks.value.filter { it.characterId == charId })

    override suspend fun insertAttack(attack: Attack): Long {
        insertedAttacks.add(attack)
        _attacks.value += attack
        return attack.attackId
    }

    override suspend fun updateAttack(attack: Attack) {
        updatedAttacks.add(attack)
        _attacks.value = _attacks.value.map { if (it.attackId == attack.attackId) attack else it }
    }

    override suspend fun deleteAttack(attack: Attack) {
        deletedAttacks.add(attack)
        _attacks.value = _attacks.value.filter { it.attackId != attack.attackId }
    }
}