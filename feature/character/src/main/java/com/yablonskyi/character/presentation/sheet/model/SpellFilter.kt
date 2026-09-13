package com.yablonskyi.character.presentation.sheet.model

import com.yablonskyi.ui.R

sealed class SpellFilter {
    data object All : SpellFilter()
    data class ByLevel(val level: Int) : SpellFilter()
    data object Concentration : SpellFilter()
    data object Ritual : SpellFilter()

    fun getLabelResId(): Int = when (this) {
        All -> R.string.filter_all
        is ByLevel -> when (level) {
            0 -> R.string.level_cantrip
            1 -> R.string.level_1
            2 -> R.string.level_2
            3 -> R.string.level_3
            4 -> R.string.level_4
            5 -> R.string.level_5
            6 -> R.string.level_6
            7 -> R.string.level_7
            8 -> R.string.level_8
            9 -> R.string.level_9
            else -> R.string.level_cantrip
        }
        Concentration -> R.string.concentration
        Ritual -> R.string.ritual
    }
}