package com.yablonskyi.character.presentation.common.validation

import com.yablonskyi.ui.R
import com.yablonskyi.ui.validation.*

object CharacterFormValidators {
    fun number(value: Int = 0, min: Int = 0, max: Int = Int.MAX_VALUE) = IntFieldState(
        value = value, minValue = min, maxValue = max,
        validator = FieldValidator(ValidationRule { if (it in min..max) null else R.string.form_invalid_number }),
    )
    val attackName = FieldValidator(ValidationRule<String> {
        if (it.isNotBlank() && it.length <= 50) null else R.string.form_invalid_name
    })
}
