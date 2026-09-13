package com.yablonskyi.character.presentation.list

import com.yablonskyi.model.character.Character
import com.yablonskyi.model.character.CharacterSheet
import com.yablonskyi.character.platform.print.CharacterPrintState
import com.yablonskyi.character.platform.print.CharacterPrintEffect

data class CharacterListState(
    val allCharacters: List<Character> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val selectedIds: Set<Long> = emptySet(),
    val isSelectionMode: Boolean = false,
    val operation: CharacterListOperation? = null,
    val pendingImport: List<CharacterSheet>? = null,
    val printState: CharacterPrintState = CharacterPrintState.Idle,
    val error: CharacterListError? = null,
) {
    val characters: List<Character> get() = allCharacters.filter { it.name.contains(searchQuery, ignoreCase = true) }
    val isAllSelected: Boolean get() = characters.isNotEmpty() && characters.all { it.id in selectedIds }
    val isPrinting: Boolean get() = printState != CharacterPrintState.Idle
}

enum class CharacterListOperation { IMPORT_PICKER, IMPORT, EXPORT_PICKER, EXPORT, DELETE }
enum class CharacterListError { LOAD, IMPORT, EMPTY_IMPORT, EXPORT, DELETE, EMPTY_SELECTION }

sealed interface CharacterListIntent {
    data class SearchChanged(val query: String) : CharacterListIntent
    data class SelectionToggled(val id: Long) : CharacterListIntent
    data object SelectAllClicked : CharacterListIntent
    data object ClearSelection : CharacterListIntent
    data object DeleteSelectedConfirmed : CharacterListIntent
    data class DeleteConfirmed(val id: Long) : CharacterListIntent
    data object ImportClicked : CharacterListIntent
    data class ImportDocumentSelected(val uri: String?) : CharacterListIntent
    data object ImportConfirmed : CharacterListIntent
    data object ImportDismissed : CharacterListIntent
    data object ExportClicked : CharacterListIntent
    data class ExportDocumentSelected(val uri: String?) : CharacterListIntent
    data class PrintClicked(val id: Long, val language: String) : CharacterListIntent
    data class PrintFinished(val requestId: Long, val result: Result<Unit>) : CharacterListIntent
    data class PrintCancelled(val requestId: Long) : CharacterListIntent
    data class CharacterClicked(val id: Long) : CharacterListIntent
    data object CreateClicked : CharacterListIntent
    data object ToggleListView : CharacterListIntent
    data object Retry : CharacterListIntent
    data object DismissError : CharacterListIntent
}

sealed interface CharacterListEffect {
    data class OpenCharacter(val id: Long) : CharacterListEffect
    data object CreateCharacter : CharacterListEffect
    data object ToggleListView : CharacterListEffect
    data object LaunchImport : CharacterListEffect
    data object LaunchExport : CharacterListEffect
    data object ImportSucceeded : CharacterListEffect
    data object ExportSucceeded : CharacterListEffect
    data class Print(val effect: CharacterPrintEffect) : CharacterListEffect
}

internal sealed interface CharacterListMutation {
    data class Loaded(val characters: List<Character>) : CharacterListMutation
    data class Search(val query: String) : CharacterListMutation
    data class ToggleSelection(val id: Long) : CharacterListMutation
    data object SelectAll : CharacterListMutation
    data object ClearSelection : CharacterListMutation
    data class Operation(val value: CharacterListOperation?) : CharacterListMutation
    data class ImportRead(val sheets: List<CharacterSheet>?) : CharacterListMutation
    data class Failed(val error: CharacterListError?) : CharacterListMutation
    data class Print(val state: CharacterPrintState) : CharacterListMutation
}
