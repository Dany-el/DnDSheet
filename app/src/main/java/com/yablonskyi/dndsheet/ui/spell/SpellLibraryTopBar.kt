package com.yablonskyi.dndsheet.ui.spell

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yablonskyi.dndsheet.R
import com.yablonskyi.dndsheet.ui.utils.SlicedDropdownMenu
import com.yablonskyi.dndsheet.ui.utils.SlicedMenuItem

enum class LibraryMenuAction(
    @StringRes val title: Int,
    val icon: ImageVector
) {
    IMPORT(R.string.confirm_import, Icons.Default.Download),
    EXPORT(R.string.export_spells, Icons.Default.Upload)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpellLibraryTopBar(
    isLearnMode: Boolean,
    isSelectionMode: Boolean,
    selectedCount: Int,
    onClearSelection: () -> Unit,
    onActionClick: (LibraryMenuAction) -> Unit,
    onNavigateBack: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        navigationIcon = {
            if (isLearnMode) {
                IconButton(
                    onClick = {
                        onNavigateBack()
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Navigate back",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            } else if (isSelectionMode) {
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
                        Text(text = "$selectedCount")
                    }
                }
            }
        },
        title = {
            if (!isSelectionMode) {
                Text(
                    text = stringResource(
                        if (isLearnMode) R.string.spell_selection else R.string.msg_spell_library
                    ),
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        actions = {
            if (!isLearnMode && !isSelectionMode) {
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
                        items = LibraryMenuAction.entries.map { action ->
                            SlicedMenuItem(
                                text = stringResource(action.title),
                                icon = action.icon,
                                onClick = {
                                    onActionClick(action)
                                }
                            )
                        }
                    )
                }
            }
        }
    )
}