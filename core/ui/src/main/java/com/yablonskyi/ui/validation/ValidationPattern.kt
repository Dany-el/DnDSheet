package com.yablonskyi.ui.validation

import androidx.annotation.StringRes
import com.yablonskyi.ui.R

sealed class ValidationPattern<in T> : ValidationRule<T> {

    // ── String ────────────────────────────────────────────────────────────────
    data object Required : ValidationPattern<String>() {
        override fun validate(value: String): Int? =
            if (value.isBlank()) R.string.error_empty else null
    }

    data class Matches(
        val pattern: Regex,
        @param:StringRes val errorRes: Int
    ) : ValidationPattern<String>() {
        override fun validate(value: String): Int? =
            if (!pattern.matches(value)) errorRes else null
    }

    data class MaxLength(val max: Int) : ValidationPattern<String>() {
        override fun validate(value: String): Int? =
            if (value.length > max) R.string.error_max_length else null
    }

    data class MinLength(val min: Int) : ValidationPattern<String>() {
        override fun validate(value: String): Int? =
            if (value.length < min) R.string.error_min_length else null
    }

    // ── Int ───────────────────────────────────────────────────────────────────
    data class Range(val min: Int, val max: Int) : ValidationPattern<Int>() {
        override fun validate(value: Int): Int? =
            if (value !in min..max) R.string.error_range else null
    }

    data class Min(val min: Int) : ValidationPattern<Int>() {
        override fun validate(value: Int): Int? =
            if (value < min) R.string.error_min else null
    }

    data class Max(val max: Int) : ValidationPattern<Int>() {
        override fun validate(value: Int): Int? =
            if (value > max) R.string.error_max else null
    }
}