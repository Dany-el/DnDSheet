package com.yablonskyi.dndsheet.ui.spell

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yablonskyi.dndsheet.R
import com.yablonskyi.dndsheet.ui.utils.DeletingItemConfirmDialog

@Composable
fun SelectionBottomBar(
    title: String,
    confirmMsg: String,
    isSelectionMode: Boolean,
    isAllSelected: Boolean,
    onExportSelected: () -> Unit,
    onDeleteSelected: () -> Unit,
    onToggleSelectAll: () -> Unit,
) {

    var showConfirmDialog by remember {
        mutableStateOf(false)
    }

    if (showConfirmDialog) {
        DeletingItemConfirmDialog(
            title = title,
            text = confirmMsg,
            onConfirm = onDeleteSelected,
            onDiscard = { showConfirmDialog = false },
        )
    }

    if (isSelectionMode) {
        BottomAppBar(
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isAllSelected,
                    onCheckedChange = {
                        onToggleSelectAll()
                    }
                )
                Text(
                    text = if (isAllSelected)
                        stringResource(R.string.unselect_all_spells)
                    else
                        stringResource(R.string.select_all_spells)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = onExportSelected) {
                Icon(Icons.Default.Share, contentDescription = "Export")
            }
            IconButton(onClick = { showConfirmDialog = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }

    }
}