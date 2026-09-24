package com.yablonskyi.character.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.yablonskyi.character.presentation.dicehistory.DiceHistoryRouteContent
import com.yablonskyi.character.presentation.list.CharacterListRoute
import com.yablonskyi.character.presentation.settings.CharacterSettingsRouteContent
import com.yablonskyi.character.presentation.sheet.CharacterSheetRouteContent
import com.yablonskyi.character.presentation.notes.NoteEditorRouteContent
import com.yablonskyi.navigation.CharacterSheetsRoute
import com.yablonskyi.ui.animation.navigation.NavAnimation
import com.yablonskyi.ui.animation.navigation.roundDuringNavigation
import com.yablonskyi.ui.settings.ListView

fun NavGraphBuilder.characterGraph(
    sharedTransitionScope: SharedTransitionScope,
    listViewState: State<ListView>,
    onCreateCharacter: () -> Unit,
    onOpenCharacter: (Long) -> Unit,
    onOpenCharacterSpells: (Long) -> Unit,
    onOpenSettings: (Long) -> Unit,
    onOpenDiceHistory: (Long) -> Unit,
    onOpenNote: (Long, String?) -> Unit,
    onToggleListView: () -> Unit,
    onBack: () -> Unit,
    onPrintCharacterSheet: suspend (String, String) -> Result<Unit>,
    enterAnimation: AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards EnterTransition? = NavAnimation.SlideTransition.enterSlideTransition,
    exitAnimation: AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards ExitTransition? = NavAnimation.SlideTransition.exitSlideTransition,
) {
    composable<CharacterSheetsRoute> {
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
            onOpenNote,
            onBack,
        )
    }
    composable<CharacterSettingsRoute>(
        enterTransition = enterAnimation,
        exitTransition = exitAnimation,
    ) {
        Box(
            modifier = roundDuringNavigation(
                modifier = Modifier.fillMaxSize(),
            ),
        ) { CharacterSettingsRouteContent(onBack) }
    }
    composable<DiceHistoryRoute>(
        enterTransition = enterAnimation,
        exitTransition = exitAnimation,
    ) {
        Box(
            modifier = roundDuringNavigation(
                modifier = Modifier.fillMaxSize(),
            ),
        ) { DiceHistoryRouteContent(onBack) }
    }
    composable<NoteEditorRoute>(
        enterTransition = enterAnimation,
        exitTransition = exitAnimation,
    ) {
        NoteEditorRouteContent(onBack)
    }
}