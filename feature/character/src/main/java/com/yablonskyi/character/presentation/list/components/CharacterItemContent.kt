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
fun CharacterItemImage(
    imagePath: String?,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape? = null
) {
    Surface(
        modifier = modifier.then(if (shape != null) Modifier.clip(shape) else Modifier),
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
            Icon(
                imageVector = Icons.Default.NoPhotography,
                contentDescription = "Fallback profile",
                modifier = Modifier
                    .padding(48.dp)
                    .fillMaxSize()
            )
        }
    }
}

@Composable
fun CharacterItemTrailingAction(
    isSelected: Boolean,
    isSelectionMode: Boolean,
    characterName: String,
    onToggleSelection: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (isSelectionMode) {
        Box(
            modifier = modifier
        ) {
            Checkbox(checked = isSelected, onCheckedChange = { onToggleSelection() })
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
