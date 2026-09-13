package com.yablonskyi.character.testutil

import com.yablonskyi.domain.repository.AttackRepository
import com.yablonskyi.model.character.Attack
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAttackRepository : AttackRepository {
    val attacks = MutableStateFlow(emptyList<Attack>())
    override fun getAttacksForCharacter(charId: Long) = attacks
    override suspend fun insertAttack(attack: Attack): Long { attacks.value += attack.copy(attackId = 1); return 1 }
    override suspend fun updateAttack(attack: Attack) { attacks.value = attacks.value.map { if (it.attackId == attack.attackId) attack else it } }
    override suspend fun deleteAttack(attack: Attack) { attacks.value = attacks.value.filterNot { it.attackId == attack.attackId } }
}
