package com.yablonskyi.character.presentation.list

internal fun reduceCharacterList(state: CharacterListState, mutation: CharacterListMutation): CharacterListState = when (mutation) {
    is CharacterListMutation.Loaded -> state.copy(
        allCharacters = mutation.characters,
        isLoading = false,
        selectedIds = state.selectedIds.intersect(mutation.characters.map { it.id }.toSet()),
        error = state.error.takeUnless { it == CharacterListError.LOAD },
    )
    is CharacterListMutation.Search -> state.copy(searchQuery = mutation.query)
    is CharacterListMutation.ToggleSelection -> if (state.allCharacters.none { it.id == mutation.id }) state else state.copy(
        isSelectionMode = true,
        selectedIds = if (mutation.id in state.selectedIds) state.selectedIds - mutation.id else state.selectedIds + mutation.id,
    )
    CharacterListMutation.SelectAll -> {
        val visibleIds = state.characters.map { it.id }.toSet()
        state.copy(selectedIds = if (state.isAllSelected) state.selectedIds - visibleIds else state.selectedIds + visibleIds)
    }
    CharacterListMutation.ClearSelection -> state.copy(selectedIds = emptySet(), isSelectionMode = false)
    is CharacterListMutation.Operation -> state.copy(operation = mutation.value, error = null)
    is CharacterListMutation.ImportRead -> state.copy(pendingImport = mutation.sheets, operation = null)
    is CharacterListMutation.Failed -> state.copy(error = mutation.error, operation = null, isLoading = false)
    is CharacterListMutation.Print -> state.copy(printState = mutation.state)
}
