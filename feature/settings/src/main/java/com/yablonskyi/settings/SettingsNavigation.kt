package com.yablonskyi.settings

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.yablonskyi.navigation.AppSettingsRoute
import com.yablonskyi.navigation.LanguagesRoute
import com.yablonskyi.settings.language.LanguagesRoute as LanguagesScreenRoute

fun NavGraphBuilder.settingsGraph(
    onOpenLanguages: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable<AppSettingsRoute> {
        AppSettingsScreen(onOpenLanguages = onOpenLanguages)
    }
    composable<LanguagesRoute> {
        LanguagesScreenRoute(onNavigateBack = onNavigateBack)
    }
}
