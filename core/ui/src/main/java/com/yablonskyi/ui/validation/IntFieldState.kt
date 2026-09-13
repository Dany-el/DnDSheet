package com.yablonskyi.ui.validation

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable

@Immutable
data class IntFieldState(
    val value: Int = 0,
    @param:StringRes val error: Int? = null,
    val isFocusedBefore: Boolean = false,
    val validator: FieldValidator<Int> = FieldValidator(),
    val minValue: Int = 0,
    val maxValue: Int = Int.MAX_VALUE,
) {
    fun onValueChanged(newValue: Int): IntFieldState = copy(
        value = newValue,
        error = validator.validateIfDirty(newValue, error)
    )

    fun onFocusGained(): IntFieldState = copy(isFocusedBefore = true)

    fun onFocusLost(): IntFieldState =
        if (isFocusedBefore) validate() else this

    fun validate(): IntFieldState = copy(error = validator.validate(value))

    val isValid: Boolean get() = validator.isValid(value)
}