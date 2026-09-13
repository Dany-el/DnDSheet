package com.yablonskyi.ui.validation

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable

@Immutable
data class FieldState(
    val text: String = "",
    @param:StringRes val error: Int? = null,
    val isFocusedBefore: Boolean = false,
    val validator: FieldValidator<String> = FieldValidator(),
) {
    fun onTextChanged(newText: String): FieldState = copy(
        text = newText,
        error = validator.validateIfDirty(newText, error)
    )

    fun onFocusGained(): FieldState = copy(isFocusedBefore = true)

    fun onFocusLost(): FieldState =
        if (isFocusedBefore) copy(error = validator.validate(text)) else this

    fun validate(): FieldState = copy(error = validator.validate(text))

    val isValid: Boolean get() = validator.isValid(text)
}