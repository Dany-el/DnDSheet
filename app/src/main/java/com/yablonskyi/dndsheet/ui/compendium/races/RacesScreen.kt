package com.yablonskyi.dndsheet.ui.compendium.races

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.window.core.layout.WindowSizeClass
import com.yablonskyi.dndsheet.R
import com.yablonskyi.dndsheet.data.model.rulebook.Race
import com.yablonskyi.dndsheet.ui.spell.SelectionBottomBar
import com.yablonskyi.dndsheet.ui.utils.DeletingItemConfirmDialog
import com.yablonskyi.dndsheet.ui.utils.SlicedDropdownMenu
import com.yablonskyi.dndsheet.ui.utils.SlicedMenuItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Immutable
data class RacesUiState(
    val origRaces: List<Race> = emptyList(),
    val homebrewRaces: List<Race> = emptyList(),
    val selectedRaceIds: Set<String> = emptySet(),
    val isSelectionMode: Boolean = false,
    val isAllSelected: Boolean = false,
    val searchQuery: String = "",
    val origSectionState: CollapsibleSectionState = CollapsibleSectionState(),
    val homebrewSectionState: CollapsibleSectionState = CollapsibleSectionState(),
)

@Immutable
data class RacesActions(
    val onSearchQueryChange: (String) -> Unit = {},
    val onClearSelection: () -> Unit = {},
    val onDeleteSelected: () -> Unit = {},
    val onToggleSelectAll: () -> Unit = {},
    val onToggleRaceSelection: (String) -> Unit = {},
    val onExportRequested: (String?) -> String? = { null },
    val onExportAllSelected: () -> String? = { null },
    val onImportRequested: (String) -> Unit = {},
    val onCreateRace: () -> Unit = {},
    val onDelete: (Race) -> Unit = {},
    val onEdit: (String) -> Unit = {},
    val onDetails: (String) -> Unit = {},
    val onNavigateBack: () -> Unit = {},
    val onCollapseOrigSection: () -> Unit = {},
    val onCollapseHomebrewSection: () -> Unit = {},
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RacesScreen(
    uiState: RacesUiState,
    actions: RacesActions,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var jsonToWrite by remember { mutableStateOf<String?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { targetUri ->
            jsonToWrite?.let { json ->
                scope.launch(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(targetUri)
                        ?.use { it.write(json.toByteArray()) }
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { targetUri ->
            scope.launch(Dispatchers.IO) {
                context.contentResolver.openInputStream(targetUri)?.use {
                    actions.onImportRequested(it.bufferedReader().readText())
                }
            }
        }
    }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            RacesTopAppBar(
                uiState = uiState,
                actions = actions,
                scrollBehavior = scrollBehavior,
                onImportClick = { importLauncher.launch(arrayOf("application/json", "*/*")) }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = !uiState.isSelectionMode,
                enter = scaleIn(),
                exit = scaleOut()
            ) {
                FloatingActionButton(onClick = actions.onCreateRace) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                }
            }
        },
        bottomBar = {
            SelectionBottomBar(
                title = stringResource(R.string.q_delete_race),
                confirmMsg = pluralStringResource(
                    R.plurals.q_confirm_text_races,
                    uiState.selectedRaceIds.size,
                    uiState.selectedRaceIds.size,
                ),
                isSelectionMode = uiState.isSelectionMode,
                isAllSelected = uiState.isAllSelected,
                onExportSelected = {
                    jsonToWrite = actions.onExportAllSelected()
                    if (jsonToWrite != null) exportLauncher.launch("races_backup.json")
                },
                onDeleteSelected = actions.onDeleteSelected,
                onToggleSelectAll = actions.onToggleSelectAll
            )
        }
    ) { innerPadding ->
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            contentPadding = PaddingValues(
                start = 8.dp,
                end = 8.dp,
                top = 12.dp,
                bottom = innerPadding.calculateBottomPadding() + 88.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalItemSpacing = 4.dp,
            modifier = Modifier
                .padding(innerPadding)
                .animateContentSize()
        ) {
            if (uiState.origRaces.isNotEmpty()) {
                CollapsibleRacesList(
                    header = R.string.player_handbook,
                    races = uiState.origRaces,
                    selectedRaceIds = emptySet(),
                    isSelectionMode = false,
                    isHomebrew = false,
                    onToggleSelection = {},
                    onExport = {},
                    onDelete = {},
                    onEdit = {},
                    onDetails = actions.onDetails,
                    state = uiState.origSectionState,
                    onCollapse = actions.onCollapseOrigSection
                )
            }

            if (uiState.homebrewRaces.isNotEmpty()) {
                CollapsibleRacesList(
                    header = R.string.homebrew,
                    races = uiState.homebrewRaces,
                    selectedRaceIds = uiState.selectedRaceIds,
                    isSelectionMode = uiState.isSelectionMode,
                    isHomebrew = true,
                    onToggleSelection = actions.onToggleRaceSelection,
                    onExport = { race ->
                        jsonToWrite = actions.onExportRequested(race.id)
                        if (jsonToWrite != null) {
                            exportLauncher.launch("${race.name.replace(" ", "_")}.json")
                        }
                    },
                    onDelete = actions.onDelete,
                    onDetails = actions.onDetails,
                    onEdit = actions.onEdit,
                    state = uiState.homebrewSectionState,
                    onCollapse = actions.onCollapseHomebrewSection
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RacesTopAppBar(
    uiState: RacesUiState,
    actions: RacesActions,
    scrollBehavior: TopAppBarScrollBehavior,
    onImportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSearchExpanded by remember { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }
    val elevatedSurfaceColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val isWideScreen =
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

    BackHandler(enabled = uiState.isSelectionMode || isSearchExpanded) {
        when {
            uiState.isSelectionMode -> actions.onClearSelection()
            isSearchExpanded -> {
                isSearchExpanded = false
                actions.onSearchQueryChange("")
            }
        }
    }

    CenterAlignedTopAppBar(
        modifier = modifier,
        scrollBehavior = if (!isSearchExpanded && !uiState.isSelectionMode) scrollBehavior else null,
        navigationIcon = {
            when {
                uiState.isSelectionMode -> Button(
                    onClick = {
                        actions.onClearSelection()
                        isSearchExpanded = false
                        actions.onSearchQueryChange("")
                    },
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Clear selection")
                        Text("${uiState.selectedRaceIds.size}")
                    }
                }

                isSearchExpanded -> IconButton(onClick = {
                    isSearchExpanded = false
                    actions.onSearchQueryChange("")
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close search")
                }

                else -> IconButton(onClick = actions.onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Navigate back")
                }
            }
        },
        title = {
            if (isSearchExpanded) {
                TextField(
                    value = uiState.searchQuery,
                    onValueChange = actions.onSearchQueryChange,
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
            } else if (!uiState.isSelectionMode) {
                Text(
                    text = stringResource(R.string.races),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                    textAlign = if (isWideScreen) TextAlign.Center else TextAlign.Left,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        actions = {
            if (!isSearchExpanded) {
                IconButton(onClick = { isSearchExpanded = true }) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
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
                                text = stringResource(R.string.confirm_import),
                                icon = Icons.Default.Download,
                                onClick = onImportClick
                            )
                        )
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = elevatedSurfaceColor,
            scrolledContainerColor = elevatedSurfaceColor,
            titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

fun LazyStaggeredGridScope.CollapsibleRacesList(
    @StringRes header: Int,
    races: List<Race>,
    selectedRaceIds: Set<String>,
    isSelectionMode: Boolean,
    isHomebrew: Boolean,
    state: CollapsibleSectionState,
    onDetails: (String) -> Unit,
    onEdit: (String) -> Unit,
    onExport: (Race) -> Unit,
    onDelete: (Race) -> Unit,
    onToggleSelection: (String) -> Unit,
    onCollapse: () -> Unit,
) {
    item(span = StaggeredGridItemSpan.FullLine) {
        HeaderItem(
            title = stringResource(header),
            isExpanded = state.isExpanded,
            onToggle = onCollapse
        )
    }

    if (state.isExpanded) {
        itemsIndexed(items = races, key = { _, race -> race.id }) { index, race ->
            val staggeredDelay = remember(index) { index * 25 }

            RaceCard(
                race = race,
                isSelected = selectedRaceIds.contains(race.id),
                isSelectionMode = isSelectionMode,
                isHomebrew = isHomebrew,
                onClick = {
                    if (isSelectionMode && isHomebrew) onToggleSelection(race.id)
                    else onDetails(race.id)
                },
                onSelect = {
                    if (isHomebrew) onToggleSelection(race.id)
                },
                onEdit = { onEdit(race.id) },
                onExport = { onExport(race) },
                onDelete = { onDelete(race) },
                onToggleSelection = { onToggleSelection(race.id) },
                modifier = Modifier.animateItem(
                    fadeInSpec = tween(
                        durationMillis = 200,
                        delayMillis = staggeredDelay,
                        easing = LinearEasing
                    ),
                    placementSpec = tween(durationMillis = 200, easing = LinearOutSlowInEasing)
                )
            )
        }
    }
}

@Composable
fun HeaderItem(
    title: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                val rotation by animateFloatAsState(
                    targetValue = if (isExpanded) 180f else 0f,
                    label = "ArrowRotation"
                )
                IconButton(
                    onClick = onToggle
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse List" else "Expand List",
                        modifier = Modifier.rotate(rotation)
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@Composable
fun RaceCard(
    race: Race,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    isHomebrew: Boolean,
    onClick: () -> Unit,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit,
    onToggleSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.secondaryContainer
        else MaterialTheme.colorScheme.surfaceContainerLowest,
        label = "raceCardColor"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = animatedColor),
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    if (isHomebrew) {
                        onSelect()
                    }
                }
            )
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = race.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    modifier = Modifier.weight(1f)
                )
                if (isHomebrew) {
                    RaceCardTrailingAction(
                        raceName = race.name,
                        isSelected = isSelected,
                        isSelectionMode = isSelectionMode,
                        onToggleSelection = onToggleSelection,
                        onExport = onExport,
                        onDelete = onDelete,
                        onEdit = onEdit
                    )
                }
            }
            Spacer(Modifier.height(32.dp))
            if (race.abilityBonuses.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    race.abilityBonuses.forEach { (ability, bonus) ->
                        AbilityBonusChip(ability = ability, bonus = bonus)
                    }
                }
            }
        }
    }
}

@Composable
private fun RaceCardTrailingAction(
    raceName: String,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onToggleSelection: () -> Unit,
    onExport: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isSelectionMode) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggleSelection() },
            modifier = modifier
        )
    } else {
        var menuExpanded by remember { mutableStateOf(false) }
        var showConfirmDialog by remember { mutableStateOf(false) }

        if (showConfirmDialog) {
            DeletingItemConfirmDialog(
                title = stringResource(R.string.q_delete_race),
                text = stringResource(R.string.q_confirm_text, raceName),
                onConfirm = onDelete,
                onDiscard = { showConfirmDialog = false }
            )
        }

        Box(modifier = modifier) {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Options")
            }
            SlicedDropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                items = listOf(
                    SlicedMenuItem(
                        text = stringResource(R.string.edit),
                        icon = Icons.Default.Edit,
                        onClick = onEdit
                    ),
                    SlicedMenuItem(
                        text = stringResource(R.string.share_as_json),
                        icon = Icons.Default.Share,
                        onClick = onExport
                    ),
                    SlicedMenuItem(
                        text = stringResource(R.string.delete),
                        icon = Icons.Default.Delete,
                        contentColor = MaterialTheme.colorScheme.error,
                        onClick = { showConfirmDialog = true }
                    )
                )
            )
        }
    }
}