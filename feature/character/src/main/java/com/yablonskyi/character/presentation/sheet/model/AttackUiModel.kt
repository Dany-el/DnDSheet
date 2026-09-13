package com.yablonskyi.character.presentation.sheet.model

import com.yablonskyi.model.character.Attack

data class AttackUiModel(
    val id: Long,
    val name: String,
    val toHit: String,
    val damage: String,
    val originalAttack: Attack
)
