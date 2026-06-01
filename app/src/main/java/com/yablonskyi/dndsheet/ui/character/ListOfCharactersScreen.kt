package com.yablonskyi.dndsheet.ui.character

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.runtime.Stable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewDynamicColors
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import coil.compose.AsyncImage
import com.yablonskyi.dndsheet.R
import com.yablonskyi.dndsheet.data.model.character.Character
import com.yablonskyi.dndsheet.data.model.character.CharacterSheet
import com.yablonskyi.dndsheet.ui.character.launchers.rememberCharacterImportLauncher
import com.yablonskyi.dndsheet.ui.character.launchers.rememberMultiCharacterExportLauncher
import com.yablonskyi.dndsheet.ui.settings.ListView
import com.yablonskyi.dndsheet.ui.spell.SelectionBottomBar
import com.yablonskyi.dndsheet.ui.theme.DnDSheetTheme
import com.yablonskyi.dndsheet.ui.utils.DeletingItemConfirmDialog
import com.yablonskyi.dndsheet.ui.utils.LoadingDialog
import com.yablonskyi.dndsheet.ui.utils.PdfExporter
import com.yablonskyi.dndsheet.ui.utils.SlicedDropdownMenu
import com.yablonskyi.dndsheet.ui.utils.SlicedMenuItem
import com.yablonskyi.dndsheet.ui.utils.UiUtils
import com.yablonskyi.dndsheet.ui.utils.sharePdfIntent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Immutable
data class CharacterListUiState(
    val characters: List<Character> = emptyList(),
    val loadingState: Boolean = false,
    val listView: ListView = ListView.LIST,
    val searchQuery: String = "",
    val isSelectionMode: Boolean = false,
    val isAllSelected: Boolean = false,
    val selectedCharacters: Set<Character> = emptySet(),
    val windowSizeClass: WindowSizeClass
)

@Immutable
data class CharacterListActions(
    val onSearchQueryChange: (String) -> Unit = {},
    val onToggleListView: () -> Unit = {},
    val onClearSelection: () -> Unit = {},
    val onDeleteSelected: () -> Unit = {},
    val onToggleSelectAll: () -> Unit = {},
    val toggleSelection: (Character) -> Unit = {},
    val onAdd: () -> Unit = {},
    val onDelete: (Character) -> Unit = {},
    val onCharacterClick: (Long) -> Unit = {},
    val onImportSheets: (List<CharacterSheet>) -> Unit = {},
    val onExportSheets: suspend () -> List<CharacterSheet> = { emptyList() },
    val onGetCharacterSheet: suspend (Character) -> CharacterSheet = { throw Exception("Not implemented") }
)

