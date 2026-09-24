package com.yablonskyi.character.presentation.list.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.yablonskyi.character.presentation.list.CharacterItemActions
import com.yablonskyi.model.character.Character
import com.yablonskyi.ui.components.RoundedCheckBox
import com.yablonskyi.ui.theme.DnDSheetTheme
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun SharedTransitionScope.CharacterListItem(
    character: Character,
    defaultTopCorners: Dp,
    defaultBottomCorners: Dp,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    isDragging: Boolean,
    reorderScope: ReorderableCollectionItemScope,
    itemActions: CharacterItemActions,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier
) {
    val selectionProgress by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(durationMillis = 250),
        label = "characterImageSelection",
    )
    val elevation by animateDpAsState(
        targetValue = if (isDragging) 16.dp else 0.dp,
        label = "characterDragElevation",
    )
    val animatedContainerColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer
        else Color.Transparent,
        label = "cardColorAnimation"
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

    val imageShape = RoundedCornerShape(
        topStart = topCorners,
        bottomStart = bottomCorners,
        topEnd = topCorners,
        bottomEnd = bottomCorners
    )

    OutlinedCard(
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = CardDefaults.cardColors(containerColor = animatedContainerColor),
        shape = animatedShape,
        modifier = modifier
            .height(140.dp)
            .wrapContentHeight()
            .clip(animatedShape)
            .combinedClickable(
                onClick = itemActions.onClick,
                onLongClick = itemActions.onLongClick
            )
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            if (isSelectionMode) {
                RoundedCheckBox(
                    isChecked = isSelected,
                    onCheckChange = { itemActions.onToggleSelection() })
            }

            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                CharacterItemImage(
                    imagePath = character.imagePath,
                    shape = imageShape,
                    selectionProgress = selectionProgress,
                    modifier = Modifier
                        .width(120.dp)
                        .height(140.dp)
                        .sharedBounds(
                            sharedContentState = rememberSharedContentState(key = "image_${character.id}"),
                            animatedVisibilityScope = animatedVisibilityScope,
                            resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
                            clipInOverlayDuringTransition = OverlayClip(imageShape)
                        )
                )
                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    Row {
                        Text(
                            text = character.name,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier
                                .weight(1f)
                                .align(Alignment.CenterVertically)
                                .sharedBounds(
                                    sharedContentState = rememberSharedContentState(key = "name_${character.id}"),
                                    animatedVisibilityScope = animatedVisibilityScope,
                                    resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
                                )
                        )
                        CharacterItemTrailingAction(
                            isSelectionMode = isSelectionMode,
                            reorderScope = reorderScope,
                            characterName = character.name,
                            onExport = itemActions.onExport,
                            onDelete = itemActions.onDelete,
                            onReorderFinished = itemActions.onReorderFinished,
                            modifier = Modifier.weight(0.2f)
                        )
                    }
                    CharacterMetadata(
                        race = character.race,
                        charClass = character.charClass,
                        level = character.level,
                        classRaceModifier = Modifier
                            .sharedBounds(
                                sharedContentState = rememberSharedContentState(key = "class_${character.id}"),
                                animatedVisibilityScope = animatedVisibilityScope,
                                resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                            )
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun CharacterListItemPreview_Default() {
    val listState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(listState) { _, _ -> }
    val character = Character(
        id = 1L,
        name = "Aelar Moonbrook",
        race = "High Elf",
        charClass = "Wizard",
        level = 7,
    )
    val actions = CharacterItemActions(
        onClick = {},
        onLongClick = {},
        onToggleSelection = {},
        onDelete = {},
        onExport = {},
        onReorderFinished = {},
    )

    DnDSheetTheme {
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
                val visibilityScope = this
                LazyColumn(
                    state = listState,
                    modifier = Modifier.padding(16.dp),
                ) {
                    item(key = character.id) {
                        ReorderableItem(reorderState, key = character.id) { isDragging ->
                            CharacterListItem(
                                character = character,
                                defaultTopCorners = 16.dp,
                                defaultBottomCorners = 16.dp,
                                isSelected = false,
                                isSelectionMode = false,
                                isDragging = isDragging,
                                reorderScope = this,
                                itemActions = actions,
                                animatedVisibilityScope = visibilityScope,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun CharacterListItemPreview_Selected() {
    val listState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(listState) { _, _ -> }
    val character = Character(
        id = 1L,
        name = "Aelar Moonbrook",
        race = "High Elf",
        charClass = "Wizard",
        level = 7,
    )
    val actions = CharacterItemActions(
        onClick = {},
        onLongClick = {},
        onToggleSelection = {},
        onDelete = {},
        onExport = {},
        onReorderFinished = {},
    )

    DnDSheetTheme {
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
                val visibilityScope = this
                LazyColumn(
                    state = listState,
                    modifier = Modifier.padding(16.dp),
                ) {
                    item(key = character.id) {
                        ReorderableItem(reorderState, key = character.id) { isDragging ->
                            CharacterListItem(
                                character = character,
                                defaultTopCorners = 16.dp,
                                defaultBottomCorners = 16.dp,
                                isSelected = true,
                                isSelectionMode = true,
                                isDragging = isDragging,
                                reorderScope = this,
                                itemActions = actions,
                                animatedVisibilityScope = visibilityScope,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }
    }
}