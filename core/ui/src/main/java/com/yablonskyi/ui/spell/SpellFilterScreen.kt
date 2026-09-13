package com.yablonskyi.ui.spell

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yablonskyi.model.character.MagicSchool
import com.yablonskyi.model.character.SpellCastTime
import com.yablonskyi.model.character.SpellDuration
import com.yablonskyi.model.character.SpellLevel
import com.yablonskyi.ui.R
import com.yablonskyi.ui.theme.Dimens
import com.yablonskyi.ui.theme.DnDSheetTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpellFilterScreen(
    filterState: SpellFilterState,
    onIntent: (SpellsIntent) -> Unit,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    BackHandler(isVisible) {
        onIntent(SpellsIntent.ToggleFiltersExpanded)
    }

    val elevatedSurfaceColor =
        MaterialTheme.colorScheme.surfaceColorAtElevation(Dimens.TopBar.Elevation)

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Column {
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    title = {
                        Text(
                            text = stringResource(R.string.filters),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                onIntent(SpellsIntent.ToggleFiltersExpanded)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.close)
                            )
                        }
                    },
                    actions = {
                        AnimatedVisibility(
                            visible = filterState.isActive,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            TextButton(onClick = { onIntent(SpellsIntent.ClearAllFilters) }) {
                                Text(
                                    stringResource(R.string.clear_all).uppercase(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = elevatedSurfaceColor,
                        scrolledContainerColor = elevatedSurfaceColor,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                item {
                    SpellFilterSection(
                        title = stringResource(R.string.filter_properties),
                        count = listOf(
                            filterState.onlyConcentration,
                            filterState.onlyRitual
                        ).count { it },
                    ) { innerModifier ->
                        FilterCheckboxRow(
                            label = stringResource(R.string.concentration),
                            checked = filterState.onlyConcentration,
                            onToggle = { onIntent(SpellsIntent.ToggleConcentration) },
                            modifier = innerModifier
                        )
                        FilterCheckboxRow(
                            label = stringResource(R.string.ritual),
                            checked = filterState.onlyRitual,
                            onToggle = { onIntent(SpellsIntent.ToggleRitual) },
                            modifier = innerModifier
                        )
                    }
                }

                item {
                    SpellFilterSection(
                        title = stringResource(R.string.spell_level),
                        activeCount = filterState.levels.size,
                        entries = SpellLevel.entries,
                        selectedEntries = filterState.levels,
                        onToggle = { onIntent(SpellsIntent.ToggleLevelFilter(it)) },
                        label = { stringResource(it.resId) }
                    )
                }

                item {
                    SpellFilterSection(
                        title = stringResource(R.string.msg_school),
                        activeCount = filterState.schools.size,
                        entries = MagicSchool.entries,
                        selectedEntries = filterState.schools,
                        onToggle = { onIntent(SpellsIntent.ToggleSchoolFilter(it)) },
                        label = { stringResource(it.resId) }
                    )
                }

                item {
                    SpellFilterSection(
                        title = stringResource(R.string.msg_duration),
                        activeCount = filterState.durations.size,
                        entries = SpellDuration.entries,
                        selectedEntries = filterState.durations,
                        onToggle = { onIntent(SpellsIntent.ToggleDurationFilter(it)) },
                        label = { stringResource(it.resId) }
                    )
                }

                item {
                    SpellFilterSection(
                        title = stringResource(R.string.msg_casting_time),
                        activeCount = filterState.castTimes.size,
                        entries = SpellCastTime.entries,
                        selectedEntries = filterState.castTimes,
                        onToggle = { onIntent(SpellsIntent.ToggleCastTimeFilter(it)) },
                        label = { stringResource(it.resId) }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SpellFilterScreenPreview() {
    DnDSheetTheme {
        Row(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f)) {
                Surface(
                    color = Color.Black.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxSize()
                ) { }
            }
            SpellFilterScreen(
                filterState = SpellFilterState(
                    levels = setOf(SpellLevel.LEVEL_2)
                ),
                onIntent = { _ -> },
                isVisible = true,
                modifier = Modifier.fillMaxWidth(0.7f)
            )
        }
    }
}