package com.yablonskyi.character.presentation.list.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yablonskyi.character.presentation.list.CharacterItemActions
import com.yablonskyi.model.character.Character
import sh.calvin.reorderable.ReorderableCollectionItemScope

@Composable
fun SharedTransitionScope.CharacterGridItem(
    character: Character,
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
        else MaterialTheme.colorScheme.surfaceContainerHighest,
        label = "cardColorAnimation"
    )

    val imageShape = RoundedCornerShape(
        topStart = 8.dp,
        topEnd = 8.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = CardDefaults.cardColors(containerColor = animatedContainerColor),
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .combinedClickable(
                onClick = itemActions.onClick,
                onLongClick = itemActions.onLongClick
            )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box {
                CharacterItemImage(
                    imagePath = character.imagePath,
                    shape = imageShape,
                    selectionProgress = selectionProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 180.dp)
                        .sharedBounds(
                            sharedContentState = rememberSharedContentState(key = "image_${character.id}"),
                            animatedVisibilityScope = animatedVisibilityScope,
                            resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
                            clipInOverlayDuringTransition = OverlayClip(imageShape)
                        ),
                )
                CharacterItemTrailingAction(
                    isSelectionMode = isSelectionMode,
                    reorderScope = reorderScope,
                    characterName = character.name,
                    onExport = itemActions.onExport,
                    onDelete = itemActions.onDelete,
                    onReorderFinished = itemActions.onReorderFinished,
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                Text(
                    text = character.name,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.sharedBounds(
                        sharedContentState = rememberSharedContentState(key = "name_${character.id}"),
                        animatedVisibilityScope = animatedVisibilityScope,
                        resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                    )
                )
                Spacer(Modifier.height(16.dp))
                CharacterMetadata(
                    race = character.race,
                    charClass = character.charClass,
                    level = character.level,
                    classRaceModifier = Modifier.sharedBounds(
                        sharedContentState = rememberSharedContentState(key = "class_${character.id}"),
                        animatedVisibilityScope = animatedVisibilityScope,
                        resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                    )
                )
            }
        }
    }
}
