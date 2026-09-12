package com.yablonskyi.settings

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.yablonskyi.navigation.AppSettingsRoute

fun NavGraphBuilder.settingsGraph() {
    composable<AppSettingsRoute> {
        AppSettingsScreen()
    }
}