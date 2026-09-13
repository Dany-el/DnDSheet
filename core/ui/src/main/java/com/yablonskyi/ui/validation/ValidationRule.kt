package com.yablonskyi.ui.validation

import androidx.annotation.StringRes

fun interface ValidationRule<in T> {
    @StringRes
    fun validate(value: T): Int?
}
