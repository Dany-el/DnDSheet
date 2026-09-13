package com.yablonskyi.ui.spell

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.yablonskyi.model.character.MagicSchool
import com.yablonskyi.model.character.Spell
import com.yablonskyi.model.character.SpellCastTime
import com.yablonskyi.model.character.SpellDuration
import com.yablonskyi.model.character.SpellLevel

@Immutable
data class SpellLibraryState(
    val spells: List<SpellLibraryItem> = emptyList(),
    val groupedSpells: Map<SpellLevel, List<SpellLibraryItem>> =
        spells.groupBy { it.spell.level }.toSortedMap(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val isLearnMode: Boolean = false,
    val filterState: SpellFilterState = SpellFilterState(),
    val isSelectionMode: Boolean = false,
    val isAllSelected: Boolean = false,
    val selectedSpellIds: Set<Long> = emptySet(),
    val isFilterExpanded: Boolean = false,
    val showBottomSheet: Spell? = null,
    val showConfirmDeleteDialog: Boolean = false,
    val pendingDelete: SpellsDeleteRequest? = null,
    val pendingImport: List<Spell>? = null,
)

sealed interface SpellsDeleteRequest {
    data class Single(val spell: Spell) : SpellsDeleteRequest
    data object Selection : SpellsDeleteRequest
}

sealed interface SpellsIntent {
    // Navigation
    data object NavigateBack : SpellsIntent
    data object AddSpell : SpellsIntent
    data class EditSpell(val spellId: Long) : SpellsIntent

    // Search & filters
    data class SearchQueryChanged(val value: String) : SpellsIntent
    data object ToggleFiltersExpanded : SpellsIntent
    data class ToggleLevelFilter(val level: SpellLevel) : SpellsIntent
    data class ToggleSchoolFilter(val school: MagicSchool) : SpellsIntent
    data class ToggleDurationFilter(val duration: SpellDuration) : SpellsIntent
    data class ToggleCastTimeFilter(val castTime: SpellCastTime) : SpellsIntent
    data object ToggleRitual : SpellsIntent
    data object ToggleConcentration : SpellsIntent
    data object ClearAllFilters : SpellsIntent

    // Details bottom sheet
    data class ShowDetails(val spell: Spell) : SpellsIntent
    data object DismissDetails : SpellsIntent

    // Learn / unlearn (character spell library only)
    data class ToggleSpell(val spell: Spell) : SpellsIntent

    // Selection
    data class ToggleSelection(val spell: Spell) : SpellsIntent
    data object ToggleSelectAll : SpellsIntent
    data object EnterSelectionMode : SpellsIntent
    data object ClearSelection : SpellsIntent

    // Delete
    data class Delete(val spell: Spell) : SpellsIntent
    data object DeleteSelected : SpellsIntent
    data object ConfirmDelete : SpellsIntent
    data object DismissDelete : SpellsIntent

    // Import / Export
    data class ShareRequested(val spell: Spell) : SpellsIntent
    data object ShareFailed : SpellsIntent
    data object ExportAllSelected : SpellsIntent
    data object RequestFilePicker : SpellsIntent
    data class ExportCompleted(val success: Boolean) : SpellsIntent
    data class ImportRequested(val json: String) : SpellsIntent
    data object ConfirmImport : SpellsIntent
    data object DismissImport : SpellsIntent
}

sealed interface SpellsEffect {
    data object NavigateBack : SpellsEffect
    data object NavigateToAddSpell : SpellsEffect
    data class NavigateToEdit(val spellId: Long) : SpellsEffect
    data class ShowSnackbar(@param:StringRes val messageRes: Int) : SpellsEffect
}