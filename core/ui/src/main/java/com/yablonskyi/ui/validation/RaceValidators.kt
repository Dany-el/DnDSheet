package com.yablonskyi.ui.validation

import com.yablonskyi.ui.R

object RaceValidators {

    val name = FieldValidator(
        ValidationPattern.Required,
        ValidationPattern.MaxLength(50)
    )

    val size = FieldValidator(
        ValidationPattern.Required
    )

    val speed = FieldValidator(
        ValidationPattern.Range(0, 100)
    )

    val description = FieldValidator(
        ValidationPattern.Required
    )

    private val TRAITS_REGEX = Regex("""^([^,]+,\s*)*([^,]+,?\s*)?$""")

    val traits = FieldValidator(
        ValidationPattern.Matches(TRAITS_REGEX, R.string.error_traits)
    )
}