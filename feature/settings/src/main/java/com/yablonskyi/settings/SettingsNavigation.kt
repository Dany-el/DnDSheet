package com.yablonskyi.settings

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.yablonskyi.navigation.AppSettingsRoute
import com.yablonskyi.navigation.BackupRestoreRoute
import com.yablonskyi.navigation.LanguagesRoute
import com.yablonskyi.ui.animation.navigation.NavAnimation
import com.yablonskyi.settings.language.LanguagesRoute as LanguagesScreenRoute

fun NavGraphBuilder.settingsGraph(
    onOpenLanguages: () -> Unit,
    onOpenBackupRestore: () -> Unit,
    onNavigateBack: () -> Unit,
    enterAnimation: AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards EnterTransition? = NavAnimation.SlideTransition.enterSlideTransition,
    exitAnimation: AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards ExitTransition? = NavAnimation.SlideTransition.exitSlideTransition,
) {
    composable<AppSettingsRoute> {
        AppSettingsScreen(
            onOpenLanguages = onOpenLanguages,
            onOpenBackupRestore = onOpenBackupRestore,
        )
    }

    composable<LanguagesRoute>(
        enterTransition = enterAnimation,
        exitTransition = exitAnimation,
    ) {
        LanguagesScreenRoute(onNavigateBack = onNavigateBack)
    }

    composable<BackupRestoreRoute>(
        enterTransition = enterAnimation,
        exitTransition = exitAnimation,
    ) {
        BackupRestoreScreen(onNavigateBack = onNavigateBack)
    }
}