package com.yablonskyi.dndsheet.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yablonskyi.dndsheet.R
import com.yablonskyi.dndsheet.ui.navigation.AppSettingsRoute
import com.yablonskyi.dndsheet.ui.navigation.BottomNavItem
import com.yablonskyi.dndsheet.ui.navigation.DnDNavGraph
import com.yablonskyi.dndsheet.ui.navigation.ListOfCharactersRoute
import com.yablonskyi.dndsheet.ui.navigation.SpellLibraryRoute

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    val topLevelRoutes = remember {
        listOf(
            BottomNavItem(R.string.characters, ListOfCharactersRoute, Icons.Default.Person),
            BottomNavItem(
                R.string.spells, SpellLibraryRoute(characterId = -1L),
                Icons.AutoMirrored.Filled.LibraryBooks
            ),
            BottomNavItem(R.string.settings, AppSettingsRoute, Icons.Default.Settings)
        )
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val isBottomBarVisible = currentDestination?.let { dest ->
        topLevelRoutes.any { item ->
            dest.hasRoute(item.route::class)
        }
    } == true

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
        bottomBar = {
            if (isBottomBarVisible) {
                NavigationBar {
                    topLevelRoutes.forEach { item ->
                        val isSelected = currentDestination.hierarchy.any {
                            it.hasRoute(item.route::class)
                        }

                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = stringResource(item.name)) },
                            label = { Text(stringResource(item.name)) },
                            selected = isSelected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { outerPadding ->
        DnDNavGraph(
            navController = navController,
            modifier = Modifier.padding(outerPadding)
        )
    }
}