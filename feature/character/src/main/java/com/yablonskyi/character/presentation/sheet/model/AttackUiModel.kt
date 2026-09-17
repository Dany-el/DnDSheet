package com.yablonskyi.character.presentation.sheet.model

import androidx.compose.runtime.Immutable
import com.yablonskyi.ui.utils.AttackCalculator

@Immutable
data class AttackUiModel(
    val id: Long,
    val name: String,
    val toHit: String,
    val damage: String,
    val calculator: AttackCalculator
)