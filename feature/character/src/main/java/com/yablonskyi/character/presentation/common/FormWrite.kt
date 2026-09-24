package com.yablonskyi.character.presentation.common

import com.yablonskyi.domain.character.CharacterChange
import com.yablonskyi.model.character.Character

/** Identifies a form edit so a late write result cannot discard a newer draft. */
data class FormWrite(val session: String, val revision: Long)
data class FormWriteResult(val write: FormWrite, val change: CharacterChange, val accepted: Boolean, val persisted: Character?)
