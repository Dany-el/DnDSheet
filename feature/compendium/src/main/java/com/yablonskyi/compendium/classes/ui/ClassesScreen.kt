package com.yablonskyi.compendium.classes.ui

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.window.core.layout.WindowSizeClass
import com.yablonskyi.compendium.classes.utils.PreviewUtils
import com.yablonskyi.compendium.classes.viewmodel.ClassUiState
import com.yablonskyi.compendium.classes.viewmodel.ClassesIntent
import com.yablonskyi.model.rulebook.CharacterClass
import com.yablonskyi.ui.R
import com.yablonskyi.ui.spell.LibraryFeedback
import com.yablonskyi.ui.theme.Dimens
import com.yablonskyi.ui.utils.DeletingItemConfirmDialog
import com.yablonskyi.ui.utils.ExpandableFab
import com.yablonskyi.ui.utils.HeaderItem
import com.yablonskyi.ui.utils.OutlinedInfoChip
import com.yablonskyi.ui.utils.PreviewThemeWrapper
import com.yablonskyi.ui.utils.SelectionBottomBar
import com.yablonskyi.ui.utils.SlicedDropdownMenu
import com.yablonskyi.ui.utils.SlicedMenuItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassesScreen(
    uiState: ClassUiState,
    snackbarHostState: SnackbarHostState,
    onIntent: (ClassesIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isFabExpanded by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.isSelectionMode) {
        if (uiState.isSelectionMode) isFabExpanded = false
    }
    var isOrigExpanded by rememberSaveable { mutableStateOf(true) }
    var isHomebrewExpanded by rememberSaveable { mutableStateOf(true) }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            ClassesTopAppBar(
                uiState = uiState,
                onIntent = onIntent,
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            SelectionBottomBar(
                title = stringResource(R.string.q_delete_class),
                confirmMsg = pluralStringResource(
                    R.plurals.q_confirm_text_classes,
                    uiState.selectedClassesIds.size,
                    uiState.selectedClassesIds.size,
                ),
                isSelectionMode = uiState.isSelectionMode,
                isAllSelected = uiState.isAllSelected,
                actionsEnabled = uiState.selectedClassesIds.isNotEmpty(),
                onExportSelected = { onIntent(ClassesIntent.ExportAllSelected) },
                onDeleteSelected = { onIntent(ClassesIntent.DeleteSelected) },
                onToggleSelectAll = { onIntent(ClassesIntent.ToggleSelectAll) }
            )
        }
    ) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            if (!uiState.isSelectionMode) {
                ExpandableFab(
                    expanded = isFabExpanded,
                    onExpandedChange = { isFabExpanded = it },
                    onLoad = { onIntent(ClassesIntent.RequestFilePicker) },
                    onSave = {
                        isHomebrewExpanded = true
                        onIntent(ClassesIntent.EnterSelectionMode)
                    },
                    onCreate = { onIntent(ClassesIntent.CreateClass) },
                    saveEnabled = uiState.homebrewClasses.isNotEmpty(),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 24.dp)
                        .zIndex(1f)
                )
            }
            if (uiState.isLoading || ((uiState.isSelectionMode || uiState.origClasses.isEmpty()) && uiState.homebrewClasses.isEmpty())) {
                LibraryFeedback(
                    isLoading = uiState.isLoading,
                    hasSearch = uiState.searchQuery.isNotBlank(),
                    onClearSearch = { onIntent(ClassesIntent.SearchQueryChanged("")) },
                    onCreate = if (!uiState.isSelectionMode) ({ onIntent(ClassesIntent.CreateClass) }) else null,
                    onImport = if (!uiState.isSelectionMode) ({ onIntent(ClassesIntent.RequestFilePicker) }) else null
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = Dimens.Spacing.Small,
                        end = Dimens.Spacing.Small,
                        top = Dimens.Spacing.Medium,
                        bottom = if (uiState.isSelectionMode) Dimens.Spacing.Small else Dimens.Fab.BottomPadding
                    ),
                    verticalArrangement = Arrangement.spacedBy(Dimens.Spacing.XSmall),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .widthIn(max = Dimens.Content.MaxWidth)
                        .fillMaxSize()
                ) {
                    if (uiState.origClasses.isNotEmpty() && !uiState.isSelectionMode) {
                        collapsibleClassesList(
                            header = R.string.player_handbook,
                            classes = uiState.origClasses,
                            selectedClassesIds = emptySet(),
                            isSelectionMode = false,
                            isHomebrew = false,
                            onToggleSelection = {},
                            onShare = {},
                            onDelete = {},
                            onEdit = {},
                            onDetails = { onIntent(ClassesIntent.Details(it)) },
                            isExpanded = isOrigExpanded,
                            onCollapse = { isOrigExpanded = !isOrigExpanded }
                        )
                    }

                    if (uiState.homebrewClasses.isNotEmpty()) {
                        collapsibleClassesList(
                            header = R.string.homebrew,
                            classes = uiState.homebrewClasses,
                            selectedClassesIds = uiState.selectedClassesIds,
                            isSelectionMode = uiState.isSelectionMode,
                            isHomebrew = true,
                            onToggleSelection = { onIntent(ClassesIntent.ToggleSelection(it)) },
                            onShare = { onIntent(ClassesIntent.ShareRequested(it.id)) },
                            onEdit = { onIntent(ClassesIntent.Edit(it)) },
                            onDelete = { onIntent(ClassesIntent.Delete(it)) },
                            onDetails = { onIntent(ClassesIntent.Details(it)) },
                            isExpanded = isHomebrewExpanded,
                            onCollapse = { isHomebrewExpanded = !isHomebrewExpanded }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassesTopAppBar(
    uiState: ClassUiState,
    onIntent: (ClassesIntent) -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier
) {
    var isSearchExpanded by remember { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }
    val elevatedSurfaceColor =
        MaterialTheme.colorScheme.surfaceColorAtElevation(Dimens.TopBar.Elevation)

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val isWideScreen =
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

    BackHandler(enabled = uiState.isSelectionMode || isSearchExpanded) {
        when {
            uiState.isSelectionMode -> onIntent(ClassesIntent.ClearSelection)
            isSearchExpanded -> {
                isSearchExpanded = false
                onIntent(ClassesIntent.SearchQueryChanged(""))
            }
        }
    }

    CenterAlignedTopAppBar(
        modifier = modifier,
        scrollBehavior = if (!isSearchExpanded && !uiState.isSelectionMode) scrollBehavior else null,
        navigationIcon = {
            when {
                uiState.isSelectionMode -> IconButton(onClick = {
                    onIntent(ClassesIntent.ClearSelection)
                    isSearchExpanded = false
                    onIntent(ClassesIntent.SearchQueryChanged(""))
                }) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.clear_selection)
                    )
                }

                isSearchExpanded -> IconButton(onClick = {
                    isSearchExpanded = false
                    onIntent(ClassesIntent.SearchQueryChanged(""))
                }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.close_search)
                    )
                }

                else -> IconButton(onClick = { onIntent(ClassesIntent.NavigateBack) }) {
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
                            uiState.selectedClassesIds.size
                        )
                    )
                }

                isSearchExpanded -> {
                    TextField(
                        value = uiState.searchQuery,
                        onValueChange = { onIntent(ClassesIntent.SearchQueryChanged(it)) },
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

                else -> {
                    Text(
                        text = stringResource(R.string.classes),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
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

        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = elevatedSurfaceColor,
            scrolledContainerColor = elevatedSurfaceColor,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

fun LazyListScope.collapsibleClassesList(
    @StringRes header: Int,
    classes: List<CharacterClass>,
    selectedClassesIds: Set<String>,
    isSelectionMode: Boolean,
    isHomebrew: Boolean,
    isExpanded: Boolean,
    onDetails: (String) -> Unit,
    onEdit: (String) -> Unit,
    onShare: (CharacterClass) -> Unit,
    onDelete: (CharacterClass) -> Unit,
    onToggleSelection: (String) -> Unit,
    onCollapse: () -> Unit,
) {
    stickyHeader(key = header) {
        HeaderItem(
            title = stringResource(header),
            isExpanded = isExpanded,
            onToggle = onCollapse
        )
    }

    if (isExpanded) {
        itemsIndexed(items = classes, key = { _, cls -> cls.id }) { index, cls ->

            ClassCard(
                defaultTopCorners = if (index == 0) 16.dp else 4.dp,
                defaultBottomCorners = if (index == classes.lastIndex) 16.dp else 4.dp,
                characterClass = cls,
                isSelected = selectedClassesIds.contains(cls.id),
                isSelectionMode = isSelectionMode,
                isHomebrew = isHomebrew,
                onClick = {
                    when {
                        isSelectionMode && isHomebrew -> onToggleSelection(cls.id)
                        isSelectionMode -> Unit
                        else -> onDetails(cls.id)
                    }
                },
                onSelect = {
                    if (isHomebrew) onToggleSelection(cls.id)
                },
                onEdit = { onEdit(cls.id) },
                onShare = { onShare(cls) },
                onDelete = { onDelete(cls) },
                onToggleSelection = { onToggleSelection(cls.id) },
                modifier = Modifier.animateItem()
            )
        }
    }
}

@Composable
fun ClassCard(
    characterClass: CharacterClass,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    isHomebrew: Boolean,
    onClick: () -> Unit,
    onSelect: () -> Unit,
    onShare: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleSelection: () -> Unit,
    modifier: Modifier = Modifier,
    defaultTopCorners: Dp = 16.dp,
    defaultBottomCorners: Dp = 16.dp
) {
    val animatedColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer
        else CardDefaults.outlinedCardColors().containerColor,
        label = "classCardColor"
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

    OutlinedCard(
        shape = animatedShape,
        colors = CardDefaults.outlinedCardColors(containerColor = animatedColor),
        modifier = modifier
            .fillMaxWidth()
            .clip(animatedShape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    if (isHomebrew) {
                        onSelect()
                    }
                }
            )
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.Spacing.Small),
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.Spacing.Large)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = characterClass.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                )
                if (isHomebrew) {
                    ClassCardTrailingAction(
                        className = characterClass.name,
                        isSelected = isSelected,
                        isSelectionMode = isSelectionMode,
                        onToggleSelection = onToggleSelection,
                        onShare = onShare,
                        onDelete = onDelete,
                        onEdit = onEdit
                    )
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(Dimens.Spacing.XSmall)
            ) {
                OutlinedInfoChip(text = characterClass.hitDice)
                OutlinedInfoChip(
                    text = pluralStringResource(
                        R.plurals.wizard_skill_choices,
                        characterClass.skillChoiceCount,
                        characterClass.skillChoiceCount
                    )
                )
                if (characterClass.savingThrows.isNotEmpty()) {
                    val saves = characterClass.savingThrows.map {
                        stringResource(it.nameRes).take(3).uppercase()
                    }.joinToString(", ")
                    OutlinedInfoChip(stringResource(R.string.wizard_saving_throws, saves))
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
    onShare: () -> Unit,
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
                Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.options))
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
                        text = stringResource(R.string.share),
                        icon = Icons.Default.Share,
                        onClick = onShare
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

@Preview
@Composable
private fun ClassesScreenPreview() {
    PreviewThemeWrapper.Preview {
        ClassesScreen(
            uiState = PreviewUtils.classesList,
            snackbarHostState = SnackbarHostState(),
            onIntent = {}
        )
    }
}