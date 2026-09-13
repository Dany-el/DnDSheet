package com.yablonskyi.ui.validation

import com.yablonskyi.ui.R

object SpellValidators {

    private val DAMAGE_DICE_REGEX =
        Regex("""^([1-9]\d*)?[dDкК](4|6|8|10|12|20|100)$""")

    private val DAMAGE_DICE_TYPING_REGEX =
        Regex("""^([1-9]\d*)?([dDкК](1(00?|2)?|20?|4|6|8)?)?$""")

    val name = FieldValidator(
        ValidationPattern.Required,
        ValidationPattern.MaxLength(100)
    )

    val description = FieldValidator(
        ValidationPattern.Required
    )

    val material = FieldValidator(
        ValidationPattern.Required
    )

    val damageDice = FieldValidator(
        ValidationPattern.Required,
        ValidationPattern.Matches(DAMAGE_DICE_REGEX, R.string.error_invalid_dice)
    )

    // Used on every keystroke to allow incremental input like "1d"
    val damageDiceTyping = FieldValidator(
        ValidationPattern.Matches(DAMAGE_DICE_TYPING_REGEX, R.string.error_invalid_dice)
    )

    val higherLevels = FieldValidator(
        ValidationPattern.MaxLength(500)
    )
}