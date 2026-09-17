package com.yablonskyi.character.presentation.list.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NoPhotography
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.yablonskyi.ui.R
import com.yablonskyi.ui.utils.DeletingItemConfirmDialog
import com.yablonskyi.ui.utils.SlicedDropdownMenu
import com.yablonskyi.ui.utils.SlicedMenuItem
import sh.calvin.reorderable.ReorderableCollectionItemScope

@Composable
fun CharacterItemImage(
    imagePath: String?,
    selectionProgress: Float,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape? = null
) {
    val imageShape = remember(shape, selectionProgress) {
        val initialShape = shape ?: RoundedCornerShape(0.dp)
        fun animatedCorner(corner: CornerSize): CornerSize = object : CornerSize {
            override fun toPx(shapeSize: Size, density: Density): Float {
                val start = corner.toPx(shapeSize, density)
                val end = with(density) { 16.dp.toPx() }
                return start + (end - start) * selectionProgress
            }
        }
        RoundedCornerShape(
            topStart = animatedCorner(initialShape.topStart),
            topEnd = animatedCorner(initialShape.topEnd),
            bottomEnd = animatedCorner(initialShape.bottomEnd),
            bottomStart = animatedCorner(initialShape.bottomStart),
        )
    }

    Surface(
        modifier = modifier
            .padding(8.dp)
            .graphicsLayer {
                val scale = 1f - 0.1f * selectionProgress
                scaleX = scale
                scaleY = scale
            },
        shape = imageShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        if (imagePath != null) {
            AsyncImage(
                model = imagePath,
                contentDescription = "Character Profile",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box {
                Icon(
                    imageVector = Icons.Default.NoPhotography,
                    contentDescription = "Fallback profile",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(32.dp)
                )
            }
        }
    }
}

@Composable
fun CharacterItemTrailingAction(
    isSelectionMode: Boolean,
    reorderScope: ReorderableCollectionItemScope,
    characterName: String,
    onExport: () -> Unit,
    onDelete: () -> Unit,
    onReorderFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (isSelectionMode) {
        val hapticFeedback = LocalHapticFeedback.current
        IconButton(
            onClick = {},
            modifier = with(reorderScope) {
                modifier.draggableHandle(
                    onDragStarted = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                    },
                    onDragStopped = {
                        onReorderFinished()
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureEnd)
                    },
                )
            },
        ) {
            Icon(Icons.Rounded.DragHandle, contentDescription = "Reorder $characterName")
        }
    } else {
        var menuExpanded by remember { mutableStateOf(false) }
        var showConfirmDialog by remember { mutableStateOf(false) }

        if (showConfirmDialog) {
            DeletingItemConfirmDialog(
                title = stringResource(R.string.q_delete_character),
                text = stringResource(R.string.q_confirm_text, characterName),
                onConfirm = onDelete,
                onDiscard = { showConfirmDialog = false },
            )
        }

        Box(
            modifier = modifier
        ) {
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(Icons.Default.MoreVert, contentDescription = "Options")
            }
            SlicedDropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                items = listOf(
                    SlicedMenuItem(
                        text = stringResource(R.string.print_character_sheet),
                        icon = Icons.Default.Share,
                        onClick = onExport
                    ),
                    SlicedMenuItem(
                        text = stringResource(R.string.delete),
                        icon = Icons.Default.Delete,
                        contentColor = MaterialTheme.colorScheme.error,
                        onClick = { showConfirmDialog = true })
                )
            )
        }
    }
}

@Composable
fun CharacterMetadata(
    race: String,
    charClass: String,
    level: Int,
    modifier: Modifier = Modifier,
    classRaceModifier: Modifier,
) {
    val characterInfo = remember(race, charClass) {
        listOf(race, charClass).filter { it.isNotBlank() }.joinToString(" — ")
    }
    Column(modifier = modifier) {
        if (characterInfo.isNotBlank()) {
            Text(
                text = characterInfo,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = classRaceModifier
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = "${stringResource(R.string.spell_level)} $level",
            style = MaterialTheme.typography.labelLarge
        )
    }
}
