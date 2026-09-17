package com.yablonskyi.character.navigation

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.State
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.yablonskyi.character.presentation.dicehistory.DiceHistoryRouteContent
import com.yablonskyi.character.presentation.list.CharacterListRoute
import com.yablonskyi.character.presentation.settings.CharacterSettingsRouteContent
import com.yablonskyi.character.presentation.sheet.CharacterSheetRouteContent
import com.yablonskyi.navigation.CharacterSheetsRoute
import com.yablonskyi.ui.animation.navigation.NavAnimation
import com.yablonskyi.ui.settings.ListView

fun NavGraphBuilder.characterGraph(
    sharedTransitionScope: SharedTransitionScope,
    listViewState: State<ListView>,
    onCreateCharacter: () -> Unit,
    onOpenCharacter: (Long) -> Unit,
    onOpenCharacterSpells: (Long) -> Unit,
    onOpenSettings: (Long) -> Unit,
    onOpenDiceHistory: (Long) -> Unit,
    onToggleListView: () -> Unit,
    onBack: () -> Unit,
    onPrintCharacterSheet: suspend (String, String) -> Result<Unit>,
) {
    composable<CharacterSheetsRoute>{
        CharacterListRoute(
            sharedTransitionScope, this, listViewState.value, onCreateCharacter,
            onOpenCharacter, onToggleListView, onPrintCharacterSheet
        )
    }
    composable<CharacterSheetRoute> {
        CharacterSheetRouteContent(
            sharedTransitionScope,
            this,
            onOpenCharacterSpells,
            onOpenSettings,
            onOpenDiceHistory,
            onBack,
        )
    }
    composable<CharacterSettingsRoute>(
        enterTransition = NavAnimation.RightSideSlideAnimation.enterSlideTransition,
        exitTransition = NavAnimation.RightSideSlideAnimation.exitSlideTransition
    ) { CharacterSettingsRouteContent(onBack) }
    composable<DiceHistoryRoute>(
        enterTransition = NavAnimation.RightSideSlideAnimation.enterSlideTransition,
        exitTransition = NavAnimation.RightSideSlideAnimation.exitSlideTransition
    ) { DiceHistoryRouteContent(onBack) }
}