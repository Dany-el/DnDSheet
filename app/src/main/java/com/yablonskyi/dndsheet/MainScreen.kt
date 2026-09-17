package com.yablonskyi.dndsheet

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldLayout
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yablonskyi.dndsheet.navigation.DnDSheetNavGraph
import com.yablonskyi.navigation.AppSettingsRoute
import com.yablonskyi.navigation.CharacterSheetsRoute
import com.yablonskyi.navigation.CompendiumRoute
import com.yablonskyi.settings.AppSettingsState
import com.yablonskyi.ui.utils.rememberDebouncedClick

@SuppressLint("UnusedContentLambdaTargetStateParameter")
@Composable
fun MainScreen(
    appState: AppSettingsState,
) {
    val navController = rememberNavController()

    val isSpellSelectionMode = false
    val isCharacterSelectionMode = false

    val topLevelRoutes = remember {
        listOf(
            BottomNavItem(R.string.characters, CharacterSheetsRoute, Icons.Default.Person),
            BottomNavItem(
                R.string.compendium, CompendiumRoute,
                Icons.AutoMirrored.Filled.MenuBook
            ),
            BottomNavItem(R.string.settings, AppSettingsRoute, Icons.Default.Settings)
        )
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val isTopLevelScreen = currentDestination?.let { dest ->
        topLevelRoutes.any { item -> dest.hasRoute(item.route::class) }
    } == true

    val isSelectionActive = isSpellSelectionMode || isCharacterSelectionMode

    val isNavVisible = isTopLevelScreen && !isSelectionActive

    val adaptiveInfo = currentWindowAdaptiveInfo()

    val navLayoutType = if (isSelectionActive) {
        NavigationSuiteType.None
    } else {
        NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(adaptiveInfo)
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        AnimatedContent(
            targetState = appState,
            transitionSpec = {
                fadeIn().togetherWith(fadeOut())
            },
            label = "LanguageAndThemeTransition"
        ) { _ ->
            NavigationSuiteScaffoldLayout(
                layoutType = navLayoutType,
                navigationSuite = {
                    when (navLayoutType) {
                        NavigationSuiteType.NavigationBar -> {

                            val navVisibility = remember {
                                MutableTransitionState(false)
                            }

                            LaunchedEffect(isNavVisible) {
                                navVisibility.targetState = isNavVisible
                            }

                            AnimatedVisibility(
                                visibleState = navVisibility,
                                enter = expandVertically(
                                    animationSpec = tween(350),
                                    expandFrom = Alignment.Top,
                                ) + fadeIn(tween(250)),
                                exit = shrinkVertically(
                                    animationSpec = tween(250),
                                    shrinkTowards = Alignment.Top,
                                ) + fadeOut(tween(200)),
                            ) {
                                NavigationBar {
                                    topLevelRoutes.forEach { item ->
                                        val isSelected =
                                            currentDestination?.hierarchy?.any { it.hasRoute(item.route::class) } == true

                                        val debouncedNavigate = rememberDebouncedClick {
                                            navController.navigate(item.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }

                                        NavigationBarItem(
                                            icon = {
                                                Icon(
                                                    item.icon,
                                                    contentDescription = stringResource(item.name)
                                                )
                                            },
                                            label = { Text(stringResource(item.name)) },
                                            selected = isSelected,
                                            onClick = debouncedNavigate
                                        )
                                    }
                                }
                            }
                        }

                        NavigationSuiteType.NavigationRail, NavigationSuiteType.NavigationDrawer -> {
                            AnimatedVisibility(
                                visible = isNavVisible,
                                enter = slideInHorizontally(
                                    animationSpec = tween(
                                        durationMillis = 400,
                                        easing = FastOutSlowInEasing
                                    )
                                ) { width -> -width } + expandHorizontally(
                                    animationSpec = tween(
                                        durationMillis = 400,
                                        easing = FastOutSlowInEasing
                                    )
                                ),

                                exit = slideOutHorizontally(
                                    animationSpec = tween(
                                        durationMillis = 400,
                                        easing = FastOutSlowInEasing
                                    )
                                ) { width -> -width } + shrinkHorizontally(
                                    animationSpec = tween(
                                        durationMillis = 400,
                                        easing = FastOutSlowInEasing
                                    )
                                )
                            ) {
                                NavigationRail(
                                    containerColor = NavigationBarDefaults.containerColor,
                                ) {
                                    Spacer(modifier = Modifier.weight(1f))

                                    topLevelRoutes.forEach { item ->
                                        val isSelected =
                                            currentDestination?.hierarchy?.any { it.hasRoute(item.route::class) } == true

                                        val debouncedNavigate = rememberDebouncedClick {
                                            navController.navigate(item.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }

                                        NavigationRailItem(
                                            icon = {
                                                Icon(
                                                    item.icon,
                                                    contentDescription = stringResource(item.name)
                                                )
                                            },
                                            label = { Text(stringResource(item.name)) },
                                            selected = isSelected,
                                            onClick = debouncedNavigate,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }

                        NavigationSuiteType.None -> {
                        }
                    }
                }
            ) {
                DnDSheetNavGraph(
                    navController = navController,
                )
            }
        }
    }
}

data class BottomNavItem<T : Any>(
    @param:StringRes val name: Int,
    val route: T,
    val icon: ImageVector
)