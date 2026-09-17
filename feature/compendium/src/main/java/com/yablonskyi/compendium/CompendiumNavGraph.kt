package com.yablonskyi.compendium

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalResources
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.yablonskyi.compendium.classes.ui.ClassCreateScreen
import com.yablonskyi.compendium.classes.ui.ClassDetailsScreen
import com.yablonskyi.compendium.classes.ui.ClassesScreen
import com.yablonskyi.compendium.classes.viewmodel.CharacterClassesViewModel
import com.yablonskyi.compendium.classes.viewmodel.ClassDetailsEffect
import com.yablonskyi.compendium.classes.viewmodel.ClassDetailsIntent
import com.yablonskyi.compendium.classes.viewmodel.ClassDetailsViewModel
import com.yablonskyi.compendium.classes.viewmodel.ClassFormEffect
import com.yablonskyi.compendium.classes.viewmodel.ClassFormViewModel
import com.yablonskyi.compendium.classes.viewmodel.ClassesEffect
import com.yablonskyi.compendium.classes.viewmodel.ClassesIntent
import com.yablonskyi.compendium.races.ui.RaceCreateScreen
import com.yablonskyi.compendium.races.ui.RaceDetailsScreen
import com.yablonskyi.compendium.races.ui.RacesScreen
import com.yablonskyi.compendium.races.viewmodel.RaceDetailsEffect
import com.yablonskyi.compendium.races.viewmodel.RaceDetailsIntent
import com.yablonskyi.compendium.races.viewmodel.RaceDetailsViewModel
import com.yablonskyi.compendium.races.viewmodel.RaceFormEffect
import com.yablonskyi.compendium.races.viewmodel.RaceFormViewModel
import com.yablonskyi.compendium.races.viewmodel.RacesEffect
import com.yablonskyi.compendium.races.viewmodel.RacesIntent
import com.yablonskyi.compendium.races.viewmodel.RacesViewModel
import com.yablonskyi.compendium.spells.ui.SpellCreateScreen
import com.yablonskyi.compendium.spells.viewmodel.CompendiumSpellsLibraryViewModel
import com.yablonskyi.compendium.spells.viewmodel.SpellFormEffect
import com.yablonskyi.compendium.spells.viewmodel.SpellFormViewModel
import com.yablonskyi.navigation.CompendiumRoute
import com.yablonskyi.ui.animation.navigation.NavAnimation
import com.yablonskyi.ui.spell.SpellLibraryScreen
import com.yablonskyi.ui.spell.SpellsEffect
import com.yablonskyi.ui.spell.SpellsIntent
import com.yablonskyi.ui.utils.LoadingDialog
import com.yablonskyi.ui.utils.rememberFileOperationHandler

