package com.yablonskyi.character.navigation

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.State
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.yablonskyi.navigation.CharacterSheetsRoute
import com.yablonskyi.ui.settings.ListView
import com.yablonskyi.character.presentation.list.CharacterListRoute
import com.yablonskyi.character.presentation.sheet.CharacterSheetRouteContent
import com.yablonskyi.character.presentation.settings.CharacterSettingsRouteContent

fun NavGraphBuilder.characterGraph(
    sharedTransitionScope: SharedTransitionScope,
    listViewState: State<ListView>,
    onCreateCharacter: () -> Unit,
    onOpenCharacter: (Long) -> Unit,
    onOpenCharacterSpells: (Long) -> Unit,
    onOpenSettings: (Long) -> Unit,
    onToggleListView: () -> Unit,
    onBack: () -> Unit,
    onPrintCharacterSheet: suspend (String, String) -> Result<Unit>,
) {
    composable<CharacterSheetsRoute> {
        CharacterListRoute(sharedTransitionScope, this, listViewState.value, onCreateCharacter,
            onOpenCharacter, onToggleListView, onPrintCharacterSheet)
    }
    composable<CharacterSheetRoute> {
        CharacterSheetRouteContent(sharedTransitionScope, this, onOpenCharacterSpells, onOpenSettings, onBack)
    }
    composable<CharacterSettingsRoute> { CharacterSettingsRouteContent(onBack) }
}
