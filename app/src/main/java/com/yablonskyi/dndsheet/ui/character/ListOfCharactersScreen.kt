package com.yablonskyi.dndsheet.ui.character

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.yablonskyi.dndsheet.ui.spell.SelectionBottomBar
import com.yablonskyi.dndsheet.ui.theme.DnDSheetTheme
import com.yablonskyi.dndsheet.ui.utils.DeletingItemConfirmDialog
import com.yablonskyi.dndsheet.ui.utils.LoadingDialog
import com.yablonskyi.dndsheet.ui.utils.SlicedDropdownMenu
import com.yablonskyi.dndsheet.ui.utils.SlicedMenuItem
import com.yablonskyi.dndsheet.ui.utils.UiUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListOfCharactersScreen(
    characters: List<Character>,
    loadingState: Boolean,
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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

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

    BackHandler(enabled = isSelectionMode) {
        onClearSelection()
    }

    val bottomNavHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val fabSpacing = 72.dp

    Scaffold(
        contentWindowInsets = WindowInsets(bottom = 0.dp),
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    if (isSelectionMode) {
                        Button(
                            onClick = onClearSelection,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear selection",
                                )
                                Text(text = "${selectedCharacters.size}")
                            }
                        }
                    }
                },
                title = {
                    if (!isSelectionMode) {
                        Text(
                            stringResource(R.string.characters),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                actions = {
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
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp + bottomNavHeight + fabSpacing
                ),
                verticalItemSpacing = 8.dp,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(characters, key = { it.id }) { character ->
                    CharacterItem(
                        character = character,
                        isSelected = selectedCharacters.contains(character),
                        isSelectionMode = isSelectionMode,
                        onToggleSelection = { toggleSelection(character) },
                        onClick = { onCharacterClick(character.id) },
                        onDelete = { onDelete(character) },
                        modifier = Modifier.animateItem()
                    )
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

// Unused
@Composable
fun InsertCharacterRow(
    onAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        Surface(
            color = MaterialTheme.colorScheme.background.copy(alpha = 0f),
            shape = MaterialTheme.shapes.extraSmall,
            border = BorderStroke(2.dp, color = MaterialTheme.colorScheme.outline),
            modifier = Modifier
                .height(100.dp)
                .clickable(
                    onClick = onAdd
                )
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.add).uppercase(),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
fun CharacterItem(
    character: Character,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onToggleSelection: () -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current

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
                }
            }
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = character.name,
                    textAlign = TextAlign.Start,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleLarge,
                )
                if (character.charClass.isNotEmpty() || character.charClass.isNotBlank()) {
                    Text(
                        text = character.charClass,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }

    /*ListItem(
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
            ),
        tonalElevation = 6.dp,
        shadowElevation = 2.dp,
        headlineContent = {
            Text(
                text = character.name,
                textAlign = TextAlign.Start,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        supportingContent = {
            if (character.charClass.isNotEmpty() || character.charClass.isNotBlank()) {
                Text(
                    text = character.charClass,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        },
        trailingContent = {
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelection() }
                )
            } else {
                *//*IconButton(
                    onClick = {
                        showConfirmDialog = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }*//*
            }
        }
    )*/
}

@Preview
@Composable
private fun ListOfCharactersRoutePreview() {
    DnDSheetTheme {
        ListOfCharactersScreen(
            characters = UiUtils.sampleCharacters,
            loadingState = false,
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
            onExportSheets = { emptyList() }
        )
    }
}