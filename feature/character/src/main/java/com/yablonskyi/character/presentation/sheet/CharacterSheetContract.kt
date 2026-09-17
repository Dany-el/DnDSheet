package com.yablonskyi.character.presentation.sheet

import androidx.compose.runtime.Immutable
import com.yablonskyi.character.presentation.common.*
import com.yablonskyi.character.presentation.sheet.model.*
import com.yablonskyi.character.presentation.sheet.mapper.*
import com.yablonskyi.domain.character.CharacterChange
import com.yablonskyi.model.character.Character
import com.yablonskyi.model.character.Attack
import com.yablonskyi.model.character.Spell

@Immutable
data class CharacterSheetState(
    val character: Character? = null,
    val status: CharacterLoadStatus = CharacterLoadStatus.LOADING,
    val allSpells: List<Spell> = emptyList(),
    val rawAttacks: List<Attack> = emptyList(),
    val currentFilter: SpellFilter = SpellFilter.All,
    val leftSelectedTab: CharacterTab = CharacterTab.ABILITIES,
    val rightSelectedTab: CharacterTab = CharacterTab.SPELLS,
    val lessDetails: Boolean = false,
    val editor: CharacterSheetEditor? = null,
    val pendingWrites: Int = 0,
    val errors: Set<CharacterUiError> = emptySet(),
) {
    val spells get() = filterCharacterSpells(allSpells, currentFilter)
    val availableFilters get() = availableSpellFilters(allSpells)
    val attacks get() = mapAttacks(character, rawAttacks)
}

sealed interface CharacterSheetIntent {
    data class Change(val change: CharacterChange) : CharacterSheetIntent
    data class AttackSaved(val attack: Attack) : CharacterSheetIntent
    data class AttackDeleted(val attack: Attack) : CharacterSheetIntent
    data class FilterChanged(val filter: SpellFilter) : CharacterSheetIntent
    data class LeftTabSelected(val tab: CharacterTab) : CharacterSheetIntent
    data class RightTabSelected(val tab: CharacterTab) : CharacterSheetIntent
    data class EditorChanged(val editor: CharacterSheetEditor?) : CharacterSheetIntent
    data object ToggleDetails : CharacterSheetIntent
    data object OpenSettings : CharacterSheetIntent
    data object ManageSpells : CharacterSheetIntent
    data object OpenDiceHistory : CharacterSheetIntent
    data object BackClicked : CharacterSheetIntent
    data object Retry : CharacterSheetIntent
    data object DismissError : CharacterSheetIntent
}

sealed interface CharacterSheetEffect {
    data class OpenSettings(val id: Long) : CharacterSheetEffect
    data class ManageSpells(val id: Long) : CharacterSheetEffect
    data class OpenDiceHistory(val characterId: Long) : CharacterSheetEffect
    data object Back : CharacterSheetEffect
}

internal sealed interface CharacterSheetMutation {
    data class CharacterLoaded(val character: Character?) : CharacterSheetMutation
    data class SpellsLoaded(val spells: List<Spell>) : CharacterSheetMutation
    data class AttacksLoaded(val attacks: List<Attack>) : CharacterSheetMutation
    data class Filter(val filter: SpellFilter) : CharacterSheetMutation
    data class LeftTab(val tab: CharacterTab) : CharacterSheetMutation
    data class RightTab(val tab: CharacterTab) : CharacterSheetMutation
    data class Editor(val editor: CharacterSheetEditor?) : CharacterSheetMutation
    data object ToggleDetails : CharacterSheetMutation
    data class WriteCount(val delta: Int) : CharacterSheetMutation
    data class Failed(val error: CharacterUiError) : CharacterSheetMutation
    data object ClearErrors : CharacterSheetMutation
}