fun NavGraphBuilder.compendiumGraph(
    actions: CompendiumNavActions,
) {
    composable<CompendiumRoute> {
        val viewModel = hiltViewModel<CompendiumViewModel>()

        val uiState by viewModel.compendiumState.collectAsStateWithLifecycle()

        CompendiumScreen(
            racesCount = uiState.racesCount,
            classesCount = uiState.classesCount,
            spellsCount = uiState.spellsCount,
            onRacesClick = actions.openRaces,
            onClassesClick = actions.openClasses,
            onSpellsClick = actions.openSpellsLibrary,
        )
    }

    composable<CompendiumRacesRoute> {
        val viewModel = hiltViewModel<RacesViewModel>()

        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val snackbarHostState = remember { SnackbarHostState() }
        val resources = LocalResources.current

        rememberFileOperationHandler(
            effects = viewModel.fileEffect,
            onExportCompleted = { success -> viewModel.onIntent(RacesIntent.ExportCompleted(success)) },
            onImportReady = { json -> viewModel.onIntent(RacesIntent.ImportRequested(json)) },
            onShareFailed = { viewModel.onIntent(RacesIntent.ShareFailed) }
        )

        val lifecycleOwner = LocalLifecycleOwner.current
        LaunchedEffect(lifecycleOwner) {
            viewModel.effect
                .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collect { effect ->
                    when (effect) {
                        RacesEffect.NavigateBack -> actions.back()
                        RacesEffect.NavigateToCreate -> actions.createRace()
                        is RacesEffect.NavigateToEdit -> actions.editRace(effect.raceId)
                        is RacesEffect.NavigateToDetails -> actions.openRaceDetails(effect.raceId)
                        is RacesEffect.ShowSnackbar -> snackbarHostState.showSnackbar(
                            message = resources.getString(effect.messageRes)
                        )
                    }
                }
        }

        RacesScreen(
            uiState = uiState,
            snackbarHostState = snackbarHostState,
            onIntent = viewModel::onIntent
        )
    }

    composable<CompendiumRaceDetailsRoute>(
        enterTransition = NavAnimation.RightSideSlideAnimation.enterSlideTransition,
        exitTransition = NavAnimation.RightSideSlideAnimation.exitSlideTransition
    ) {
        val viewModel = hiltViewModel<RaceDetailsViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val snackbarHostState = remember { SnackbarHostState() }
        val resources = LocalResources.current

        rememberFileOperationHandler(
            effects = viewModel.fileEffect,
            onExportCompleted = {},
            onImportReady = {},
            onShareFailed = { viewModel.onIntent(RaceDetailsIntent.ShareFailed) }
        )

        val lifecycleOwner = LocalLifecycleOwner.current
        LaunchedEffect(lifecycleOwner) {
            viewModel.effect
                .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collect { effect ->
                    when (effect) {
                        RaceDetailsEffect.NavigateBack -> actions.back()
                        is RaceDetailsEffect.NavigateToEdit -> actions.editRace(effect.raceId)
                        is RaceDetailsEffect.ShowSnackbar -> snackbarHostState.showSnackbar(
                            message = resources.getString(effect.messageRes)
                        )
                    }
                }
        }

        if (uiState.isLoading) {
            LoadingDialog()
        } else {
            RaceDetailsScreen(
                uiState = uiState,
                snackbarHostState = snackbarHostState,
                onIntent = viewModel::onIntent
            )
        }
    }

    composable<CompendiumRaceCreateRoute>(
        enterTransition = NavAnimation.RightSideSlideAnimation.enterSlideTransition,
        exitTransition = NavAnimation.RightSideSlideAnimation.exitSlideTransition
    ) {
        val viewModel = hiltViewModel<RaceFormViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        val lifecycleOwner = LocalLifecycleOwner.current
        LaunchedEffect(lifecycleOwner) {
            viewModel.effect
                .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collect { effect ->
                    when (effect) {
                        RaceFormEffect.NavigateBack -> actions.back()
                    }
                }
        }

        RaceCreateScreen(
            uiState = uiState,
            onIntent = viewModel::onIntent,
        )
    }

    composable<CompendiumRaceUpdateRoute>(
        enterTransition = NavAnimation.RightSideSlideAnimation.enterSlideTransition,
        exitTransition = NavAnimation.RightSideSlideAnimation.exitSlideTransition
    ) {
        val viewModel = hiltViewModel<RaceFormViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        val lifecycleOwner = LocalLifecycleOwner.current
        LaunchedEffect(lifecycleOwner) {
            viewModel.effect
                .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collect { effect ->
                    when (effect) {
                        RaceFormEffect.NavigateBack -> actions.back()
                    }
                }
        }

        if (uiState.isLoading) {
            LoadingDialog()
        } else {
            RaceCreateScreen(
                uiState = uiState,
                onIntent = viewModel::onIntent,
            )
        }
    }

    composable<CompendiumClassesRoute> {
        val viewModel = hiltViewModel<CharacterClassesViewModel>()

        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val snackbarHostState = remember { SnackbarHostState() }
        val resources = LocalResources.current

        rememberFileOperationHandler(
            effects = viewModel.fileEffect,
            onExportCompleted = { success ->
                viewModel.onIntent(ClassesIntent.ExportCompleted(success))
            },
            onImportReady = { json -> viewModel.onIntent(ClassesIntent.ImportRequested(json)) },
            onShareFailed = { viewModel.onIntent(ClassesIntent.ShareFailed) }
        )

        val lifecycleOwner = LocalLifecycleOwner.current
        LaunchedEffect(lifecycleOwner) {
            viewModel.effect
                .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collect { effect ->
                    when (effect) {
                        ClassesEffect.NavigateBack -> actions.back()
                        ClassesEffect.NavigateToCreate -> actions.createClass()
                        is ClassesEffect.NavigateToEdit -> actions.editClass(effect.classId)
                        is ClassesEffect.NavigateToDetails -> actions.openClassDetails(effect.classId)
                        is ClassesEffect.ShowSnackbar -> snackbarHostState.showSnackbar(
                            message = resources.getString(effect.messageRes)
                        )
                    }
                }
        }

        ClassesScreen(
            uiState = uiState,
            snackbarHostState = snackbarHostState,
            onIntent = viewModel::onIntent
        )
    }

    composable<CompendiumClassDetailsRoute>(
        enterTransition = NavAnimation.RightSideSlideAnimation.enterSlideTransition,
        exitTransition = NavAnimation.RightSideSlideAnimation.exitSlideTransition
    ) {
        val viewModel = hiltViewModel<ClassDetailsViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val snackbarHostState = remember { SnackbarHostState() }
        val resources = LocalResources.current

        rememberFileOperationHandler(
            effects = viewModel.fileEffect,
            onExportCompleted = {},
            onImportReady = {},
            onShareFailed = { viewModel.onIntent(ClassDetailsIntent.ShareFailed) }
        )

        val lifecycleOwner = LocalLifecycleOwner.current
        LaunchedEffect(lifecycleOwner) {
            viewModel.effect
                .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collect { effect ->
                    when (effect) {
                        ClassDetailsEffect.NavigateBack -> actions.back()
                        is ClassDetailsEffect.NavigateToEdit -> actions.editClass(effect.classId)
                        is ClassDetailsEffect.ShowSnackbar -> snackbarHostState.showSnackbar(
                            message = resources.getString(effect.messageRes)
                        )
                    }
                }
        }

        if (uiState.isLoading) {
            LoadingDialog()
        } else {
            ClassDetailsScreen(
                uiState = uiState,
                snackbarHostState = snackbarHostState,
                onIntent = viewModel::onIntent
            )
        }
    }

    composable<CompendiumClassCreateRoute>(
        enterTransition = NavAnimation.RightSideSlideAnimation.enterSlideTransition,
        exitTransition = NavAnimation.RightSideSlideAnimation.exitSlideTransition
    ) {
        val viewModel = hiltViewModel<ClassFormViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        val lifecycleOwner = LocalLifecycleOwner.current
        LaunchedEffect(lifecycleOwner) {
            viewModel.effect
                .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collect { effect ->
                    when (effect) {
                        is ClassFormEffect.NavigateBack -> actions.back()
                    }
                }
        }

        ClassCreateScreen(
            uiState = uiState,
            onIntent = viewModel::onIntent,
        )
    }

    composable<CompendiumClassUpdateRoute>(
        enterTransition = NavAnimation.RightSideSlideAnimation.enterSlideTransition,
        exitTransition = NavAnimation.RightSideSlideAnimation.exitSlideTransition
    ) {
        val viewModel = hiltViewModel<ClassFormViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        val lifecycleOwner = LocalLifecycleOwner.current
        LaunchedEffect(lifecycleOwner) {
            viewModel.effect
                .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collect { effect ->
                    when (effect) {
                        is ClassFormEffect.NavigateBack -> actions.back()
                    }
                }
        }

        if (uiState.isLoading) {
            LoadingDialog()
        } else {
            ClassCreateScreen(
                uiState = uiState,
                onIntent = viewModel::onIntent,
            )
        }
    }

    composable<CompendiumSpellsLibraryRoute> {
        val viewModel = hiltViewModel<CompendiumSpellsLibraryViewModel>()

        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val snackbarHostState = remember { SnackbarHostState() }
        val resources = LocalResources.current

        rememberFileOperationHandler(
            effects = viewModel.fileEffect,
            onExportCompleted = { success ->
                viewModel.onIntent(SpellsIntent.ExportCompleted(success))
            },
            onImportReady = { json -> viewModel.onIntent(SpellsIntent.ImportRequested(json)) },
            onShareFailed = { viewModel.onIntent(SpellsIntent.ShareFailed) }
        )

        val lifecycleOwner = LocalLifecycleOwner.current
        LaunchedEffect(lifecycleOwner) {
            viewModel.effect
                .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collect { effect ->
                    when (effect) {
                        SpellsEffect.NavigateBack -> actions.back()
                        SpellsEffect.NavigateToAddSpell -> actions.createSpell()
                        is SpellsEffect.NavigateToEdit -> actions.openSpellDetails(effect.spellId)
                        is SpellsEffect.ShowSnackbar -> snackbarHostState.showSnackbar(
                            message = resources.getString(effect.messageRes)
                        )
                    }
                }
        }

        SpellLibraryScreen(
            uiState = uiState,
            snackbarHostState = snackbarHostState,
            onIntent = viewModel::onIntent
        )
    }

    composable<CompendiumSpellUpdateRoute>(
        enterTransition = NavAnimation.RightSideSlideAnimation.enterSlideTransition,
        exitTransition = NavAnimation.RightSideSlideAnimation.exitSlideTransition
    ) {
        val viewModel = hiltViewModel<SpellFormViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        val lifecycleOwner = LocalLifecycleOwner.current
        LaunchedEffect(lifecycleOwner) {
            viewModel.effect
                .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collect { effect ->
                    when (effect) {
                        is SpellFormEffect.NavigateBack -> actions.back()
                    }
                }
        }

        if (uiState.isLoading) {
            LoadingDialog()
        } else {
            SpellCreateScreen(
                uiState = uiState,
                onIntent = viewModel::onIntent,
            )
        }
    }
}