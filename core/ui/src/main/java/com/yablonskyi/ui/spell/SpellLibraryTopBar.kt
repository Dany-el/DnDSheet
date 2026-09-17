package com.yablonskyi.ui.spell

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.window.core.layout.WindowSizeClass
import com.yablonskyi.ui.R
import com.yablonskyi.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpellLibraryTopBar(
    uiState: SpellLibraryState,
    onIntent: (SpellsIntent) -> Unit,
) {
    var isSearchExpanded by remember { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val isWideScreen =
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

    BackHandler(enabled = uiState.isSelectionMode || isSearchExpanded) {
        if (uiState.isSelectionMode) onIntent(SpellsIntent.ClearSelection)
        else {
            isSearchExpanded = false
            onIntent(SpellsIntent.SearchQueryChanged(""))
        }
    }

    val topBarColor by animateColorAsState(
        targetValue = if (uiState.isSelectionMode) MaterialTheme.colorScheme.surface
        else MaterialTheme.colorScheme.surfaceColorAtElevation(Dimens.TopBar.Elevation)
    )

    CenterAlignedTopAppBar(
        navigationIcon = {
            when {
                uiState.isSelectionMode -> IconButton(
                    onClick = {
                        onIntent(SpellsIntent.ClearSelection)
                        isSearchExpanded = false
                        onIntent(SpellsIntent.SearchQueryChanged(""))
                    }
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.clear_selection)
                    )
                }

                isSearchExpanded -> IconButton(
                    onClick = {
                        isSearchExpanded = false
                        onIntent(SpellsIntent.SearchQueryChanged(""))
                    }
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.close_search)
                    )
                }

                else -> IconButton(onClick = { onIntent(SpellsIntent.NavigateBack) }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.navigate_back)
                    )
                }
            }
        },
        title = {
            when {
                uiState.isSelectionMode -> {
                    Text(
                        stringResource(
                            R.string.compendium_selected_count,
                            uiState.selectedSpellIds.size
                        )
                    )
                }

                isSearchExpanded -> {
                    TextField(
                        value = uiState.searchQuery,
                        onValueChange = { onIntent(SpellsIntent.SearchQueryChanged(it)) },
                        placeholder = { Text(stringResource(R.string.search_spells)) },
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

                else -> {
                    Text(
                        text = stringResource(
                            if (uiState.isLearnMode) R.string.spell_selection else R.string.msg_spell_library
                        ),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        textAlign = if (isWideScreen) TextAlign.Center else TextAlign.Left,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        actions = {
            if (!isSearchExpanded && !uiState.isSelectionMode) {
                IconButton(onClick = { isSearchExpanded = true }) {
                    Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search))
                }
            }
            IconButton(onClick = { onIntent(SpellsIntent.ToggleFiltersExpanded) }) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = stringResource(R.string.toggle_filters),
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = topBarColor,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}