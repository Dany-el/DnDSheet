package com.yablonskyi.ui.validation

import com.yablonskyi.model.character.Skill

object ClassValidators {

    val name = FieldValidator(
        ValidationPattern.Required,
        ValidationPattern.MaxLength(50)
    )

    val description = FieldValidator(
        ValidationPattern.Required
    )

    fun skillChoiceCount(min: Int = 0, max: Int = Skill.entries.size) = FieldValidator(
        ValidationPattern.Range(min, max)
    )
}