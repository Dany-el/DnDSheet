package com.yablonskyi.character.presentation.sheet.model

import androidx.compose.runtime.Immutable
import com.yablonskyi.ui.utils.AttackCalculator

@Immutable
data class AttackUiModel(
    val id: Long,
    val name: String,
    val toHit: String,
    val damage: String,
    val calculator: AttackCalculator,
    val description: String = calculator.attack.notes,
    val usages: Set<com.yablonskyi.model.character.AttackUsage> = calculator.attack.usages,
    val canRollDamage: Boolean = calculator.attack.damageMode == com.yablonskyi.model.character.DamageMode.DICE
)