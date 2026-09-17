package com.yablonskyi.compendium.races.ui

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
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
import androidx.compose.ui.graphics.graphicsLayer
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
import com.yablonskyi.compendium.races.utils.PreviewUtils
import com.yablonskyi.compendium.races.viewmodel.RaceUiState
import com.yablonskyi.compendium.races.viewmodel.RacesIntent
import com.yablonskyi.model.rulebook.Race
import com.yablonskyi.model.rulebook.RaceSize
import com.yablonskyi.ui.R
import com.yablonskyi.ui.animation.utils.Entrance
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
fun RacesScreen(
    uiState: RaceUiState,
    snackbarHostState: SnackbarHostState,
    onIntent: (RacesIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isFabExpanded by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.isSelectionMode) {
        if (uiState.isSelectionMode) isFabExpanded = false
    }
    var isOrigExpanded by rememberSaveable { mutableStateOf(true) }
    var isHomebrewExpanded by rememberSaveable { mutableStateOf(true) }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            RacesTopAppBar(
                uiState = uiState,
                onIntent = onIntent,
            )
        },
    ) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            val visibleListIsEmpty = if (uiState.isSelectionMode) {
                uiState.homebrewRaces.isEmpty()
            } else {
                uiState.listIsEmpty
            }

            AnimatedVisibility(
                !uiState.isSelectionMode,
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
                    onLoad = { onIntent(RacesIntent.RequestFilePicker) },
                    onSave = {
                        isHomebrewExpanded = true
                        onIntent(RacesIntent.EnterSelectionMode)
                    },
                    onCreate = { onIntent(RacesIntent.CreateRace) },
                    saveEnabled = uiState.homebrewRaces.isNotEmpty(),
                )
            }
            SelectionBottomBar(
                title = stringResource(R.string.q_delete_race),
                confirmMsg = pluralStringResource(
                    R.plurals.q_confirm_text_races,
                    uiState.selectedRaceIds.size,
                    uiState.selectedRaceIds.size,
                ),
                isSelectionMode = uiState.isSelectionMode,
                isAllSelected = uiState.isAllSelected,
                actionsEnabled = uiState.selectedRaceIds.isNotEmpty(),
                onExportSelected = { onIntent(RacesIntent.ExportAllSelected) },
                onDeleteSelected = { onIntent(RacesIntent.DeleteSelected) },
                onToggleSelectAll = { onIntent(RacesIntent.ToggleSelectAll) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .zIndex(1f)
            )

            if (uiState.isLoading || visibleListIsEmpty) {
                LibraryFeedback(
                    isLoading = uiState.isLoading,
                    isEmpty = visibleListIsEmpty,
                    hasSearch = uiState.searchQuery.isNotBlank(),
                    onClearSearch = { onIntent(RacesIntent.SearchQueryChanged("")) },
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = Dimens.Spacing.Small,
                        top = Dimens.Spacing.Small,
                        end = Dimens.Spacing.Small,
                        bottom = Dimens.Fab.BottomPadding
                    ),
                    verticalArrangement = Arrangement.spacedBy(Dimens.Spacing.XSmall),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .widthIn(max = Dimens.Content.MaxWidth)
                        .fillMaxSize()
                ) {
                    if (uiState.origRaces.isNotEmpty() && !uiState.isSelectionMode) {
                        collapsibleRacesList(
                            header = R.string.player_handbook,
                            races = uiState.origRaces,
                            selectedRaceIds = emptySet(),
                            isSelectionMode = false,
                            isHomebrew = false,
                            onToggleSelection = {},
                            onShare = {},
                            onDelete = {},
                            onEdit = {},
                            onDetails = { onIntent(RacesIntent.Details(it)) },
                            isExpanded = isOrigExpanded,
                            onCollapse = { isOrigExpanded = !isOrigExpanded }
                        )
                    }

                    if (uiState.homebrewRaces.isNotEmpty()) {
                        collapsibleRacesList(
                            header = R.string.homebrew,
                            races = uiState.homebrewRaces,
                            selectedRaceIds = uiState.selectedRaceIds,
                            isSelectionMode = uiState.isSelectionMode,
                            isHomebrew = true,
                            onToggleSelection = { onIntent(RacesIntent.ToggleSelection(it)) },
                            onShare = { onIntent(RacesIntent.ShareRequested(it.id)) },
                            onDelete = { onIntent(RacesIntent.Delete(it)) },
                            onDetails = { onIntent(RacesIntent.Details(it)) },
                            onEdit = { onIntent(RacesIntent.Edit(it)) },
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
fun RacesTopAppBar(
    uiState: RaceUiState,
    onIntent: (RacesIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    var isSearchExpanded by remember { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }
    val topBarColor by animateColorAsState(
        targetValue = if (uiState.isSelectionMode) MaterialTheme.colorScheme.surface
        else MaterialTheme.colorScheme.surfaceColorAtElevation(Dimens.TopBar.Elevation)
    )

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val isWideScreen =
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

    BackHandler(enabled = uiState.isSelectionMode || isSearchExpanded) {
        when {
            uiState.isSelectionMode -> onIntent(RacesIntent.ClearSelection)
            isSearchExpanded -> {
                isSearchExpanded = false
                onIntent(RacesIntent.SearchQueryChanged(""))
            }
        }
    }

    CenterAlignedTopAppBar(
        modifier = modifier,
        navigationIcon = {
            when {
                uiState.isSelectionMode -> IconButton(
                    onClick = {
                        onIntent(RacesIntent.ClearSelection)
                        isSearchExpanded = false
                        onIntent(RacesIntent.SearchQueryChanged(""))
                    }
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.clear_selection)
                    )
                }

                isSearchExpanded -> IconButton(onClick = {
                    isSearchExpanded = false
                    onIntent(RacesIntent.SearchQueryChanged(""))
                }) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.close_search)
                    )
                }

                else -> IconButton(onClick = { onIntent(RacesIntent.NavigateBack) }) {
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
                            uiState.selectedRaceIds.size
                        )
                    )
                }

                isSearchExpanded -> {
                    TextField(
                        value = uiState.searchQuery,
                        onValueChange = { onIntent(RacesIntent.SearchQueryChanged(it)) },
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
                        text = stringResource(R.string.races),
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
            containerColor = topBarColor,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

fun LazyListScope.collapsibleRacesList(
    @StringRes header: Int,
    races: List<Race>,
    selectedRaceIds: Set<String>,
    isSelectionMode: Boolean,
    isHomebrew: Boolean,
    isExpanded: Boolean,
    onDetails: (String) -> Unit,
    onEdit: (String) -> Unit,
    onShare: (Race) -> Unit,
    onDelete: (Race) -> Unit,
    onToggleSelection: (String) -> Unit,
    onCollapse: () -> Unit,
) {
    stickyHeader(key = header) {
        Entrance {
            HeaderItem(
                title = stringResource(header),
                isExpanded = isExpanded,
                onToggle = onCollapse
            )
        }
    }

    if (isExpanded) {
        itemsIndexed(items = races, key = { _, race -> race.id }) { index, race ->

            val progress = remember(race.id) { Animatable(0f) }

            LaunchedEffect(race.id) {
                progress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = 400,
                        delayMillis = index.coerceAtMost(4) * 100,
                        easing = FastOutSlowInEasing,
                    ),
                )
            }

            RaceCard(
                defaultTopCorners = if (index == 0) 16.dp else 4.dp,
                defaultBottomCorners = if (index == races.lastIndex) 16.dp else 4.dp,
                race = race,
                isSelected = selectedRaceIds.contains(race.id),
                isSelectionMode = isSelectionMode,
                isHomebrew = isHomebrew,
                onClick = {
                    when {
                        isSelectionMode && isHomebrew -> onToggleSelection(race.id)
                        isSelectionMode -> Unit
                        else -> onDetails(race.id)
                    }
                },
                onSelect = {
                    if (isHomebrew) onToggleSelection(race.id)
                },
                onEdit = { onEdit(race.id) },
                onShare = { onShare(race) },
                onDelete = { onDelete(race) },
                onToggleSelection = { onToggleSelection(race.id) },
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

@Composable
fun RaceCard(
    race: Race,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    isHomebrew: Boolean,
    onClick: () -> Unit,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onToggleSelection: () -> Unit,
    modifier: Modifier = Modifier,
    defaultTopCorners: Dp = 16.dp,
    defaultBottomCorners: Dp = 16.dp
) {
    val animatedColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer
        else CardDefaults.outlinedCardColors().containerColor,
        label = "raceCardColor"
    )

    val infoChipBorderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
        else MaterialTheme.colorScheme.outlineVariant,
        label = "infoChipBorderColor"
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
                    text = race.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                )
                if (isHomebrew) {
                    Spacer(Modifier.width(Dimens.Spacing.Medium))
                    RaceCardTrailingAction(
                        raceName = race.name,
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
                if (race.abilityBonuses.isNotEmpty()) {
                    val bonuses = race.abilityBonuses.map { (ability, bonus) ->
                        val name = stringResource(ability.nameRes).take(3).uppercase()
                        "$name\u00A0${if (bonus >= 0) "+" else ""}$bonus"
                    }.joinToString()
                    OutlinedInfoChip(
                        bonuses,
                        borderColor = infoChipBorderColor
                    )
                }
                OutlinedInfoChip(
                    text = stringResource(RaceSize.fromString(race.size).resId),
                    borderColor = infoChipBorderColor
                )
                OutlinedInfoChip(
                    text = "${race.speed} ${stringResource(R.string.feets)}",
                    borderColor = infoChipBorderColor
                )
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
        var showConfirmDialog by rememberSaveable { mutableStateOf(false) }

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
private fun RacesScreenPreview() {
    PreviewThemeWrapper.Preview {
        RacesScreen(
            uiState = PreviewUtils.racesList,
            snackbarHostState = SnackbarHostState(),
            onIntent = {}
        )
    }
}