package com.yablonskyi.domain.repository

import com.yablonskyi.model.character.Attack
import kotlinx.coroutines.flow.Flow

interface AttackRepository {
    suspend fun insertAttack(attack: Attack): Long

    suspend fun updateAttack(attack: Attack)

    suspend fun deleteAttack(attack: Attack)

    fun getAttacksForCharacter(charId: Long): Flow<List<Attack>>
}