package com.yablonskyi.character.presentation.settings

import com.yablonskyi.character.presentation.common.*
import com.yablonskyi.domain.character.*

internal fun reduceCharacterSettings(state: CharacterSettingsState, mutation: CharacterSettingsMutation): CharacterSettingsState = when (mutation) {
    is CharacterSettingsMutation.Loaded -> state.copy(
        character = mutation.character?.let { character -> state.drafts.entries.fold(character) { current, (field, value) ->
            applyCharacterChange(current, CharacterChange.Text(field, value))
        } },
        status = if (mutation.character == null) CharacterLoadStatus.NOT_FOUND else CharacterLoadStatus.CONTENT,
        errors = state.errors - CharacterUiError.LOAD,
    )
    is CharacterSettingsMutation.Draft -> state.copy(drafts = state.drafts + (mutation.change.field to mutation.change.value),
        character = state.character?.let { applyCharacterChange(it, mutation.change) })
    is CharacterSettingsMutation.DiscardDraft -> if (state.drafts[mutation.change.field] == mutation.change.value)
        state.copy(drafts = state.drafts - mutation.change.field) else state
    is CharacterSettingsMutation.WriteCount -> state.copy(pendingWrites = (state.pendingWrites + mutation.delta).coerceAtLeast(0))
    is CharacterSettingsMutation.PickingImage -> state.copy(isPickingImage = mutation.picking)
    is CharacterSettingsMutation.SavingImage -> state.copy(isSavingImage = mutation.saving)
    is CharacterSettingsMutation.Failed -> state.copy(errors = state.errors + mutation.error,
        status = if (mutation.error == CharacterUiError.LOAD && state.character == null) CharacterLoadStatus.ERROR else state.status)
    CharacterSettingsMutation.ClearErrors -> state.copy(errors = emptySet())
}
