package com.yablonskyi.character.presentation.sheet.model

import com.yablonskyi.model.character.Ability

sealed interface CharacterSheetEditor {
    data class EditAbility(val ability: Ability) : CharacterSheetEditor
    data object EditHealth : CharacterSheetEditor
    data class EditAttack(val attackId: Long = 0) : CharacterSheetEditor
    data class ViewSpell(val spellId: Long) : CharacterSheetEditor
}
