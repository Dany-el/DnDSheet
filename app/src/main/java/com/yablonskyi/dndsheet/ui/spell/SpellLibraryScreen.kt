package com.yablonskyi.dndsheet.ui.spell

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import com.yablonskyi.dndsheet.data.model.character.Spell
import com.yablonskyi.dndsheet.ui.character.slides.SpellDialog
import com.yablonskyi.dndsheet.ui.character.slides.SpellFiltersRow
import com.yablonskyi.dndsheet.ui.character.slides.SpellTag
import com.yablonskyi.dndsheet.ui.theme.DnDSheetTheme
import com.yablonskyi.dndsheet.ui.utils.UiUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpellLibraryScreen(
    spells: List<SpellLibraryItem>,
    searchQuery: String,
    currentFilter: SpellFilter,
    availableFilters: List<SpellFilter>,
    isSelectionMode: Boolean,
    onNavigateBack: () -> Unit,
    onAddSpell: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onFilterChange: (SpellFilter) -> Unit,
    onEditSpell: (Long) -> Unit,
    onToggleSpell: (Spell) -> Unit,
    onDeleteSpell: (Spell) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSpell by remember { mutableStateOf<Spell?>(null) }

    val groupedSpells = remember(spells) {
        spells.groupBy { it.spell.level }.toSortedMap()
    }

    val keyboardController = LocalSoftwareKeyboardController.current

    var isFabVisible by remember { mutableStateOf(true) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -5f) {
                    isFabVisible = false
                } else if (available.y > 5f) {
                    isFabVisible = true
                }
                return Offset.Zero
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(bottom = 0.dp),
        modifier = modifier.nestedScroll(nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                navigationIcon = {
                    if (isSelectionMode) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Localized description",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                title = {
                    Text(
                        text = stringResource(
                            if (isSelectionMode) R.string.spell_selection else R.string.msg_spell_library
                        ),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            )
        },
        floatingActionButton = {
            if (!isSelectionMode) {
                AnimatedVisibility(
                    visible = isFabVisible,
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
            }
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

            if (spells.isNotEmpty()) {
                SpellFiltersRow(
                    filters = availableFilters,
                    selectedFilter = currentFilter,
                    onFilterChange = onFilterChange,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    groupedSpells.forEach { (level, spells) ->
                        stickyHeader {
                            Surface(
                                color = MaterialTheme.colorScheme.background
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.Start,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = stringResource(level.resId),
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                }
                            }
                        }
                        items(
                            items = spells, key = { it.spell.spellId }
                        ) { item ->
                            SpellLibraryRow(
                                item = item,
                                isSelectionMode = isSelectionMode,
                                onToggle = { onToggleSpell(item.spell) },
                                onEdit = { onEditSpell(item.spell.spellId) },
                                onDelete = { onDeleteSpell(item.spell) },
                                onSpellClick = { selectedSpell = item.spell }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    if (selectedSpell != null) {
        SpellDialog(
            spell = selectedSpell!!,
            onDismissRequest = {
                selectedSpell = null
            }
        )
    }
}

@Composable
fun SpellLibraryRow(
    item: SpellLibraryItem,
    isSelectionMode: Boolean,
    onSpellClick: () -> Unit,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val spell = item.spell

    val haptics = LocalHapticFeedback.current

    ListItem(
        modifier = Modifier
            .combinedClickable(
                onClick = { if (isSelectionMode) onToggle() else onEdit() },
                onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSpellClick
                }
            ),
        headlineContent = {
            Text(
                text = spell.name,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.SemiBold,
            )
        },
        supportingContent = {
            if (spell.isConcentration || spell.isRitual) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    if (spell.isConcentration) {
                        SpellTag(
                            stringResource(R.string.concentration)
                        )
                    }
                    if (spell.isRitual) {
                        SpellTag(
                            stringResource(R.string.ritual)
                        )
                    }
                }
            }
        },
        trailingContent = {
            if (isSelectionMode) {
                Switch(
                    checked = item.isLearned,
                    onCheckedChange = { onToggle() }
                )
            } else {
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
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
            searchQuery = "",
            currentFilter = SpellFilter.All,
            availableFilters = UiUtils.availableFilters,
            isSelectionMode = true,
            onSearchQueryChange = {},
            onFilterChange = {},
            onNavigateBack = {},
            onAddSpell = {},
            onEditSpell = {},
            onToggleSpell = {},
            onDeleteSpell = {}
        )
    }
}

@Preview
@Composable
private fun SpellLibraryPreview_SELECTION_OFF() {

    val mockSpells = UiUtils.sampleSpells.map {
        SpellLibraryItem(it, isLearned = with(it) { this.isConcentration })
    }

    DnDSheetTheme {
        SpellLibraryScreen(
            spells = mockSpells,
            searchQuery = "",
            currentFilter = SpellFilter.All,
            availableFilters = UiUtils.availableFilters,
            isSelectionMode = false,
            onSearchQueryChange = {},
            onFilterChange = {},
            onNavigateBack = {},
            onAddSpell = {},
            onEditSpell = {},
            onToggleSpell = {},
            onDeleteSpell = {}
        )
    }
}