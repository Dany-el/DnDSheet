package com.yablonskyi.dndsheet.ui.utils

import kotlin.math.roundToInt

data class CoinPurse(
    val gold: Int,
    val silver: Int,
    val copper: Int
)

object CurrencyHelper {
    fun parse(value: Double): CoinPurse {
        val totalCopper = (value * 100).roundToInt()

        val gold = totalCopper / 100
        val remainingAfterGold = totalCopper % 100
        val silver = remainingAfterGold / 10
        val copper = remainingAfterGold % 10

        return CoinPurse(gold, silver, copper)
    }

    fun modify(currentTotal: Double, amountToAdd: Double): Double {
        val newVal = currentTotal + amountToAdd
        return (newVal * 100.0).roundToInt() / 100.0
    }
}