@Stable
data class CharacterItemActions(
    val onClick: () -> Unit,
    val onLongClick: () -> Unit,
    val onToggleSelection: () -> Unit,
    val onDelete: () -> Unit,
    val onExport: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedTransitionScope.ListOfCharactersScreen(
    uiState: CharacterListUiState,
    actions: CharacterListActions,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val selectionFailureMessage = stringResource(R.string.select_at_least_one_item)
    val importMessage = stringResource(R.string.import_file_empty)

    var isLocalLoading by remember { mutableStateOf(false) }
    var pendingImportSheets by remember { mutableStateOf<List<CharacterSheet>?>(null) }

    // Export Launcher
    val exportLauncher = rememberMultiCharacterExportLauncher(
        onLoadingStateChange = { isLocalLoading = it },
        sheetsToExport = actions.onExportSheets,
        onExportComplete = actions.onClearSelection
    )

    // Import Launcher
    val importLauncher = rememberCharacterImportLauncher(
        onLoadingStateChange = { isLocalLoading = it },
        onImportCharacters = { sheets ->
            if (sheets.isNotEmpty()) {
                pendingImportSheets = sheets
            } else {
                Toast.makeText(context, importMessage, Toast.LENGTH_SHORT).show()
            }
        }
    )

    val onExportPdf: (Character) -> Unit = remember(context, actions.onGetCharacterSheet) {
        { character: Character ->
            isLocalLoading = true
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val sheet = actions.onGetCharacterSheet(character)
                    val pdfFile = PdfExporter.generateCustomSheet(context, sheet)
                    withContext(Dispatchers.Main) {
                        isLocalLoading = false
                        if (pdfFile != null) {
                            sharePdfIntent(context, pdfFile)
                        } else {
                            Toast.makeText(
                                context, "Failed to generate PDF", Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        isLocalLoading = false
                        Toast.makeText(
                            context,
                            "An error occurred during export",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    val isWideScreen =
        uiState.windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
    val screenInsets =
        if (isWideScreen) ScaffoldDefaults.contentWindowInsets else WindowInsets(bottom = 0.dp)
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    Scaffold(
        contentWindowInsets = screenInsets,
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CharactersTopAppBar(
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
                FloatingActionButton(
                    onClick = actions.onAdd,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create New Character"
                    )
                }
            }
        },
        bottomBar = {
            SelectionBottomBar(
                title = stringResource(R.string.q_delete_character),
                confirmMsg = stringResource(
                    R.string.q_confirm_text_characters,
                    uiState.selectedCharacters.size
                ),
                isSelectionMode = uiState.isSelectionMode,
                isAllSelected = uiState.isAllSelected,
                onExportSelected = {
                    if (uiState.selectedCharacters.isEmpty()) {
                        Toast.makeText(context, selectionFailureMessage, Toast.LENGTH_SHORT).show()
                    } else {
                        exportLauncher.launch("characters_backup.json")
                    }
                },
                onDeleteSelected = actions.onDeleteSelected,
                onToggleSelectAll = actions.onToggleSelectAll
            )
        }
    ) { padding ->
        if (!uiState.loadingState) {
            CharactersContent(
                uiState = uiState,
                actions = actions,
                animatedVisibilityScope = animatedVisibilityScope,
                onExportPdf = onExportPdf,
                modifier = Modifier.padding(padding)
            )
        } else {
            Box(
                Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
        }
    }

    if (isLocalLoading) LoadingDialog()

    pendingImportSheets?.let { sheets ->
        val successMsg = stringResource(R.string.success_import)
        AlertDialog(
            onDismissRequest = { pendingImportSheets = null },
            title = { Text(stringResource(R.string.confirm_import_title)) },
            text = { Text(stringResource(R.string.alert_dialog_import_chars, sheets.size)) },
            confirmButton = {
                TextButton(onClick = {
                    actions.onImportSheets(sheets)
                    pendingImportSheets = null
                    Toast.makeText(context, successMsg, Toast.LENGTH_SHORT).show()
                }) { Text(stringResource(R.string.confirm_import)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    pendingImportSheets = null
                }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharactersTopAppBar(
    uiState: CharacterListUiState,
    actions: CharacterListActions,
    scrollBehavior: TopAppBarScrollBehavior,
    onImportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSearchExpanded by remember { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }

    val elevatedSurfaceColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
    val isWideScreen =
        uiState.windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

    BackHandler(enabled = uiState.isSelectionMode || isSearchExpanded) {
        if (uiState.isSelectionMode) {
            actions.onClearSelection()
        } else if (isSearchExpanded) {
            isSearchExpanded = false
            actions.onSearchQueryChange("")
        }
    }

    CenterAlignedTopAppBar(
        modifier = modifier,
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
                        Text(text = "${uiState.selectedCharacters.size}")

                    }
                }

                isSearchExpanded -> IconButton(
                    onClick = {
                        isSearchExpanded = false
                        actions.onSearchQueryChange("")
                    }
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close search")
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
                    stringResource(R.string.sheets),
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
                                text = when (uiState.listView) {
                                    ListView.LIST -> stringResource(R.string.grid)
                                    ListView.GRID -> stringResource(R.string.list)
                                },
                                icon = when (uiState.listView) {
                                    ListView.LIST -> Icons.Default.GridView
                                    ListView.GRID -> Icons.AutoMirrored.Default.List
                                },
                                onClick = actions.onToggleListView
                            ),
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
        scrollBehavior = if (!uiState.isSelectionMode && !isSearchExpanded) scrollBehavior else null,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = elevatedSurfaceColor,
            scrolledContainerColor = elevatedSurfaceColor,
            titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

@Composable
fun SharedTransitionScope.CharactersContent(
    uiState: CharacterListUiState,
    actions: CharacterListActions,
    onExportPdf: (Character) -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier
) {
    val fabSpacing = 72.dp

    Box(
        modifier = modifier
            .fillMaxSize()
            .consumeWindowInsets(PaddingValues(bottom = fabSpacing)),
        contentAlignment = Alignment.TopCenter
    ) {
        when (uiState.listView) {
            ListView.LIST -> {
                CharactersList(
                    uiState = uiState,
                    actions = actions,
                    onExportPdf = onExportPdf,
                    animatedVisibilityScope = animatedVisibilityScope,
                    fabSpacing = fabSpacing
                )
            }

            ListView.GRID -> {
                CharactersGrid(
                    uiState = uiState,
                    actions = actions,
                    onExportPdf = onExportPdf,
                    animatedVisibilityScope = animatedVisibilityScope,
                    fabSpacing = fabSpacing
                )
            }
        }
    }
}

@Composable
fun SharedTransitionScope.CharactersList(
    uiState: CharacterListUiState,
    actions: CharacterListActions,
    onExportPdf: (Character) -> Unit,
    fabSpacing: Dp,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(
            start = 8.dp,
            top = 8.dp,
            end = 8.dp,
            bottom = 8.dp + fabSpacing
        ),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.widthIn(max = 840.dp)
    ) {
        itemsIndexed(uiState.characters, key = { _, item -> item.id }) { index, character ->
            val topCorners = if (uiState.characters.size == 1 || index == 0) 16.dp else 4.dp
            val bottomCorners =
                if (uiState.characters.size == 1 || index == uiState.characters.lastIndex) 16.dp else 4.dp

            val itemActions = rememberCharacterItemActions(character, uiState, actions, onExportPdf)

            CharacterListItem(
                character = character,
                defaultTopCorners = topCorners,
                defaultBottomCorners = bottomCorners,
                isSelected = uiState.selectedCharacters.contains(character),
                isSelectionMode = uiState.isSelectionMode,
                itemActions = itemActions,
                animatedVisibilityScope = animatedVisibilityScope,
                modifier = Modifier.animateItem()
            )
        }
    }
}

@Composable
fun SharedTransitionScope.CharactersGrid(
    uiState: CharacterListUiState,
    actions: CharacterListActions,
    onExportPdf: (Character) -> Unit,
    fabSpacing: Dp,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier
) {
    val gridState = rememberLazyStaggeredGridState()
    val columnCount = when {
        uiState.windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> 3
        uiState.windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> 3
        else -> 2
    }

    LazyVerticalStaggeredGrid(
        state = gridState,
        columns = StaggeredGridCells.Fixed(columnCount),
        contentPadding = PaddingValues(
            start = 8.dp,
            top = 8.dp,
            end = 8.dp,
            bottom = 8.dp + fabSpacing
        ),
        verticalItemSpacing = 4.dp,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.widthIn(max = 840.dp)
    ) {
        items(uiState.characters, key = { it.id }) { character ->
            val itemActions = rememberCharacterItemActions(character, uiState, actions, onExportPdf)
            CharacterGridItem(
                character = character,
                isSelected = uiState.selectedCharacters.contains(character),
                isSelectionMode = uiState.isSelectionMode,
                itemActions = itemActions,
                animatedVisibilityScope = animatedVisibilityScope,
                modifier = Modifier.animateItem(
                    fadeInSpec = tween(600),
                    placementSpec = null,
                    fadeOutSpec = tween(200)
                )
            )
        }
    }
}

@Composable
fun rememberCharacterItemActions(
    character: Character,
    uiState: CharacterListUiState,
    actions: CharacterListActions,
    onExportPdf: (Character) -> Unit
): CharacterItemActions {
    return remember(character, uiState.isSelectionMode, actions, onExportPdf) {
        CharacterItemActions(
            onClick = {
                if (uiState.isSelectionMode) actions.toggleSelection(character)
                else actions.onCharacterClick(character.id)
            },
            onLongClick = {
                actions.toggleSelection(character)
            },
            onToggleSelection = { actions.toggleSelection(character) },
            onDelete = { actions.onDelete(character) },
            onExport = { onExportPdf(character) }
        )
    }
}

@Composable
fun SharedTransitionScope.CharacterListItem(
    character: Character,
    defaultTopCorners: Dp,
    defaultBottomCorners: Dp,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    itemActions: CharacterItemActions,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier
) {
    val animatedContainerColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.secondaryContainer
        else MaterialTheme.colorScheme.surfaceVariant,
        label = "cardColorAnimation"
    )

    val animatedBorderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onSurfaceVariant
        else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "cardColorAnimation"
    )

    val animatedBorderWidth by animateDpAsState(
        targetValue = if (isSelected) 1.5.dp else 0.7.dp,
        label = "cardBorderWidth"
    )

    val topCorners by animateDpAsState(
        targetValue = if (isSelected) 16.dp else defaultTopCorners,
        label = "topCornersAnimation"
    )
    val bottomCorners by animateDpAsState(
        targetValue = if (isSelected) 16.dp else defaultBottomCorners,
        label = "bottomCornersAnimation"
    )

    val animatedShape = RoundedCornerShape(
        topStart = topCorners,
        topEnd = topCorners,
        bottomStart = bottomCorners,
        bottomEnd = bottomCorners
    )

    val imageShape = RoundedCornerShape(
        topStart = topCorners,
        bottomStart = bottomCorners,
        topEnd = 0.dp,
        bottomEnd = 0.dp
    )

    OutlinedCard (
        colors = CardDefaults.cardColors(containerColor = animatedContainerColor),
        shape = animatedShape,
        border = BorderStroke(
            width = animatedBorderWidth,
            color = animatedBorderColor
        ),
        modifier = modifier
            .heightIn(min = 120.dp)
            .wrapContentHeight()
            .clip(animatedShape)
            .combinedClickable(
                onClick = itemActions.onClick,
                onLongClick = itemActions.onLongClick
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            CharacterItemImage(
                imagePath = character.imagePath,
                shape = imageShape,
                modifier = Modifier
                    .size(120.dp)
                    .sharedBounds(
                        sharedContentState = rememberSharedContentState(key = "image_${character.id}"),
                        animatedVisibilityScope = animatedVisibilityScope,
                        resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
                        clipInOverlayDuringTransition = OverlayClip(imageShape)
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                Row {
                    Text(
                        text = character.name,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically)
                            .sharedBounds(
                                sharedContentState = rememberSharedContentState(key = "name_${character.id}"),
                                animatedVisibilityScope = animatedVisibilityScope,
                                resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
                            )
                    )
                    CharacterItemTrailingAction(
                        isSelected = isSelected,
                        isSelectionMode = isSelectionMode,
                        characterName = character.name,
                        onToggleSelection = itemActions.onToggleSelection,
                        onExport = itemActions.onExport,
                        onDelete = itemActions.onDelete,
                        modifier = Modifier.weight(0.2f)
                    )
                }
                CharacterMetadata(
                    race = character.race,
                    charClass = character.charClass,
                    level = character.level,
                    classRaceModifier = Modifier.sharedBounds(
                        sharedContentState = rememberSharedContentState(key = "class_${character.id}"),
                        animatedVisibilityScope = animatedVisibilityScope,
                        resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                    )
                )
            }
        }
    }
}

@Composable
fun SharedTransitionScope.CharacterGridItem(
    character: Character,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    itemActions: CharacterItemActions,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier
) {
    val animatedContainerColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.secondaryContainer
        else MaterialTheme.colorScheme.surfaceVariant,
        label = "cardColorAnimation"
    )

    val animatedBorderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onSurfaceVariant
        else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "cardColorAnimation"
    )

    val animatedBorderWidth by animateDpAsState(
        targetValue = if (isSelected) 1.5.dp else 0.7.dp,
        label = "cardBorderWidth"
    )

    val imageShape = RoundedCornerShape(
        topStart = 8.dp,
        topEnd = 8.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )

    OutlinedCard (
        colors = CardDefaults.cardColors(containerColor = animatedContainerColor),
        border = BorderStroke(
            width = animatedBorderWidth,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .combinedClickable(
                onClick = itemActions.onClick,
                onLongClick = itemActions.onLongClick
            )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box {
                CharacterItemImage(
                    imagePath = character.imagePath,
                    shape = imageShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 180.dp)
                        .sharedBounds(
                            sharedContentState = rememberSharedContentState(key = "image_${character.id}"),
                            animatedVisibilityScope = animatedVisibilityScope,
                            resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
                            clipInOverlayDuringTransition = OverlayClip(imageShape)
                        ),
                )
                CharacterItemTrailingAction(
                    isSelected = isSelected,
                    isSelectionMode = isSelectionMode,
                    characterName = character.name,
                    onToggleSelection = itemActions.onToggleSelection,
                    onExport = itemActions.onExport,
                    onDelete = itemActions.onDelete,
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                Text(
                    text = character.name,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.sharedBounds(
                        sharedContentState = rememberSharedContentState(key = "name_${character.id}"),
                        animatedVisibilityScope = animatedVisibilityScope,
                        resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                    )
                )
                CharacterMetadata(
                    race = character.race,
                    charClass = character.charClass,
                    level = character.level,
                    classRaceModifier = Modifier.sharedBounds(
                        sharedContentState = rememberSharedContentState(key = "class_${character.id}"),
                        animatedVisibilityScope = animatedVisibilityScope,
                        resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                    )
                )
            }
        }
    }
}

@Composable
fun CharacterItemImage(
    imagePath: String?,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape? = null
) {
    Surface(
        modifier = modifier.then(if (shape != null) Modifier.clip(shape) else Modifier),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        if (imagePath != null) {
            AsyncImage(
                model = imagePath,
                contentDescription = "Character Profile",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Fallback profile",
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxSize()
            )
        }
    }
}

@Composable
fun CharacterItemTrailingAction(
    isSelected: Boolean,
    isSelectionMode: Boolean,
    characterName: String,
    onToggleSelection: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (isSelectionMode) {
        Box(
            modifier = modifier
        ) {
            Checkbox(checked = isSelected, onCheckedChange = { onToggleSelection() })
        }
    } else {
        var menuExpanded by remember { mutableStateOf(false) }
        var showConfirmDialog by remember { mutableStateOf(false) }

        if (showConfirmDialog) {
            DeletingItemConfirmDialog(
                title = stringResource(R.string.q_delete_character),
                text = stringResource(R.string.q_confirm_text, characterName),
                onConfirm = onDelete,
                onDiscard = { showConfirmDialog = false },
            )
        }

        Box(
            modifier = modifier
        ) {
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(Icons.Default.MoreVert, contentDescription = "Options")
            }
            SlicedDropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                items = listOf(
                    SlicedMenuItem(
                        text = stringResource(R.string.share_as_pdf),
                        icon = Icons.Default.Share,
                        onClick = onExport
                    ),
                    SlicedMenuItem(
                        text = stringResource(R.string.delete),
                        icon = Icons.Default.Delete,
                        contentColor = MaterialTheme.colorScheme.error,
                        onClick = { showConfirmDialog = true })
                )
            )
        }
    }
}

@Composable
fun CharacterMetadata(
    race: String,
    charClass: String,
    level: Int,
    modifier: Modifier = Modifier,
    classRaceModifier: Modifier,
) {
    val characterInfo = remember(race, charClass) {
        listOf(race, charClass).filter { it.isNotBlank() }.joinToString(" — ")
    }
    if (characterInfo.isNotBlank()) {
        Text(
            text = characterInfo,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = classRaceModifier
        )
    }
    Text(
        text = "${stringResource(R.string.spell_level)} $level",
        style = MaterialTheme.typography.labelLarge
    )
}

@PreviewLightDark
@PreviewDynamicColors
@Composable
private fun ListOfCharactersRoutePreview_LIST() {
    DnDSheetTheme {
        SharedTransitionLayout {
            AnimatedVisibility(true) {
                ListOfCharactersScreen(
                    uiState = CharacterListUiState(
                        characters = UiUtils.sampleCharacters,
                        loadingState = false,
                        searchQuery = "",
                        listView = ListView.LIST,
                        selectedCharacters = setOf(UiUtils.sampleCharacters.first()),
                        windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
                    ),
                    actions = CharacterListActions(),
                    animatedVisibilityScope = this
                )
            }
        }
    }
}