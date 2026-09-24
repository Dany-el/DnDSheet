package com.yablonskyi.ui.spell

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.window.core.layout.WindowSizeClass
import com.yablonskyi.ui.R
import com.yablonskyi.ui.animation.utils.Entrance
import com.yablonskyi.ui.theme.Dimens
import com.yablonskyi.ui.utils.DeletingItemConfirmDialog
import com.yablonskyi.ui.utils.ExpandableFab
import com.yablonskyi.ui.utils.InfoChip
import com.yablonskyi.ui.utils.PreviewThemeWrapper
import com.yablonskyi.ui.utils.SelectionBottomBar
import com.yablonskyi.ui.utils.SlicedDropdownMenu
import com.yablonskyi.ui.utils.SlicedMenuItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpellLibraryScreen(
    uiState: SpellLibraryState,
    snackbarHostState: SnackbarHostState,
    onIntent: (SpellsIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    var isFabExpanded by remember { mutableStateOf(false) }
    LaunchedEffect(
        uiState.isSelectionMode,
        uiState.isLearnMode,
        uiState.isFilterExpanded,
        uiState.showBottomSheet
    ) {
        if (uiState.isSelectionMode || uiState.isLearnMode || uiState.isFilterExpanded || uiState.showBottomSheet != null) {
            isFabExpanded = false
        }
    }
    val listState = rememberLazyListState()
    val wideFilters =
        currentWindowAdaptiveInfo().windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
    val groupedSpells = uiState.groupedSpells

    BackHandler(enabled = uiState.isSelectionMode) {
        onIntent(SpellsIntent.ClearSelection)
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                SpellLibraryTopBar(
                    uiState = uiState,
                    onIntent = onIntent,
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding),
            ) {
                AnimatedVisibility(
                    !uiState.isSelectionMode && !uiState.isLearnMode && !uiState.isFilterExpanded,
                    enter = fadeIn() + slideInVertically { -it / 2 },
                    exit = fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 24.dp)
                        .zIndex(1f)
                ) {
                    ExpandableFab(
                        expanded = isFabExpanded,
                        onExpandedChange = { isFabExpanded = it },
                        onLoad = { onIntent(SpellsIntent.RequestFilePicker) },
                        onSave = { onIntent(SpellsIntent.EnterSelectionMode) },
                        onCreate = { onIntent(SpellsIntent.AddSpell) },
                        saveEnabled = uiState.spells.isNotEmpty(),
                    )
                }

                SelectionBottomBar(
                    isSelectionMode = uiState.isSelectionMode,
                    isAllSelected = uiState.isAllSelected,
                    actionsEnabled = uiState.selectedSpellIds.isNotEmpty(),
                    onExportSelected = { onIntent(SpellsIntent.ExportAllSelected) },
                    onDeleteSelected = { onIntent(SpellsIntent.DeleteSelected) },
                    onToggleSelectAll = { onIntent(SpellsIntent.ToggleSelectAll) },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .zIndex(1f),
                )

                Column(
                    modifier = Modifier.widthIn(max = Dimens.Content.MaxWidth)
                ) {
                    AnimatedVisibility(uiState.filterState.isActive) {
                        SpellFiltersRow(
                            uiState = uiState,
                            modifier = Modifier.padding(horizontal = Dimens.Spacing.Large)
                        )
                    }

                    if (uiState.isLoading || (uiState.spells.isEmpty())) {
                        LibraryFeedback(
                            isLoading = uiState.isLoading,
                            isEmpty = uiState.spells.isEmpty(),
                            hasSearch = uiState.searchQuery.isNotBlank(),
                            hasFilters = uiState.filterState.isActive,
                            onClearFilters = { onIntent(SpellsIntent.ClearAllFilters) },
                            onClearSearch = { onIntent(SpellsIntent.SearchQueryChanged("")) },
                        )
                    } else {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(
                                start = Dimens.Spacing.Small,
                                top = Dimens.Spacing.Small,
                                end = Dimens.Spacing.Small,
                                bottom = Dimens.Fab.BottomPadding
                            ),
                            verticalArrangement = Arrangement.spacedBy(Dimens.Spacing.XSmall),
                        ) {
                            groupedSpells.forEach { (level, spells) ->
                                stickyHeader {
                                    Entrance {
                                        Surface(
                                            color = MaterialTheme.colorScheme.background,
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.Start,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = Dimens.Spacing.Small)
                                            ) {
                                                Text(
                                                    text = stringResource(level.resId),
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontWeight = FontWeight.SemiBold,
                                                )
                                            }
                                        }
                                    }
                                }
                                itemsIndexed(
                                    items = spells, key = { _, item -> item.spell.spellId }
                                ) { index, item ->

                                    val topCorners =
                                        if (spells.size == 1 || index == 0) 16.dp else 4.dp
                                    val bottomCorners =
                                        if (spells.size == 1 || index == spells.lastIndex) 16.dp else 4.dp

                                    val progress = remember(item.spell.spellId) { Animatable(0f) }

                                    LaunchedEffect(item.spell.spellId) {
                                        progress.animateTo(
                                            targetValue = 1f,
                                            animationSpec = tween(
                                                durationMillis = 300,
                                                delayMillis = index.coerceAtMost(2) * 100,
                                                easing = FastOutSlowInEasing,
                                            ),
                                        )
                                    }

                                    SpellLibraryRow(
                                        item = item,
                                        defaultTopCorners = topCorners,
                                        defaultBottomCorners = bottomCorners,
                                        isLearnMode = uiState.isLearnMode,
                                        isSelected = item.spell.spellId in uiState.selectedSpellIds,
                                        isSelectionMode = uiState.isSelectionMode,
                                        onToggle = { onIntent(SpellsIntent.ToggleSpell(item.spell)) },
                                        onEdit = { onIntent(SpellsIntent.EditSpell(item.spell.spellId)) },
                                        onDelete = { onIntent(SpellsIntent.Delete(item.spell)) },
                                        onShare = { onIntent(SpellsIntent.ShareRequested(item.spell)) },
                                        onToggleSelection = {
                                            onIntent(
                                                SpellsIntent.ToggleSelection(
                                                    item.spell
                                                )
                                            )
                                        },
                                        onShowDetails = { onIntent(SpellsIntent.ShowDetails(item.spell)) },
                                        modifier = Modifier
                                            .animateItem()
                                            .graphicsLayer {
                                                alpha = progress.value
                                                translationY = 24.dp.toPx() * (1f - progress.value)
                                            },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = uiState.isFilterExpanded,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onIntent(SpellsIntent.ToggleFiltersExpanded)
                        }
                )

                SpellFilterScreen(
                    filterState = uiState.filterState,
                    isVisible = uiState.isFilterExpanded,
                    onIntent = onIntent,
                    modifier = Modifier
                        .then(if (wideFilters) Modifier.width(400.dp) else Modifier.fillMaxWidth())
                        .fillMaxHeight()
                        .align(Alignment.CenterEnd)
                        .clickable(enabled = false) {}
                        .animateEnterExit(
                            enter = slideInHorizontally(initialOffsetX = { it }),
                            exit = slideOutHorizontally(targetOffsetX = { it })
                        )
                )
            }
        }
    }

    uiState.showBottomSheet?.let { spell ->
        ModalBottomSheet(
            sheetState = rememberBottomSheetState(
                initialValue = SheetValue.Hidden,
                enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
            ),
            onDismissRequest = { onIntent(SpellsIntent.DismissDetails) }
        ) {
            SpellInfoSheet(
                spell = spell,
                onDismiss = { onIntent(SpellsIntent.DismissDetails) }
            )
        }
    }

    if (uiState.showConfirmDeleteDialog) {
        when (val request = uiState.pendingDelete) {
            is SpellsDeleteRequest.Single -> DeletingItemConfirmDialog(
                title = stringResource(R.string.q_delete_spell),
                text = stringResource(R.string.q_confirm_text, request.spell.name),
                onConfirm = { onIntent(SpellsIntent.ConfirmDelete) },
                onDiscard = { onIntent(SpellsIntent.DismissDelete) }
            )

            SpellsDeleteRequest.Selection -> DeletingItemConfirmDialog(
                title = stringResource(R.string.q_delete_spell),
                text = pluralStringResource(
                    R.plurals.q_confirm_text_spells,
                    uiState.selectedSpellIds.size
                ),
                onConfirm = { onIntent(SpellsIntent.ConfirmDelete) },
                onDiscard = { onIntent(SpellsIntent.DismissDelete) }
            )

            null -> Unit
        }
    }

    uiState.pendingImport?.let { spells ->
        AlertDialog(
            onDismissRequest = { onIntent(SpellsIntent.DismissImport) },
            title = { Text(stringResource(R.string.confirm_import_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.alert_dialog_spell_confirm_msg,
                        spells.size
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = { onIntent(SpellsIntent.ConfirmImport) }
                ) {
                    Text(stringResource(R.string.confirm_import))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onIntent(SpellsIntent.DismissImport) }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun SpellFiltersRow(
    uiState: SpellLibraryState,
    modifier: Modifier = Modifier
) {
    val filterState = uiState.filterState

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.Spacing.Medium)
    ) {
        if (filterState.onlyConcentration) {
            InfoChip(stringResource(R.string.concentration))
        }
        if (filterState.onlyRitual) {
            InfoChip(stringResource(R.string.ritual))
        }
        FilterSummaryChip(
            selected = filterState.levels,
            categoryLabel = stringResource(R.string.spell_level),
            label = { stringResource(it.resId) }
        )
        FilterSummaryChip(
            selected = filterState.schools,
            categoryLabel = stringResource(R.string.msg_school),
            label = { stringResource(it.resId) }
        )
        FilterSummaryChip(
            selected = filterState.castTimes,
            categoryLabel = stringResource(R.string.msg_casting_time),
            label = { stringResource(it.resId) }
        )
        FilterSummaryChip(
            selected = filterState.durations,
            categoryLabel = stringResource(R.string.msg_duration),
            label = { stringResource(it.resId) }
        )
    }
}

@Composable
fun <T> FilterSummaryChip(
    selected: Set<T>,
    categoryLabel: String,
    label: @Composable (T) -> String,
    modifier: Modifier = Modifier
) {
    if (selected.isEmpty()) return

    val text = if (selected.size == 1) {
        "$categoryLabel: ${label(selected.first())}"
    } else {
        "$categoryLabel: ${selected.size}"
    }

    InfoChip(text, modifier)
}

@Composable
fun SpellLibraryRow(
    item: SpellLibraryItem,
    defaultTopCorners: Dp,
    defaultBottomCorners: Dp,
    isLearnMode: Boolean,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onToggleSelection: () -> Unit,
    onShowDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spell = item.spell

    val animatedColor by animateColorAsState(
        targetValue = if (isSelected || (item.isLearned && isLearnMode))
            MaterialTheme.colorScheme.primaryContainer
        else CardDefaults.outlinedCardColors().containerColor,
        label = "cardColorAnimation"
    )

    val topCorners by animateDpAsState(
        targetValue = if (isSelected || (item.isLearned && isLearnMode)) 16.dp else defaultTopCorners,
        label = "topCornersAnimation"
    )

    val bottomCorners by animateDpAsState(
        targetValue = if (isSelected || (item.isLearned && isLearnMode)) 16.dp else defaultBottomCorners,
        label = "bottomCornersAnimation"
    )

    val animatedShape = RoundedCornerShape(
        topStart = topCorners,
        topEnd = topCorners,
        bottomStart = bottomCorners,
        bottomEnd = bottomCorners
    )

    var menuExpanded by remember { mutableStateOf(false) }

    OutlinedCard(
        shape = animatedShape,
        colors = CardDefaults.outlinedCardColors(containerColor = animatedColor),
        modifier = modifier
            .fillMaxWidth()
            .clip(animatedShape)
            .combinedClickable(
                role = Role.Button,
                onClick = {
                    when {
                        isSelectionMode -> onToggleSelection()
                        isLearnMode -> onToggle()
                        else -> onShowDetails()
                    }
                },
                onLongClick = { onToggleSelection() }
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.Spacing.Large, vertical = Dimens.Spacing.Medium)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(spell.school.resId),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = Dimens.Spacing.XSmall)
                )
                Text(
                    text = spell.name,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            when {
                isLearnMode -> Switch(
                    checked = item.isLearned,
                    onCheckedChange = { onToggle() }
                )

                isSelectionMode -> Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelection() }
                )

                else -> Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.options),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    SlicedDropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        items = listOf(
                            SlicedMenuItem(
                                text = stringResource(R.string.edit),
                                icon = Icons.Default.Edit,
                                onClick = { onEdit() }
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
                                onClick = { onDelete() }
                            )
                        )
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun SpellLibraryPreview() {
    PreviewThemeWrapper.Preview {
        SpellLibraryScreen(
            uiState = PreviewUtils.StateProvider.default,
            snackbarHostState = SnackbarHostState(),
            onIntent = {}
        )
    }
}

@Preview(group = "selection_on")
@Composable
private fun SpellLibraryPreview_SELECT_MODE_ON() {
    PreviewThemeWrapper.Preview {
        SpellLibraryScreen(
            uiState = PreviewUtils.selectionMode,
            snackbarHostState = SnackbarHostState(),
            onIntent = {}
        )
    }
}

@Preview(group = "selection_off")
@Composable
private fun SpellLibraryPreview_SELECTION_OFF() {
    PreviewThemeWrapper.Preview {
        SpellLibraryScreen(
            uiState = PreviewUtils.learnMode,
            snackbarHostState = SnackbarHostState(),
            onIntent = {}
        )
    }
}

@Preview(group = "selection_off", locale = "ru")
@Composable
private fun SpellLibraryPreview_SELECTION_OFF_RU() {
    PreviewThemeWrapper.Preview {
        SpellLibraryScreen(
            uiState = PreviewUtils.learnMode,
            snackbarHostState = SnackbarHostState(),
            onIntent = {}
        )
    }
}

@Preview(group = "selection_off", locale = "uk")
@Composable
private fun SpellLibraryPreview_SELECTION_OFF_UK() {
    PreviewThemeWrapper.Preview {
        SpellLibraryScreen(
            uiState = PreviewUtils.learnMode,
            snackbarHostState = SnackbarHostState(),
            onIntent = {}
        )
    }
}