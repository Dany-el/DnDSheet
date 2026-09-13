package com.yablonskyi.character.presentation.list.components

import com.yablonskyi.character.platform.print.normalizePrintLanguage
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NoPhotography
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
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewDynamicColors
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import coil.compose.AsyncImage
import com.yablonskyi.character.presentation.common.UiUtils
import com.yablonskyi.model.character.Character
import com.yablonskyi.model.character.CharacterSheet
import com.yablonskyi.ui.R
import com.yablonskyi.ui.settings.ListView
import com.yablonskyi.ui.theme.DnDSheetTheme
import com.yablonskyi.ui.utils.DeletingItemConfirmDialog
import com.yablonskyi.ui.utils.LoadingDialog
import com.yablonskyi.ui.utils.SlicedDropdownMenu
import com.yablonskyi.ui.utils.SlicedMenuItem
import com.yablonskyi.ui.utils.SelectionBottomBar
import com.yablonskyi.character.presentation.list.*

@Composable
fun SharedTransitionScope.CharacterListItem(
    character: Character,
    defaultTopCorners: Dp,
    defaultBottomCorners: Dp,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    itemActions: CharacterItemActions,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier
) {
    val animatedContainerColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceContainerHighest,
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
        topEnd = 0.dp,
        bottomEnd = 0.dp
    )

    Card(
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            CharacterItemImage(
                imagePath = character.imagePath,
                shape = imageShape,
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
                        isSelected = isSelected,
                        isSelectionMode = isSelectionMode,
                        characterName = character.name,
                        onToggleSelection = itemActions.onToggleSelection,
                        onExport = itemActions.onExport,
                        onDelete = itemActions.onDelete,
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
