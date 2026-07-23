package com.yablonskyi.dndsheet.ui.compendium.classes

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
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
import androidx.window.core.layout.WindowSizeClass
import com.yablonskyi.dndsheet.R
import com.yablonskyi.dndsheet.data.model.rulebook.CharacterClass
import com.yablonskyi.dndsheet.ui.compendium.races.CollapsibleSectionState
import com.yablonskyi.dndsheet.ui.compendium.races.HeaderItem
import com.yablonskyi.dndsheet.ui.spell.SelectionBottomBar
import com.yablonskyi.dndsheet.ui.utils.DeletingItemConfirmDialog
import com.yablonskyi.dndsheet.ui.utils.SlicedDropdownMenu
import com.yablonskyi.dndsheet.ui.utils.SlicedMenuItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Immutable
data class ClassesUiState(
    val origClasses: List<CharacterClass> = emptyList(),
    val homebrewClasses: List<CharacterClass> = emptyList(),
    val selectedClassesIds: Set<String> = emptySet(),
    val isSelectionMode: Boolean = false,
    val isAllSelected: Boolean = false,
    val searchQuery: String = "",
    val origSectionState: CollapsibleSectionState = CollapsibleSectionState(),
    val homebrewSectionState: CollapsibleSectionState = CollapsibleSectionState(),
)

@Immutable
data class ClassesActions(
    val onSearchQueryChange: (String) -> Unit = {},
    val onClearSelection: () -> Unit = {},
    val onDeleteSelected: () -> Unit = {},
    val onToggleSelectAll: () -> Unit = {},
    val onToggleRaceSelection: (String) -> Unit = {},
    val onExportRequested: (String?) -> String? = { null },
    val onExportAllSelected: () -> String? = { null },
    val onImportRequested: (String) -> Unit = {},
    val onCreateClass: () -> Unit = {},
    val onDelete: (CharacterClass) -> Unit = {},
    val onEdit: (String) -> Unit = {},
    val onDetails: (String) -> Unit = {},
    val onNavigateBack: () -> Unit = {},
    val onCollapseOrigSection: () -> Unit = {},
    val onCollapseHomebrewSection: () -> Unit = {},
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassesScreen(
    uiState: ClassesUiState,
    actions: ClassesActions,
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
            ClassesTopAppBar(
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
                FloatingActionButton(onClick = actions.onCreateClass) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                }
            }
        },
        bottomBar = {
            SelectionBottomBar(
                title = stringResource(R.string.q_delete_race),
                confirmMsg = pluralStringResource(
                    R.plurals.q_confirm_text_races,
                    uiState.selectedClassesIds.size,
                    uiState.selectedClassesIds.size,
                ),
                isSelectionMode = uiState.isSelectionMode,
                isAllSelected = uiState.isAllSelected,
                onExportSelected = {
                    jsonToWrite = actions.onExportAllSelected()
                    if (jsonToWrite != null) exportLauncher.launch("classes_backup.json")
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
            if (uiState.origClasses.isNotEmpty()) {
                CollapsibleClassesList(
                    header = R.string.player_handbook,
                    classes = uiState.origClasses,
                    selectedClassesIds = emptySet(),
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

            if (uiState.homebrewClasses.isNotEmpty()) {
                CollapsibleClassesList(
                    header = R.string.homebrew,
                    classes = uiState.homebrewClasses,
                    selectedClassesIds = uiState.selectedClassesIds,
                    isSelectionMode = uiState.isSelectionMode,
                    isHomebrew = true,
                    onToggleSelection = actions.onToggleRaceSelection,
                    onExport = { cls ->
                        jsonToWrite = actions.onExportRequested(cls.id)
                        if (jsonToWrite != null) {
                            exportLauncher.launch("${cls.name.replace(" ", "_")}.json")
                        }
                    },
                    onEdit = actions.onEdit,
                    onDelete = actions.onDelete,
                    onDetails = actions.onDetails,
                    state = uiState.homebrewSectionState,
                    onCollapse = actions.onCollapseHomebrewSection
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassesTopAppBar(
    uiState: ClassesUiState,
    actions: ClassesActions,
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
                        Text("${uiState.selectedClassesIds.size}")
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
                    text = stringResource(R.string.classes),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
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

fun LazyStaggeredGridScope.CollapsibleClassesList(
    @StringRes header: Int,
    classes: List<CharacterClass>,
    selectedClassesIds: Set<String>,
    isSelectionMode: Boolean,
    isHomebrew: Boolean,
    state: CollapsibleSectionState,
    onDetails: (String) -> Unit,
    onEdit: (String) -> Unit,
    onExport: (CharacterClass) -> Unit,
    onDelete: (CharacterClass) -> Unit,
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
        items(items = classes, key = { cls -> cls.id }) { cls ->

            ClassCard(
                cls = cls,
                isSelected = selectedClassesIds.contains(cls.id),
                isSelectionMode = isSelectionMode,
                isHomebrew = isHomebrew,
                onClick = {
                    if (isSelectionMode && isHomebrew) onToggleSelection(cls.id)
                    else onDetails(cls.id)
                },
                onSelect = {
                    if (isHomebrew) onToggleSelection(cls.id)
                },
                onEdit = { onEdit(cls.id) },
                onExport = { onExport(cls) },
                onDelete = { onDelete(cls) },
                onToggleSelection = { onToggleSelection(cls.id) },
            )
        }
    }
}

@Composable
fun ClassCard(
    cls: CharacterClass,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    isHomebrew: Boolean,
    onClick: () -> Unit,
    onSelect: () -> Unit,
    onExport: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceContainerHighest,
        label = "classCardColor"
    )

    OutlinedCard (
        colors = CardDefaults.outlinedCardColors(containerColor = animatedColor),
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
                    text = cls.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    modifier = Modifier.weight(1f)
                )
                if (isHomebrew) {
                    ClassCardTrailingAction(
                        className = cls.name,
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
            Card(
                modifier = modifier,
                shape = MaterialTheme.shapes.small,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = cls.hitDice,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun ClassCardTrailingAction(
    className: String,
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
                title = stringResource(R.string.q_delete_class),
                text = stringResource(R.string.q_confirm_text, className),
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