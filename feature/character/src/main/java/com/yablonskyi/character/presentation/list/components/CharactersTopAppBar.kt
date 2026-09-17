package com.yablonskyi.character.presentation.list.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import com.yablonskyi.character.presentation.list.CharacterListIntent
import com.yablonskyi.character.presentation.list.CharacterListState
import com.yablonskyi.ui.R
import com.yablonskyi.ui.settings.ListView
import com.yablonskyi.ui.theme.Dimens
import com.yablonskyi.ui.utils.SlicedDropdownMenu
import com.yablonskyi.ui.utils.SlicedMenuItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharactersTopAppBar(
    uiState: CharacterListState,
    onIntent: (CharacterListIntent) -> Unit,
    modifier: Modifier = Modifier,
    listView: ListView = ListView.LIST,
) {
    var isSearchExpanded by remember { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }

    val topBarColor by animateColorAsState(
        targetValue = if (uiState.isSelectionMode) MaterialTheme.colorScheme.surface
        else MaterialTheme.colorScheme.surfaceColorAtElevation(Dimens.TopBar.Elevation)
    )

    val windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val isWideScreen =
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

    BackHandler(enabled = uiState.isSelectionMode || isSearchExpanded) {
        if (uiState.isSelectionMode) {
            onIntent(CharacterListIntent.ClearSelection)
        } else if (isSearchExpanded) {
            isSearchExpanded = false
            onIntent(CharacterListIntent.SearchChanged(""))
        }
    }

    CenterAlignedTopAppBar(
        modifier = modifier,
        navigationIcon = {
            when {
                uiState.isSelectionMode -> {
                    IconButton(
                        onClick = {
                            onIntent(CharacterListIntent.ClearSelection)
                            isSearchExpanded = false
                            onIntent(CharacterListIntent.SearchChanged(""))
                        }
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.clear_selection)
                        )
                    }
                }

                isSearchExpanded -> {
                    IconButton(
                        onClick = {
                            isSearchExpanded = false
                            onIntent(CharacterListIntent.SearchChanged(""))
                        }
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.close_search)
                        )
                    }
                }
            }
        },
        title = {
            when {
                isSearchExpanded -> {
                    TextField(
                        value = uiState.searchQuery,
                        onValueChange = { onIntent(CharacterListIntent.SearchChanged(it)) },
                        placeholder = { Text(stringResource(R.string.search)) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(searchFocusRequester),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                    LaunchedEffect(Unit) { searchFocusRequester.requestFocus() }
                }

                uiState.isSelectionMode -> {
                    Text(
                        stringResource(
                            R.string.compendium_selected_count,
                            uiState.selectedIds.size
                        )
                    )
                }

                else -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // TODO
                        Icon(
                            painter = painterResource(com.yablonskyi.dice.R.drawable.dice_d20),
                            contentDescription = null
                        )
                        Text(
                            text = stringResource(R.string.app_name),
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                            textAlign = if (isWideScreen) TextAlign.Center else TextAlign.Left,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        actions = {
            if (!isSearchExpanded && !uiState.isSelectionMode) {
                IconButton(onClick = { isSearchExpanded = true }) {
                    Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search))
                }
            }
            if (!uiState.isSelectionMode) {
                var menuExpanded by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                    }
                    SlicedDropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        items = listOf(
                            SlicedMenuItem(
                                text = when (listView) {
                                    ListView.LIST -> stringResource(R.string.grid)
                                    ListView.GRID -> stringResource(R.string.list)
                                },
                                icon = when (listView) {
                                    ListView.LIST -> Icons.Default.GridView
                                    ListView.GRID -> Icons.AutoMirrored.Default.List
                                },
                                onClick = { onIntent(CharacterListIntent.ToggleListView) }
                            ),
                        )
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = topBarColor,
            titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}