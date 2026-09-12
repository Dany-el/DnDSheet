package com.yablonskyi.model.rulebook

import androidx.annotation.StringRes
import com.yablonskyi.model.R

enum class RaceSize(@param:StringRes val resId: Int) {
    TINY(R.string.size_tiny),
    SMALL(R.string.size_small),
    MEDIUM(R.string.size_medium),
    LARGE(R.string.size_large),
    HUGE(R.string.size_huge),
    GARGANTUAN(R.string.size_gargantuan);

    companion object {
        fun fromString(value: String): RaceSize =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: MEDIUM
    }
}