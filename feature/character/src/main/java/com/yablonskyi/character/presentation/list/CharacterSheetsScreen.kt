package com.yablonskyi.character.presentation.list

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewDynamicColors
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.window.core.layout.WindowSizeClass
import com.yablonskyi.character.platform.print.normalizePrintLanguage
import com.yablonskyi.character.presentation.common.UiUtils
import com.yablonskyi.character.presentation.list.components.CharacterGridItem
import com.yablonskyi.character.presentation.list.components.CharacterListItem
import com.yablonskyi.character.presentation.list.components.CharactersTopAppBar
import com.yablonskyi.model.character.Character
import com.yablonskyi.ui.R
import com.yablonskyi.ui.settings.ListView
import com.yablonskyi.ui.theme.Dimens
import com.yablonskyi.ui.theme.DnDSheetTheme
import com.yablonskyi.ui.utils.ExpandableFab
import com.yablonskyi.ui.utils.LoadingDialog
import com.yablonskyi.ui.utils.SelectionBottomBar
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import sh.calvin.reorderable.rememberReorderableLazyStaggeredGridState

@Stable
data class CharacterItemActions(
    val onClick: () -> Unit,
    val onLongClick: () -> Unit,
    val onToggleSelection: () -> Unit,
    val onDelete: () -> Unit,
    val onExport: () -> Unit,
    val onReorderFinished: () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedTransitionScope.CharacterSheetsScreen(
    uiState: CharacterListState,
    onIntent: (CharacterListIntent) -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
    listView: ListView = ListView.LIST,
) {
    val configuration = LocalConfiguration.current

    val onExportPdf: (Character) -> Unit = { character ->
        if (!uiState.isPrinting) {
            val language = normalizePrintLanguage(configuration.locales[0]?.language.orEmpty())
            onIntent(CharacterListIntent.PrintClicked(character.id, language))
        }
    }

    val windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val isWideScreen =
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
    val screenInsets =
        if (isWideScreen) ScaffoldDefaults.contentWindowInsets else WindowInsets(bottom = 0.dp)

    Scaffold(
        contentWindowInsets = screenInsets,
        modifier = modifier,
        topBar = {
            CharactersTopAppBar(
                listView = listView,
                uiState = uiState,
                onIntent = onIntent,
            )
        },
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
        } else {
            CharactersContent(
                listView = listView,
                uiState = uiState,
                onIntent = onIntent,
                animatedVisibilityScope = animatedVisibilityScope,
                onExportPdf = onExportPdf,
                modifier = Modifier.padding(padding)
            )
        }
    }

    if (uiState.operation in setOf(
            CharacterListOperation.IMPORT,
            CharacterListOperation.EXPORT,
            CharacterListOperation.DELETE
        ) || uiState.isPrinting
    ) LoadingDialog()

    uiState.error?.let { error ->
        AlertDialog(
            onDismissRequest = { onIntent(CharacterListIntent.DismissError) },
            title = { Text(stringResource(R.string.character_operation_failed)) },
            text = {
                Text(
                    stringResource(
                        when (error) {
                            CharacterListError.LOAD -> R.string.character_load_failed
                            CharacterListError.REORDER -> R.string.character_reorder_failed
                            CharacterListError.IMPORT -> R.string.failure_import
                            CharacterListError.EMPTY_IMPORT -> R.string.import_file_empty
                            CharacterListError.EXPORT -> R.string.failure_export
                            CharacterListError.DELETE -> R.string.character_delete_failed
                            CharacterListError.EMPTY_SELECTION -> R.string.select_at_least_one_item
                        }
                    )
                )
            },
            confirmButton = {
                if (error != CharacterListError.EMPTY_IMPORT && error != CharacterListError.EMPTY_SELECTION) {
                    TextButton(onClick = { onIntent(CharacterListIntent.Retry) }) {
                        Text(stringResource(R.string.character_retry))
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { onIntent(CharacterListIntent.DismissError) }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    uiState.pendingImport?.takeIf { uiState.error == null && uiState.operation == null }
        ?.let { sheets ->
            AlertDialog(
                onDismissRequest = { onIntent(CharacterListIntent.ImportDismissed) },
                title = { Text(stringResource(R.string.confirm_import_title)) },
                text = { Text(stringResource(R.string.alert_dialog_import_chars, sheets.size)) },
                confirmButton = {
                    TextButton(onClick = {
                        onIntent(CharacterListIntent.ImportConfirmed)
                    }) { Text(stringResource(R.string.confirm_import)) }
                },
                dismissButton = {
                    TextButton(onClick = {
                        onIntent(CharacterListIntent.ImportDismissed)
                    }) { Text(stringResource(R.string.cancel)) }
                }
            )
        }
}

@Composable
fun SharedTransitionScope.CharactersContent(
    uiState: CharacterListState,
    modifier: Modifier = Modifier,
    listView: ListView = ListView.LIST,
    onIntent: (CharacterListIntent) -> Unit,
    onExportPdf: (Character) -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val fabSpacing = 72.dp
    var isFabExpanded by remember { mutableStateOf(false) }

    val lazyListState = rememberLazyListState()
    val lazyGridState = rememberLazyStaggeredGridState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .consumeWindowInsets(PaddingValues(bottom = fabSpacing)),
        contentAlignment = Alignment.TopCenter
    ) {
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
                onLoad = { onIntent(CharacterListIntent.ImportClicked) },
                onSave = { onIntent(CharacterListIntent.EnterSelectionMode) },
                onCreate = { onIntent(CharacterListIntent.CreateClicked) },
                saveEnabled = uiState.characters.isNotEmpty(),
            )
        }
        SelectionBottomBar(
            title = stringResource(R.string.q_delete_character),
            confirmMsg = pluralStringResource(
                R.plurals.q_confirm_text_characters,
                uiState.selectedIds.size,
                uiState.selectedIds.size
            ),
            isSelectionMode = uiState.isSelectionMode,
            isAllSelected = uiState.isAllSelected,
            actionsEnabled = uiState.selectedIds.isNotEmpty(),
            onExportSelected = { onIntent(CharacterListIntent.ExportClicked) },
            onDeleteSelected = { onIntent(CharacterListIntent.DeleteSelectedConfirmed) },
            onToggleSelectAll = { onIntent(CharacterListIntent.SelectAllClicked) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(1f)
        )
        AnimatedContent(
            targetState = listView,
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
            transitionSpec = {
                (
                        fadeIn(tween(220, delayMillis = 90)) togetherWith
                                fadeOut(tween(90))
                        ).using(null)
            },
            label = "CharacterListGridTransition",
        ) { targetView ->
            when (targetView) {
                ListView.LIST -> {
                    CharactersList(
                        uiState = uiState,
                        lazyListState = lazyListState,
                        onIntent = onIntent,
                        onExportPdf = onExportPdf,
                        animatedVisibilityScope = animatedVisibilityScope,
                    )
                }

                ListView.GRID -> {
                    CharactersGrid(
                        uiState = uiState,
                        lazyGridState = lazyGridState,
                        onIntent = onIntent,
                        onExportPdf = onExportPdf,
                        animatedVisibilityScope = animatedVisibilityScope,
                        fabSpacing = fabSpacing,
                    )
                }
            }
        }
    }
}

@Composable
fun SharedTransitionScope.CharactersList(
    uiState: CharacterListState,
    lazyListState: LazyListState,
    onIntent: (CharacterListIntent) -> Unit,
    onExportPdf: (Character) -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    val reorderState = rememberReorderableLazyListState(lazyListState) { from, to ->
        onIntent(CharacterListIntent.MoveCharacter(from.key as Long, to.key as Long))
        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
    }
    LazyColumn(
        state = lazyListState,
        contentPadding = PaddingValues(
            start = 8.dp,
            top = 8.dp,
            end = 8.dp,
            bottom = Dimens.Fab.BottomPadding
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.widthIn(max = 840.dp)
    ) {
        itemsIndexed(uiState.characters, key = { _, item -> item.id }) { index, character ->
            val topCorners = if (uiState.characters.size == 1 || index == 0) 16.dp else 4.dp
            val bottomCorners =
                if (uiState.characters.size == 1 || index == uiState.characters.lastIndex) 16.dp else 4.dp

            val itemActions =
                rememberCharacterItemActions(character, uiState, onIntent, onExportPdf)

            ReorderableItem(reorderState, key = character.id) { isDragging ->
                val progress = remember(character.id) { Animatable(0f) }

                LaunchedEffect(character.id) {
                    progress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = 300,
                            delayMillis = index.coerceAtMost(2) * 100,
                            easing = FastOutSlowInEasing,
                        ),
                    )
                }

                CharacterListItem(
                    character = character,
                    defaultTopCorners = topCorners,
                    defaultBottomCorners = bottomCorners,
                    isSelected = uiState.selectedIds.contains(character.id),
                    isSelectionMode = uiState.isSelectionMode,
                    isDragging = isDragging,
                    reorderScope = this,
                    itemActions = itemActions,
                    animatedVisibilityScope = animatedVisibilityScope,
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

@Composable
fun SharedTransitionScope.CharactersGrid(
    uiState: CharacterListState,
    lazyGridState: LazyStaggeredGridState,
    modifier: Modifier = Modifier,
    onIntent: (CharacterListIntent) -> Unit,
    onExportPdf: (Character) -> Unit,
    fabSpacing: Dp,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val hapticFeedback = LocalHapticFeedback.current
    val reorderState = rememberReorderableLazyStaggeredGridState(lazyGridState) { from, to ->
        onIntent(CharacterListIntent.MoveCharacter(from.key as Long, to.key as Long))
        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
    }
    val columnCount = when {
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> 3
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> 3
        else -> 2
    }

    LazyVerticalStaggeredGrid(
        state = lazyGridState,
        columns = StaggeredGridCells.Fixed(columnCount),
        contentPadding = PaddingValues(
            start = 8.dp,
            top = 8.dp,
            end = 8.dp,
            bottom = 8.dp + fabSpacing
        ),
        verticalItemSpacing = 8.dp,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.widthIn(max = 840.dp)
    ) {
        itemsIndexed(uiState.characters, key = { _, c -> c.id }) { index, character ->
            val itemActions =
                rememberCharacterItemActions(character, uiState, onIntent, onExportPdf)
            ReorderableItem(reorderState, key = character.id) { isDragging ->
                val progress = remember(character.id) { Animatable(0f) }

                LaunchedEffect(character.id) {
                    progress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = 300,
                            delayMillis = index.coerceAtMost(2) * 100,
                            easing = FastOutSlowInEasing,
                        ),
                    )
                }

                CharacterGridItem(
                    character = character,
                    isSelected = uiState.selectedIds.contains(character.id),
                    isSelectionMode = uiState.isSelectionMode,
                    isDragging = isDragging,
                    reorderScope = this,
                    itemActions = itemActions,
                    animatedVisibilityScope = animatedVisibilityScope,
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

@Composable
fun rememberCharacterItemActions(
    character: Character,
    uiState: CharacterListState,
    onIntent: (CharacterListIntent) -> Unit,
    onExportPdf: (Character) -> Unit
): CharacterItemActions {
    return remember(character, uiState.isSelectionMode, onIntent, onExportPdf) {
        CharacterItemActions(
            onClick = {
                if (uiState.isSelectionMode) onIntent(CharacterListIntent.SelectionToggled(character.id))
                else onIntent(CharacterListIntent.CharacterClicked(character.id))
            },
            onLongClick = {
                onIntent(CharacterListIntent.SelectionToggled(character.id))
            },
            onToggleSelection = { onIntent(CharacterListIntent.SelectionToggled(character.id)) },
            onDelete = { onIntent(CharacterListIntent.DeleteConfirmed(character.id)) },
            onExport = { onExportPdf(character) },
            onReorderFinished = { onIntent(CharacterListIntent.ReorderFinished) },
        )
    }
}

@PreviewLightDark
@PreviewDynamicColors
@Composable
private fun ListOfCharactersRoutePreview_LIST() {
    DnDSheetTheme {
        SharedTransitionLayout {
            AnimatedVisibility(true) {
                CharacterSheetsScreen(
                    uiState = CharacterListState(
                        allCharacters = UiUtils.sampleCharacters,
                        isLoading = false,
                        searchQuery = "",
                        selectedIds = setOf(UiUtils.sampleCharacters.first().id),
                    ),
                    onIntent = {},
                    animatedVisibilityScope = this
                )
            }
        }
    }
}

@PreviewLightDark
@PreviewDynamicColors
@Composable
private fun CharacterSheetsRoutePreview_GRID() {
    DnDSheetTheme {
        SharedTransitionLayout {
            AnimatedVisibility(true) {
                CharacterSheetsScreen(
                    uiState = CharacterListState(
                        allCharacters = UiUtils.sampleCharacters,
                        isLoading = false,
                        searchQuery = "",
                        selectedIds = setOf(UiUtils.sampleCharacters.first().id),
                    ),
                    onIntent = {},
                    animatedVisibilityScope = this
                )
            }
        }
    }
}