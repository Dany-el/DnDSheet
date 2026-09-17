package com.yablonskyi.model.dice

import kotlinx.serialization.Serializable

@Serializable
data class SavedDiceRoll(
    val id: Long = 0,
    val characterId: Long,
    val label: String,
    val numbers: List<Int>,
    val modifier: Int?,
    val result: Int,
    val dices: List<DiceGroup>,
    val timestamp: Long,
) {
    val hasCritSuccess: Boolean = numbers.any { it == 20 } && dices.any { it.isRegularDice }
    val hasCritFailure: Boolean = numbers.any { it == 1 } && dices.any { it.isRegularDice }
}

@Serializable
data class DiceGroup(
    val sides: Int,
    val count: Int,
) {
    val isRegularDice: Boolean = sides == 20
}
