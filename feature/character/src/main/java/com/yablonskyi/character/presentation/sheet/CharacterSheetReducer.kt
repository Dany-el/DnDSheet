package com.yablonskyi.character.presentation.sheet

import com.yablonskyi.character.presentation.common.*

internal fun reduceCharacterSheet(state: CharacterSheetState, mutation: CharacterSheetMutation): CharacterSheetState = when (mutation) {
    is CharacterSheetMutation.CharacterLoaded -> state.copy(character = mutation.character,
        status = if (mutation.character == null) CharacterLoadStatus.NOT_FOUND else CharacterLoadStatus.CONTENT,
        errors = state.errors - CharacterUiError.LOAD)
    is CharacterSheetMutation.SpellsLoaded -> state.copy(allSpells = mutation.spells, errors = state.errors - CharacterUiError.SPELLS)
    is CharacterSheetMutation.AttacksLoaded -> state.copy(rawAttacks = mutation.attacks, errors = state.errors - CharacterUiError.ATTACKS)
    is CharacterSheetMutation.Filter -> state.copy(currentFilter = mutation.filter)
    is CharacterSheetMutation.LeftTab -> state.copy(leftSelectedTab = mutation.tab,
        rightSelectedTab = if (mutation.tab == state.rightSelectedTab) state.leftSelectedTab else state.rightSelectedTab)
    is CharacterSheetMutation.RightTab -> state.copy(rightSelectedTab = mutation.tab,
        leftSelectedTab = if (mutation.tab == state.leftSelectedTab) state.rightSelectedTab else state.leftSelectedTab)
    is CharacterSheetMutation.Editor -> state.copy(editor = mutation.editor)
    CharacterSheetMutation.ToggleDetails -> state.copy(lessDetails = !state.lessDetails)
    is CharacterSheetMutation.WriteCount -> state.copy(pendingWrites = (state.pendingWrites + mutation.delta).coerceAtLeast(0))
    is CharacterSheetMutation.Failed -> state.copy(errors = state.errors + mutation.error,
        status = if (mutation.error == CharacterUiError.LOAD && state.character == null) CharacterLoadStatus.ERROR else state.status)
    CharacterSheetMutation.ClearErrors -> state.copy(errors = emptySet())
}
