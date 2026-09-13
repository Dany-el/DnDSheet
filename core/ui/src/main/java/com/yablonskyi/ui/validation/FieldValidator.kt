package com.yablonskyi.ui.validation

import androidx.annotation.StringRes

class FieldValidator<T>(private vararg val patterns: ValidationRule<T>) {

    @StringRes
    fun validate(value: T): Int? =
        patterns.firstNotNullOfOrNull { it.validate(value) }

    fun isValid(value: T): Boolean = validate(value) == null

    // Validates only if a prior error exists — for real-time correction feedback
    fun validateIfDirty(value: T, currentError: Int?): Int? =
        if (currentError != null) validate(value) else null
}
