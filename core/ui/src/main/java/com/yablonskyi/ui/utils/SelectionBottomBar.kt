package com.yablonskyi.ui.utils

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yablonskyi.ui.R
import com.yablonskyi.ui.theme.Dimens

@Composable
fun SelectionBottomBar(
    isSelectionMode: Boolean,
    isAllSelected: Boolean,
    onExportSelected: () -> Unit,
    onDeleteSelected: () -> Unit,
    onToggleSelectAll: () -> Unit,
    title: String? = null,
    confirmMsg: String? = null,
    actionsEnabled: Boolean = true,
) {
    var showConfirmDialog by remember { mutableStateOf(false) }

    if (showConfirmDialog && title != null && confirmMsg != null) {
        DeletingItemConfirmDialog(
            title = title,
            text = confirmMsg,
            onConfirm = onDeleteSelected,
            onDiscard = { showConfirmDialog = false },
        )
    }

    AnimatedVisibility(
        visible = isSelectionMode,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it/2}),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it/2})
    ) {
        val shape = MaterialTheme.shapes.extraLarge

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(BottomAppBarDefaults.windowInsets)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            BottomAppBar(
                contentPadding = PaddingValues(horizontal = Dimens.Spacing.Large),
                tonalElevation = 6.dp,
                windowInsets = WindowInsets(0.dp),
                modifier = Modifier
                    .shadow(
                        elevation = 6.dp,
                        shape = shape,
                        clip = false
                    )
                    .clip(shape)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            onToggleSelectAll()
                        }
                    )
                ) {
                    Checkbox(
                        checked = isAllSelected,
                        onCheckedChange = {
                            onToggleSelectAll()
                        }
                    )
                    Text(
                        text = if (isAllSelected)
                            stringResource(R.string.unselect_all)
                        else
                            stringResource(R.string.select_all)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(enabled = actionsEnabled, onClick = onExportSelected) {
                    Icon(Icons.Default.Save, contentDescription = stringResource(R.string.export))
                }
                IconButton(enabled = actionsEnabled, onClick = {
                    if (title != null && confirmMsg != null) showConfirmDialog = true
                    else onDeleteSelected()
                }) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                }
            }
        }
    }
}