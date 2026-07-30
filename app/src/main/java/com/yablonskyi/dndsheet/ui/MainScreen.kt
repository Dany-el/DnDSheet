package com.yablonskyi.dndsheet.ui

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yablonskyi.dndsheet.R
import com.yablonskyi.dndsheet.ui.character.CharacterListViewModel
import com.yablonskyi.dndsheet.ui.navigation.AppSettingsRoute
import com.yablonskyi.dndsheet.ui.navigation.BottomNavItem
import com.yablonskyi.dndsheet.ui.navigation.CompendiumRoute
import com.yablonskyi.dndsheet.ui.navigation.DnDNavGraph
import com.yablonskyi.dndsheet.ui.navigation.ListOfCharactersRoute
import com.yablonskyi.dndsheet.ui.settings.AppSettingsState
import com.yablonskyi.dndsheet.ui.spell.GlobalSpellLibraryViewModel
import com.yablonskyi.dndsheet.ui.utils.rememberDebouncedClick

@SuppressLint("UnusedContentLambdaTargetStateParameter")
@Composable
fun MainScreen(
    appState: AppSettingsState,
    globalSpellLibraryViewModel: GlobalSpellLibraryViewModel = hiltViewModel(),
    characterListViewModel: CharacterListViewModel = hiltViewModel()
) {
    val navController = rememberNavController()

    val isSpellSelectionMode by globalSpellLibraryViewModel.isSelectionMode.collectAsStateWithLifecycle()
    val isCharacterSelectionMode by characterListViewModel.isSelectionMode.collectAsStateWithLifecycle()

    val topLevelRoutes = remember {
        listOf(
            BottomNavItem(R.string.characters, ListOfCharactersRoute, Icons.Default.Person),
            BottomNavItem(
                R.string.compendium, CompendiumRoute.Home,
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
                            AnimatedVisibility(
                                visible = isNavVisible,
                                enter = slideInVertically(
                                    animationSpec = tween(
                                        durationMillis = 400,
                                        easing = FastOutSlowInEasing
                                    )
                                ) { height -> height } + expandVertically(
                                    animationSpec = tween(
                                        durationMillis = 400,
                                        easing = FastOutSlowInEasing
                                    )
                                ),
                                exit = slideOutVertically(
                                    animationSpec = tween(
                                        durationMillis = 300,
                                        easing = FastOutSlowInEasing
                                    )
                                ) { height -> height } + shrinkVertically(
                                    animationSpec = tween(
                                        durationMillis = 300,
                                        easing = FastOutSlowInEasing
                                    )
                                )
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
                DnDNavGraph(
                    navController = navController,
                )
            }
        }
    }
}