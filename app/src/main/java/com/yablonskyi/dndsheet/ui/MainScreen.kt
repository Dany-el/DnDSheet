package com.yablonskyi.dndsheet.ui

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.yablonskyi.dndsheet.data.model.character.Spell
import com.yablonskyi.dndsheet.ui.character.CharacterListViewModel
import com.yablonskyi.dndsheet.ui.navigation.AppSettingsRoute
import com.yablonskyi.dndsheet.ui.navigation.BottomNavItem
import com.yablonskyi.dndsheet.ui.navigation.DnDNavGraph
import com.yablonskyi.dndsheet.ui.navigation.GlobalSpellLibraryRoute
import com.yablonskyi.dndsheet.ui.navigation.ListOfCharactersRoute
import com.yablonskyi.dndsheet.ui.spell.GlobalSpellLibraryViewModel
import com.yablonskyi.dndsheet.ui.utils.importSpellsFromJson

@Composable
fun MainScreen(
    incomingUri: Uri?,
    globalSpellLibraryViewModel: GlobalSpellLibraryViewModel = hiltViewModel(),
    characterListViewModel: CharacterListViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val isSpellSelectionMode by globalSpellLibraryViewModel.isSelectionMode.collectAsStateWithLifecycle()
    val isCharacterSelectionMode by characterListViewModel.isSelectionMode.collectAsStateWithLifecycle()

    var spellsToImport by remember { mutableStateOf<List<Spell>?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }
    val msgOnFailure = stringResource(R.string.failed_to_read_file)
    val msgOnSuccess = stringResource(R.string.spells_imported_success)

    LaunchedEffect(incomingUri) {
        if (incomingUri != null) {
            val result = importSpellsFromJson(context, incomingUri)
            result.onSuccess { spells ->
                spellsToImport = spells
                showImportDialog = true
            }.onFailure { error ->
                Toast.makeText(context, msgOnFailure, Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (showImportDialog) {
        spellsToImport?.let { spells ->
            AlertDialog(
                onDismissRequest = { showImportDialog = false },
                title = { Text(stringResource(R.string.alert_dialog_import_spells)) },
                text = {
                    Text(
                        stringResource(
                            R.string.alert_dialog_spell_confirm_msg,
                            spells.size
                        )
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        globalSpellLibraryViewModel.importSpells(spells)
                        showImportDialog = false
                        Toast.makeText(context, msgOnSuccess, Toast.LENGTH_SHORT).show()

                        navController.navigate(GlobalSpellLibraryRoute) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }) {
                        Text(stringResource(R.string.confirm_import))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showImportDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }

    val topLevelRoutes = remember {
        listOf(
            BottomNavItem(R.string.characters, ListOfCharactersRoute, Icons.Default.Person),
            BottomNavItem(
                R.string.spells, GlobalSpellLibraryRoute,
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
    } == true && !isSpellSelectionMode && !isCharacterSelectionMode

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
                            icon = {
                                Icon(
                                    item.icon,
                                    contentDescription = stringResource(item.name)
                                )
                            },
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