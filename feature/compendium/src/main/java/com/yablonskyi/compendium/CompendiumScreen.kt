package com.yablonskyi.compendium

import androidx.compose.foundation.layout.widthIn
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import com.yablonskyi.ui.R
import com.yablonskyi.ui.theme.Dimens
import com.yablonskyi.ui.utils.PreviewThemeWrapper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompendiumScreen(
    onRacesClick: () -> Unit,
    onClassesClick: () -> Unit,
    onSpellsClick: () -> Unit,
    racesCount: Int,
    classesCount: Int,
    spellsCount: Int,
    modifier: Modifier = Modifier
) {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val isWideScreen =
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.compendium),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        textAlign = if (isWideScreen) TextAlign.Center else TextAlign.Left,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(Dimens.TopBar.Elevation),
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(Dimens.TopBar.Elevation),
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(180.dp),
                verticalArrangement = Arrangement.spacedBy(Dimens.Spacing.Small),
                horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing.Small),
                contentPadding = PaddingValues(
                    start = Dimens.Spacing.Small,
                    end = Dimens.Spacing.Small,
                    top = Dimens.Spacing.Small,
                    bottom = innerPadding.calculateBottomPadding()
                ),
                modifier = Modifier.widthIn(max = Dimens.Content.MaxWidth)
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
            ) {
                items(items = CompendiumScreenCells.entries) { cell ->
                    val count = when (cell) {
                        CompendiumScreenCells.RACES -> racesCount
                        CompendiumScreenCells.CLASSES -> classesCount
                        CompendiumScreenCells.SPELLS -> spellsCount
                    }

                    CompendiumCard(
                        cell = cell,
                        count = count,
                        onClick = {
                            when (cell) {
                                CompendiumScreenCells.RACES -> onRacesClick()
                                CompendiumScreenCells.CLASSES -> onClassesClick()
                                CompendiumScreenCells.SPELLS -> onSpellsClick()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CompendiumCard(
    cell: CompendiumScreenCells,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.outlinedCardColors(
            containerColor = CardDefaults.outlinedCardColors().containerColor
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.Spacing.Large),
            verticalArrangement = Arrangement.spacedBy(Dimens.Spacing.Medium)
        ) {
            // Icon in a tinted circle
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = cell.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Category name
            Text(
                text = stringResource(cell.string),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )

            // Entry count
            Text(
                text = pluralStringResource(cell.countLabel, count, count),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

enum class CompendiumScreenCells(
    @param:StringRes val string: Int,
    val icon: ImageVector,
    @param:PluralsRes val countLabel: Int
) {
    RACES(R.string.races, Icons.Default.People, R.plurals.races_count),
    CLASSES(R.string.classes, Icons.Default.Shield, R.plurals.classes_count),
    SPELLS(R.string.spells, Icons.Default.AutoAwesome, R.plurals.spells_count)
}

@Preview
@Composable
private fun CompendiumScreenPreview() {
    PreviewThemeWrapper.Preview {
        CompendiumScreen(
            onRacesClick = {},
            onSpellsClick = {},
            onClassesClick = {},
            racesCount = 20,
            classesCount = 10,
            spellsCount = 30
        )
    }
}