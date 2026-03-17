package com.yablonskyi.dndsheet.ui.character

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListOfCharactersScreen(
    characters: List<Character>,
    loadingState: Boolean,
    listView: ListView,
    // Search
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    // Selection
    isSelectionMode: Boolean,
    isAllSelected: Boolean,
    onClearSelection: () -> Unit,
    onDeleteSelected: () -> Unit,
    onToggleSelectAll: () -> Unit,
    selectedCharacters: Set<Character>,
    toggleSelection: (Character) -> Unit,
    onAdd: () -> Unit,
    onDelete: (Character) -> Unit,
    onCharacterClick: (Long) -> Unit,
    // Export/Import
    onImportSheets: (List<CharacterSheet>) -> Unit,
    onExportSheets: suspend () -> List<CharacterSheet>,
    onGetCharacterSheet: suspend (Character) -> CharacterSheet,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isSearchExpanded by remember { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }

    val selectionFailureMessage = stringResource(R.string.select_at_least_one_item)
    val importMessage = stringResource(R.string.import_file_empty)

    var isLoading by remember { mutableStateOf(false) }

    var pendingImportSheets by remember { mutableStateOf<List<CharacterSheet>?>(null) }

    // Export Launcher
    val exportLauncher = rememberMultiCharacterExportLauncher(
        onLoadingStateChange = { isLoading = it },
        sheetsToExport = onExportSheets,
        onExportComplete = onClearSelection
    )

    // Import Launcher
    val importLauncher = rememberCharacterImportLauncher(
        onLoadingStateChange = { isLoading = it },
        onImportCharacters = { sheets ->
            if (sheets.isNotEmpty()) {
                pendingImportSheets = sheets
            } else {
                Toast.makeText(
                    context,
                    importMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    )

    val onExport = { character: Character ->
        isLoading = true

        coroutineScope.launch(Dispatchers.IO) {
            try {
                val sheet = onGetCharacterSheet(character)
                val pdfFile = PdfExporter.generateCustomSheet(context, sheet)

                withContext(Dispatchers.Main) {
                    isLoading = false

                    if (pdfFile != null) {
                        sharePdfIntent(context, pdfFile)
                    } else {
                        Toast.makeText(
                            context,
                            "Failed to generate PDF",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    isLoading = false
                    Toast.makeText(
                        context,
                        "An error occurred during export",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    BackHandler(enabled = isSelectionMode || isSearchExpanded) {
        if (isSelectionMode) {
            onClearSelection()
        } else if (isSearchExpanded) {
            isSearchExpanded = false
            onSearchQueryChange("")
        }
    }

    val bottomNavHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val fabSpacing = 72.dp

    Scaffold(
        contentWindowInsets = WindowInsets(bottom = 0.dp),
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    if (isSelectionMode) {
                        Button(onClick = onClearSelection) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Clear selection")
                                Text(text = "${selectedCharacters.size}")
                            }
                        }
                    } else if (isSearchExpanded) {
                        IconButton(
                            onClick = {
                                isSearchExpanded = false
                                onSearchQueryChange("")
                            }
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Close search"
                            )
                        }
                    }
                },
                title = {
                    if (isSearchExpanded) {
                        TextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
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

                        LaunchedEffect(Unit) {
                            searchFocusRequester.requestFocus()
                        }
                    } else if (!isSelectionMode) {
                        Text(
                            stringResource(R.string.characters),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                actions = {
                    if (!isSearchExpanded) {
                        IconButton(onClick = { isSearchExpanded = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    }

                    if (!isSelectionMode) {
                        var menuExpanded by remember { mutableStateOf(false) }

                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More Options"
                                )
                            }

                            SlicedDropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false },
                                items = listOf(
                                    SlicedMenuItem(
                                        text = stringResource(R.string.confirm_import),
                                        icon = Icons.Default.Download,
                                        onClick = {
                                            importLauncher.launch(
                                                arrayOf(
                                                    "application/json",
                                                    "*/*"
                                                )
                                            )
                                        }
                                    )
                                )
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                )
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = !isSelectionMode,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                FloatingActionButton(
                    onClick = onAdd,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create New Character",
                    )
                }
            }
        },
        bottomBar = {
            SelectionBottomBar(
                title = stringResource(R.string.q_delete_character),
                confirmMsg = stringResource(
                    R.string.q_confirm_text_characters,
                    selectedCharacters.size
                ),
                isSelectionMode = isSelectionMode,
                isAllSelected = isAllSelected,
                onExportSelected = {
                    if (selectedCharacters.isEmpty()) {
                        Toast.makeText(
                            context,
                            selectionFailureMessage,
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        exportLauncher.launch("characters_backup.json")
                    }
                },
                onDeleteSelected = onDeleteSelected,
                onToggleSelectAll = onToggleSelectAll
            )
        }
    ) { padding ->
        if (!loadingState) {
            when (listView) {
                ListView.LIST -> {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            top = 16.dp,
                            end = 16.dp,
                            bottom = 16.dp + bottomNavHeight + fabSpacing
                        ),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        itemsIndexed(characters, key = { _, item -> item.id }) { index, character ->

                            val itemShape = when {
                                characters.size == 1 -> RoundedCornerShape(16.dp)
                                index == 0 -> RoundedCornerShape(
                                    topStart = 16.dp, topEnd = 16.dp,
                                    bottomStart = 4.dp, bottomEnd = 4.dp
                                )

                                index == characters.lastIndex -> RoundedCornerShape(
                                    topStart = 4.dp, topEnd = 4.dp,
                                    bottomStart = 16.dp, bottomEnd = 16.dp
                                )

                                else -> MaterialTheme.shapes.extraSmall
                            }

                            CharacterListItem(
                                character = character,
                                shape = itemShape,
                                isSelected = selectedCharacters.contains(character),
                                isSelectionMode = isSelectionMode,
                                onToggleSelection = { toggleSelection(character) },
                                onClick = { onCharacterClick(character.id) },
                                onDelete = { onDelete(character) },
                                onExport = { onExport(character) },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }

                ListView.GRID -> {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            top = 16.dp,
                            end = 16.dp,
                            bottom = 16.dp + bottomNavHeight + fabSpacing
                        ),
                        verticalItemSpacing = 4.dp,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        items(characters, key = { it.id }) { character ->
                            CharacterGridItem(
                                character = character,
                                isSelected = selectedCharacters.contains(character),
                                isSelectionMode = isSelectionMode,
                                onToggleSelection = { toggleSelection(character) },
                                onClick = { onCharacterClick(character.id) },
                                onDelete = { onDelete(character) },
                                onExport = { onExport(character) },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
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

    if (isLoading) {
        LoadingDialog()
    }

    pendingImportSheets?.let { sheetsToImport ->
        val successMsg: String = stringResource(R.string.success_import)

        AlertDialog(
            onDismissRequest = {
                pendingImportSheets = null
            },
            title = {
                Text(stringResource(R.string.confirm_import_title))
            },
            text = {
                Text(stringResource(R.string.alert_dialog_import_chars, sheetsToImport.size))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onImportSheets(sheetsToImport)
                        pendingImportSheets = null
                        Toast.makeText(context, successMsg, Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text(stringResource(R.string.confirm_import))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        pendingImportSheets = null
                    }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun CharacterListItem(
    character: Character,
    shape: Shape,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onToggleSelection: () -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onExport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current

    var menuExpanded by remember { mutableStateOf(false) }

    var showConfirmDialog by remember {
        mutableStateOf(false)
    }

    if (showConfirmDialog) {
        DeletingItemConfirmDialog(
            title = stringResource(R.string.q_delete_character),
            text = stringResource(R.string.q_confirm_text, character.name),
            onConfirm = onDelete,
            onDiscard = { showConfirmDialog = false },
        )
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = shape,
        modifier = modifier
            .heightIn(min = 120.dp)
            .wrapContentHeight()
            .combinedClickable(
                onClick = {
                    if (isSelectionMode) {
                        onToggleSelection()
                    } else {
                        onClick()
                    }
                },
                onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggleSelection()
                }
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Surface(
                modifier = Modifier
                    .wrapContentHeight()
                    .heightIn(max = 120.dp)
                    .widthIn(max = 120.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                if (character.imagePath != null) {
                    AsyncImage(
                        model = character.imagePath,
                        contentDescription = "Character Profile",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Add Photo",
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxSize()
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                Row {
                    Text(
                        text = character.name,
                        textAlign = TextAlign.Start,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically)
                    )
                    if (isSelectionMode) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { onToggleSelection() },
                            modifier = Modifier.weight(0.2f)
                        )
                    } else {
                        Box {
                            IconButton(
                                onClick = { menuExpanded = true },
                                modifier = Modifier.align(
                                    Alignment.TopEnd
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Options",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
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
                                        onClick = { showConfirmDialog = true }
                                    )
                                )
                            )
                        }
                    }
                }
                val characterInfo = listOf(character.race, character.charClass)
                    .filter { it.isNotBlank() }
                    .joinToString(" — ")

                if (characterInfo.isNotBlank()) {
                    Text(
                        text = characterInfo,
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "${stringResource(R.string.spell_level)} ${character.level}",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
fun CharacterGridItem(
    character: Character,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onToggleSelection: () -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onExport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current

    var menuExpanded by remember { mutableStateOf(false) }

    var showConfirmDialog by remember {
        mutableStateOf(false)
    }

    if (showConfirmDialog) {
        DeletingItemConfirmDialog(
            title = stringResource(R.string.q_delete_character),
            text = stringResource(R.string.q_confirm_text, character.name),
            onConfirm = onDelete,
            onDiscard = { showConfirmDialog = false },
        )
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .combinedClickable(
                onClick = {
                    if (isSelectionMode) {
                        onToggleSelection()
                    } else {
                        onClick()
                    }
                },
                onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggleSelection()
                }
            )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .heightIn(min = 180.dp)
                        .clip(
                            RoundedCornerShape(
                                topStart = 8.dp,
                                topEnd = 8.dp,
                            )
                        ),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    if (character.imagePath != null) {
                        AsyncImage(
                            model = character.imagePath,
                            contentDescription = "Character Profile",
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Add Photo",
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxSize()
                        )
                    }
                }
                if (isSelectionMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onToggleSelection() },
                        modifier = Modifier.align(Alignment.TopEnd)
                    )
                } else {
                    Box(
                        Modifier.align(
                            Alignment.TopEnd
                        )
                    ) {
                        IconButton(
                            onClick = { menuExpanded = true },
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
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
                                    onClick = { showConfirmDialog = true }
                                )
                            )
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                Row {
                    Text(
                        text = character.name,
                        textAlign = TextAlign.Start,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically)
                    )
                }
                val characterInfo = listOf(character.race, character.charClass)
                    .filter { it.isNotBlank() }
                    .joinToString(" — ")

                if (characterInfo.isNotBlank()) {
                    Text(
                        text = characterInfo,
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "${stringResource(R.string.spell_level)} ${character.level}",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Preview
@Composable
private fun ListOfCharactersRoutePreview_LIST() {
    DnDSheetTheme {
        ListOfCharactersScreen(
            characters = UiUtils.sampleCharacters,
            loadingState = false,
            searchQuery = "",
            onSearchQueryChange = {},
            listView = ListView.LIST,
            onAdd = {},
            onDelete = {},
            onCharacterClick = {},
            onDeleteSelected = {},
            onToggleSelectAll = {},
            onClearSelection = {},
            toggleSelection = {},
            isSelectionMode = false,
            isAllSelected = false,
            selectedCharacters = emptySet(),
            onImportSheets = {},
            onExportSheets = { emptyList() },
            onGetCharacterSheet = {
                CharacterSheet(
                    UiUtils.sampleCharacters.first(),
                    UiUtils.sampleSpells,
                    UiUtils.rawAttacks
                )
            },
        )
    }
}

@Preview
@Composable
private fun ListOfCharactersRoutePreview_GRID() {
    DnDSheetTheme {
        ListOfCharactersScreen(
            characters = UiUtils.sampleCharacters,
            loadingState = false,
            searchQuery = "",
            onSearchQueryChange = {},
            listView = ListView.GRID,
            onAdd = {},
            onDelete = {},
            onCharacterClick = {},
            onDeleteSelected = {},
            onToggleSelectAll = {},
            onClearSelection = {},
            toggleSelection = {},
            isSelectionMode = false,
            isAllSelected = false,
            selectedCharacters = emptySet(),
            onImportSheets = {},
            onExportSheets = { emptyList() },
            onGetCharacterSheet = {
                CharacterSheet(
                    UiUtils.sampleCharacters.first(),
                    UiUtils.sampleSpells,
                    UiUtils.rawAttacks
                )
            },
        )
    }
}