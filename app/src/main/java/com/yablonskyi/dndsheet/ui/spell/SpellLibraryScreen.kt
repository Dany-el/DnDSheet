package com.yablonskyi.dndsheet.ui.spell

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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yablonskyi.dndsheet.R
import com.yablonskyi.dndsheet.data.model.character.MagicSchool
import com.yablonskyi.dndsheet.data.model.character.Spell
import com.yablonskyi.dndsheet.data.model.character.SpellCastTime
import com.yablonskyi.dndsheet.data.model.character.SpellDuration
import com.yablonskyi.dndsheet.data.model.character.SpellLevel
import com.yablonskyi.dndsheet.ui.character.slides.SpellFiltersRow
import com.yablonskyi.dndsheet.ui.spell.launchers.rememberExportLauncher
import com.yablonskyi.dndsheet.ui.spell.launchers.rememberImportLauncher
import com.yablonskyi.dndsheet.ui.theme.DnDSheetTheme
import com.yablonskyi.dndsheet.ui.utils.DeletingItemConfirmDialog
import com.yablonskyi.dndsheet.ui.utils.LoadingDialog
import com.yablonskyi.dndsheet.ui.utils.SlicedDropdownMenu
import com.yablonskyi.dndsheet.ui.utils.SlicedMenuItem
import com.yablonskyi.dndsheet.ui.utils.UiUtils
import com.yablonskyi.dndsheet.ui.utils.shareSpellAsJsonFile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpellLibraryScreen(
    spells: List<SpellLibraryItem>,
    loadingState: Boolean,
    searchQuery: String,
    isLearnMode: Boolean,

    onNavigateBack: () -> Unit,
    onAddSpell: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onEditSpell: (Long) -> Unit,
    onToggleSpell: (Spell) -> Unit,
    onDeleteSpell: (Spell) -> Unit,
    onImportSpells: (List<Spell>) -> Unit,
    // Filters
    selectedSchool: Set<MagicSchool>,
    selectedLevels: Set<SpellLevel>,
    selectedDurations: Set<SpellDuration>,
    selectedCastTimes: Set<SpellCastTime>,
    selectedConcentration: Boolean,
    selectedRitual: Boolean,
    // Toggles
    toggleSchoolFilter: (MagicSchool) -> Unit,
    toggleLevelFilter: (SpellLevel) -> Unit,
    toggleDurationFilter: (SpellDuration) -> Unit,
    toggleCastTimeFilter: (SpellCastTime) -> Unit,
    toggleRitual: () -> Unit,
    toggleConcentration: () -> Unit,
    // Selection
    isSelectionMode: Boolean,
    isAllSelected: Boolean,
    onClearSelection: () -> Unit,
    onDeleteSelected: () -> Unit,
    onToggleSelectAll: () -> Unit,
    selectedSpells: Set<Spell>,
    toggleSelection: (Spell) -> Unit,
) {
    val context = LocalContext.current

    val groupedSpells = remember(spells) {
        spells.groupBy { it.spell.level }.toSortedMap()
    }

    val keyboardController = LocalSoftwareKeyboardController.current

    // ------------ USER SCROLL ------------

    // ------------ EXPORT & IMPORT OF SPELLS ------------

    val spellsToExport = if (selectedSpells.isNotEmpty()) {
        selectedSpells.toList()
    } else {
        spells.map { it.spell }
    }

    var isLoading by remember { mutableStateOf(false) }

    var pendingImportSpells by remember { mutableStateOf<List<Spell>?>(null) }
    val importMessage = stringResource(R.string.import_file_empty)

    // Export Launcher
    val exportLauncher = rememberExportLauncher(
        onLoadingStateChange = { isLoading = it },
        spellsToExport = spellsToExport,
        onExportComplete = onClearSelection
    )

    // Import Launcher
    val importLauncher = rememberImportLauncher(
        onLoadingStateChange = { isLoading = it },
        onImportSpells = { spells ->
            if (spells.isNotEmpty()) {
                pendingImportSpells = spells
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

    Scaffold(
        contentWindowInsets = WindowInsets(bottom = 0.dp),
        topBar = {
            SpellLibraryTopBar(
                isLearnMode = isLearnMode,
                isSelectionMode = isSelectionMode,
                selectedCount = selectedSpells.size,
                onClearSelection = onClearSelection,
                onNavigateBack = onNavigateBack,
                onActionClick = { action ->
                    when (action) {
                        LibraryMenuAction.IMPORT -> {
                            importLauncher.launch(arrayOf("application/json", "*/*"))
                        }

                        LibraryMenuAction.EXPORT -> {
                            exportLauncher.launch("spells_backup.json")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = !isLearnMode && !isSelectionMode,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                FloatingActionButton(
                    onClick = onAddSpell,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create New Spell",
                    )
                }
            }
        },
        bottomBar = {
            SelectionBottomBar(
                title = stringResource(R.string.q_delete_spell),
                confirmMsg = stringResource(
                    R.string.q_confirm_text_spells,
                    selectedSpells.size
                ),
                isSelectionMode = isSelectionMode,
                isAllSelected = isAllSelected,
                onExportSelected = { exportLauncher.launch("selected_spells.json") },
                onDeleteSelected = onDeleteSelected,
                onToggleSelectAll = onToggleSelectAll
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text(stringResource(R.string.search_spells)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                } else null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            SpellFiltersRow(
                // Filters
                selectedSchool = selectedSchool,
                selectedLevels = selectedLevels,
                selectedDurations = selectedDurations,
                selectedCastTimes = selectedCastTimes,
                selectedConcentration = selectedConcentration,
                selectedRitual = selectedRitual,
                // Toggles
                toggleSchoolFilter = toggleSchoolFilter,
                toggleLevelFilter = toggleLevelFilter,
                toggleDurationFilter = toggleDurationFilter,
                toggleCastTimeFilter = toggleCastTimeFilter,
                toggleRitual = toggleRitual,
                toggleConcentration = toggleConcentration,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp)
            )

            if (!loadingState) {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    groupedSpells.forEach { (level, spells) ->
                        stickyHeader {
                            Surface(
                                color = MaterialTheme.colorScheme.background,
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.Start,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                ) {
                                    Text(
                                        text = stringResource(level.resId),
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        }
                        itemsIndexed(
                            items = spells, key = { _, item -> item.spell.spellId }
                        ) { index, item ->

                            val itemShape = when {
                                spells.size == 1 -> RoundedCornerShape(16.dp)
                                index == 0 -> RoundedCornerShape(
                                    topStart = 16.dp, topEnd = 16.dp,
                                    bottomStart = 4.dp, bottomEnd = 4.dp
                                )

                                index == spells.lastIndex -> RoundedCornerShape(
                                    topStart = 4.dp, topEnd = 4.dp,
                                    bottomStart = 16.dp, bottomEnd = 16.dp
                                )

                                else -> MaterialTheme.shapes.extraSmall
                            }

                            SpellLibraryRow(
                                item = item,
                                shape = itemShape,
                                isLearnMode = isLearnMode,
                                isSelected = selectedSpells.contains(item.spell),
                                isSelectionMode = isSelectionMode,
                                onToggle = { onToggleSpell(item.spell) },
                                onEdit = { onEditSpell(item.spell.spellId) },
                                onDelete = { onDeleteSpell(item.spell) },
                                onToggleSelection = { toggleSelection(item.spell) },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            } else {
                Box(
                    Modifier.fillMaxSize()
                ) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
            }
        }
    }

    if (isLoading) {
        LoadingDialog()
    }

    pendingImportSpells?.let { spells ->
        val successMsg: String = stringResource(R.string.success_import)

        AlertDialog(
            onDismissRequest = { pendingImportSpells = null },
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
                    onClick = {
                        onImportSpells(spells)
                        pendingImportSpells = null
                        Toast.makeText(context, successMsg, Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text(stringResource(R.string.confirm_import))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { pendingImportSpells = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun SpellLibraryRow(
    item: SpellLibraryItem,
    shape: Shape,
    isLearnMode: Boolean,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spell = item.spell

    val haptics = LocalHapticFeedback.current
    val context = LocalContext.current

    var showConfirmDialog by remember {
        mutableStateOf(false)
    }

    var menuExpanded by remember { mutableStateOf(false) }

    if (showConfirmDialog) {
        DeletingItemConfirmDialog(
            title = stringResource(R.string.q_delete_spell),
            text = stringResource(R.string.q_confirm_text, item.spell.name),
            onConfirm = onDelete,
            onDiscard = { showConfirmDialog = false },
        )
    }

    ListItem(
        modifier = modifier
            .clip(shape)
            .combinedClickable(
                onClick = {
                    if (isSelectionMode) {
                        onToggleSelection()
                    } else if (isLearnMode) {
                        onToggle()
                    } else {
                        onEdit()
                    }
                },
                onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggleSelection()
                }
            ),
        tonalElevation = 6.dp,
        shadowElevation = 2.dp,
        overlineContent = {
            Text(
                text = stringResource(spell.school.resId),
                modifier = Modifier.padding(bottom = 4.dp)
            )
        },
        headlineContent = {
            Text(
                text = spell.name,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.SemiBold,
            )
        },
        trailingContent = {
            if (isLearnMode) {
                Switch(
                    checked = item.isLearned,
                    onCheckedChange = { onToggle() }
                )
            } else if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelection() }
                )
            } else {
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
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
                                text = stringResource(R.string.edit),
                                icon = Icons.Default.Edit,
                                onClick = { onEdit() }
                            ),
                            SlicedMenuItem(
                                text = stringResource(R.string.share),
                                icon = Icons.Default.Share,
                                onClick = { shareSpellAsJsonFile(context, spell) }
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
    )
}

@Preview
@Composable
private fun SpellLibraryPreview() {

    val mockSpells = UiUtils.sampleSpells.map {
        SpellLibraryItem(it, isLearned = with(it) { this.isConcentration })
    }

    DnDSheetTheme {
        SpellLibraryScreen(
            spells = mockSpells,
            loadingState = false,
            searchQuery = "",
            isLearnMode = true,
            onNavigateBack = {},
            onAddSpell = {},
            onSearchQueryChange = {},
            onEditSpell = {},
            onToggleSpell = {},
            selectedLevels = emptySet(),
            selectedRitual = false,
            selectedSchool = emptySet(),
            selectedDurations = emptySet(),
            selectedConcentration = false,
            selectedCastTimes = emptySet(),
            toggleRitual = {},
            toggleConcentration = {},
            toggleLevelFilter = {},
            toggleSchoolFilter = {},
            toggleDurationFilter = {},
            toggleCastTimeFilter = {},
            onDeleteSpell = {},
            onImportSpells = {},
            onToggleSelectAll = {},
            onDeleteSelected = {},
            onClearSelection = {},
            toggleSelection = {},
            isSelectionMode = false,
            selectedSpells = emptySet(),
            isAllSelected = false,
        )
    }
}

@Preview
@Composable
private fun SpellLibraryPreview_SELECT_MODE_ON() {

    val mockSpells = UiUtils.sampleSpells.map {
        SpellLibraryItem(it, isLearned = false)
    }

    val selectedSpells =
        mockSpells.filter { it.spell.level == SpellLevel.LEVEL_1 }.map { it.spell }.toSet()

    DnDSheetTheme {
        SpellLibraryScreen(
            spells = mockSpells,
            loadingState = false,
            searchQuery = "",
            isLearnMode = false,
            onNavigateBack = {},
            onAddSpell = {},
            onSearchQueryChange = {},
            onEditSpell = {},
            onToggleSpell = {},
            selectedLevels = emptySet(),
            selectedRitual = false,
            selectedSchool = emptySet(),
            selectedDurations = emptySet(),
            selectedConcentration = false,
            selectedCastTimes = emptySet(),
            toggleRitual = {},
            toggleConcentration = {},
            toggleLevelFilter = {},
            toggleSchoolFilter = {},
            toggleDurationFilter = {},
            toggleCastTimeFilter = {},
            onDeleteSpell = {},
            onImportSpells = {},
            onToggleSelectAll = {},
            onDeleteSelected = {},
            onClearSelection = {},
            toggleSelection = {},
            isSelectionMode = true,
            selectedSpells = selectedSpells,
            isAllSelected = false,
        )
    }
}

@Preview(group = "selection_off")
@Composable
private fun SpellLibraryPreview_SELECTION_OFF() {

    val mockSpells = UiUtils.sampleSpells.map {
        SpellLibraryItem(it, isLearned = with(it) { this.isConcentration })
    }

    DnDSheetTheme {
        SpellLibraryScreen(
            spells = mockSpells,
            loadingState = false,
            searchQuery = "",
            isLearnMode = true,
            onNavigateBack = {},
            onAddSpell = {},
            onSearchQueryChange = {},
            onEditSpell = {},
            onToggleSpell = {},
            selectedLevels = emptySet(),
            selectedRitual = false,
            selectedSchool = emptySet(),
            selectedDurations = emptySet(),
            selectedConcentration = false,
            selectedCastTimes = emptySet(),
            toggleRitual = {},
            toggleConcentration = {},
            toggleLevelFilter = {},
            toggleSchoolFilter = {},
            toggleDurationFilter = {},
            toggleCastTimeFilter = {},
            onDeleteSpell = {},
            onImportSpells = {},
            onToggleSelectAll = {},
            onDeleteSelected = {},
            onClearSelection = {},
            toggleSelection = {},
            isSelectionMode = false,
            selectedSpells = emptySet(),
            isAllSelected = false,
        )
    }
}

@Preview(group = "selection_off", locale = "ru")
@Composable
private fun SpellLibraryPreview_SELECTION_OFF_RU() {

    val mockSpells = UiUtils.sampleSpells.filter { it.isConcentration }.map {
        SpellLibraryItem(it, isLearned = with(it) { this.isConcentration })
    }

    DnDSheetTheme {
        SpellLibraryScreen(
            spells = mockSpells,
            loadingState = false,
            searchQuery = "",
            isLearnMode = true,
            onNavigateBack = {},
            onAddSpell = {},
            onSearchQueryChange = {},
            onEditSpell = {},
            onToggleSpell = {},
            selectedLevels = emptySet(),
            selectedRitual = false,
            selectedSchool = emptySet(),
            selectedDurations = emptySet(),
            selectedConcentration = false,
            selectedCastTimes = emptySet(),
            toggleRitual = {},
            toggleConcentration = {},
            toggleLevelFilter = {},
            toggleSchoolFilter = {},
            toggleDurationFilter = {},
            toggleCastTimeFilter = {},
            onDeleteSpell = {},
            onImportSpells = {},
            onToggleSelectAll = {},
            onDeleteSelected = {},
            onClearSelection = {},
            toggleSelection = {},
            isSelectionMode = false,
            selectedSpells = emptySet(),
            isAllSelected = false,
        )
    }
}

@Preview(group = "selection_off", locale = "uk")
@Composable
private fun SpellLibraryPreview_SELECTION_OFF_UK() {

    val mockSpells = UiUtils.sampleSpells.filter { it.isConcentration }.map {
        SpellLibraryItem(it, isLearned = with(it) { this.isConcentration })
    }

    DnDSheetTheme {
        SpellLibraryScreen(
            spells = mockSpells,
            loadingState = false,
            searchQuery = "",
            isLearnMode = true,
            onNavigateBack = {},
            onAddSpell = {},
            onSearchQueryChange = {},
            onEditSpell = {},
            onToggleSpell = {},
            selectedLevels = emptySet(),
            selectedRitual = false,
            selectedSchool = emptySet(),
            selectedDurations = emptySet(),
            selectedConcentration = false,
            selectedCastTimes = emptySet(),
            toggleRitual = {},
            toggleConcentration = {},
            toggleLevelFilter = {},
            toggleSchoolFilter = {},
            toggleDurationFilter = {},
            toggleCastTimeFilter = {},
            onDeleteSpell = {},
            onImportSpells = {},
            onToggleSelectAll = {},
            onDeleteSelected = {},
            onClearSelection = {},
            toggleSelection = {},
            isSelectionMode = false,
            selectedSpells = emptySet(),
            isAllSelected = false,
        )
    }
}