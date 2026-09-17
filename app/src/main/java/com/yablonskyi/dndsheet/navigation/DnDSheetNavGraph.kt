package com.yablonskyi.dndsheet.navigation

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.yablonskyi.character.navigation.CharacterSettingsRoute
import com.yablonskyi.character.navigation.CharacterSheetRoute
import com.yablonskyi.character.navigation.DiceHistoryRoute
import com.yablonskyi.character.navigation.characterGraph
import com.yablonskyi.characterspells.navigation.CharacterSpellsRoute
import com.yablonskyi.characterspells.navigation.characterSpellsGraph
import com.yablonskyi.compendium.CompendiumNavActions
import com.yablonskyi.compendium.compendiumGraph
import com.yablonskyi.compendium.navigateToCompendiumClassCreate
import com.yablonskyi.compendium.navigateToCompendiumClassDetails
import com.yablonskyi.compendium.navigateToCompendiumClassUpdate
import com.yablonskyi.compendium.navigateToCompendiumClasses
import com.yablonskyi.compendium.navigateToCompendiumRaceCreate
import com.yablonskyi.compendium.navigateToCompendiumRaceDetails
import com.yablonskyi.compendium.navigateToCompendiumRaceUpdate
import com.yablonskyi.compendium.navigateToCompendiumRaces
import com.yablonskyi.compendium.navigateToCompendiumSpellUpdate
import com.yablonskyi.compendium.navigateToCompendiumSpellsLibrary
import com.yablonskyi.navigation.CharacterSheetsRoute
import com.yablonskyi.pdf.html.HtmlToPdfConverter
import com.yablonskyi.settings.AppSettingsViewModel
import com.yablonskyi.settings.settingsGraph
import com.yablonskyi.ui.animation.navigation.NavAnimation
import com.yablonskyi.ui.settings.ListView
import com.yablonskyi.wizard.navigation.CharacterCreationWizardRoute
import com.yablonskyi.wizard.navigation.wizardGraph
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun DnDSheetNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val activity = LocalActivity.current as ComponentActivity
    val htmlPrinter = remember { HtmlToPdfConverter() }
    val settingsViewModel =
        hiltViewModel<AppSettingsViewModel>(viewModelStoreOwner = activity)
    val listViewState: State<ListView> = remember {
        settingsViewModel.uiState.map { it.listView }
    }.collectAsStateWithLifecycle(initialValue = ListView.LIST)

    SharedTransitionLayout {
        val sharedTransitionScope = this
        NavHost(
            navController = navController,
            startDestination = CharacterSheetsRoute,
            enterTransition = { NavAnimation.FadeTransition.enterTransition },
            exitTransition = { NavAnimation.FadeTransition.exitTransition },
            modifier = modifier
        ) {
            characterGraph(
                sharedTransitionScope = sharedTransitionScope,
                listViewState = listViewState,
                onCreateCharacter = {
                    navController.navigate(CharacterCreationWizardRoute)
                },
                onOpenCharacter = { id ->
                    navController.navigate(CharacterSheetRoute(id = id))
                },
                onOpenCharacterSpells = { id ->
                    navController.navigate(CharacterSpellsRoute(characterId = id))
                },
                onOpenSettings = { id ->
                    navController.navigate(CharacterSettingsRoute(id = id))
                },
                onOpenDiceHistory = { characterId ->
                    navController.navigate(DiceHistoryRoute(characterId))
                },
                onToggleListView = {
                    val next = if (listViewState.value == ListView.LIST) {
                        ListView.GRID
                    } else {
                        ListView.LIST
                    }
                    settingsViewModel.updateListView(next)
                },
                onBack = { navController.popBackStack() },
                onPrintCharacterSheet = { html, jobName -> htmlPrinter.print(activity, html, jobName) },
            )
            compendiumGraph(
                actions = CompendiumNavActions(
                    openRaces = { navigateToCompendiumRaces(navController) },
                    openClasses = { navigateToCompendiumClasses(navController) },
                    openSpellsLibrary = { navigateToCompendiumSpellsLibrary(navController) },
                    openRaceDetails = { raceId -> navigateToCompendiumRaceDetails(navController, raceId) },
                    editRace = { raceId -> navigateToCompendiumRaceUpdate(navController, raceId) },
                    createRace = { navigateToCompendiumRaceCreate(navController) },
                    openClassDetails = { classId -> navigateToCompendiumClassDetails(navController, classId) },
                    editClass = { classId -> navigateToCompendiumClassUpdate(navController, classId) },
                    createClass = { navigateToCompendiumClassCreate(navController) },
                    openSpellDetails = { spellId -> navigateToCompendiumSpellUpdate(navController, spellId) },
                    createSpell = { navigateToCompendiumSpellUpdate(navController, 0L) },
                    back = { navController.popBackStack() }
                )
            )
            settingsGraph()
            wizardGraph(
                onCharacterCreated = { id ->
                    navController.navigate(CharacterSheetRoute(id = id)) {
                        popUpTo(CharacterSheetsRoute) { saveState = false }
                        launchSingleTop = true
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
            characterSpellsGraph(
                onBack = { navController.popBackStack() }
            )
        }
    }
}