package com.yablonskyi.compendium.races.ui

import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.window.core.layout.WindowSizeClass
import com.yablonskyi.compendium.races.utils.PreviewUtils
import com.yablonskyi.compendium.races.viewmodel.RaceDetailsIntent
import com.yablonskyi.compendium.races.viewmodel.RaceDetailsUiState
import com.yablonskyi.model.character.Ability
import com.yablonskyi.model.rulebook.RaceSize
import com.yablonskyi.ui.R
import com.yablonskyi.ui.theme.Dimens
import com.yablonskyi.ui.utils.EditFAB
import com.yablonskyi.ui.utils.InfoChip
import com.yablonskyi.ui.utils.PreviewThemeWrapper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RaceDetailsScreen(
    uiState: RaceDetailsUiState,
    snackbarHostState: SnackbarHostState,
    onIntent: (RaceDetailsIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val race = uiState.selectedRace ?: return

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val isWideScreen =
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            LargeTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing.Small)
                    ) {
                        Text(
                            text = race.name,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            textAlign = if (isWideScreen) TextAlign.Center else TextAlign.Left,
                            modifier = Modifier.weight(1f)
                        )
                        if (race.isHomebrew) {
                            InfoChip(
                                text = stringResource(R.string.homebrew),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(end = Dimens.Spacing.Small)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onIntent(RaceDetailsIntent.NavigateBack) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    if (race.isHomebrew) {
                        IconButton(
                            onClick = { onIntent(RaceDetailsIntent.ShareRequested) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = stringResource(R.string.share_as_json)
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(Dimens.TopBar.Elevation),
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                        Dimens.TopBar.Elevation
                    ),
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            if (race.isHomebrew) {
                EditFAB(
                    onClick = { onIntent(RaceDetailsIntent.Edit) }
                )
            }
        },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { innerPadding ->
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = Dimens.Spacing.Small,
                    end = Dimens.Spacing.Small,
                    top = Dimens.Spacing.Medium,
                    bottom = innerPadding.calculateBottomPadding() + Dimens.Fab.BottomPadding
                ),
                verticalArrangement = Arrangement.spacedBy(Dimens.Spacing.Medium),
                modifier = Modifier.widthIn(max = Dimens.Content.MaxWidth)
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
            ) {
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing.Medium),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RaceStatCard(
                            label = stringResource(R.string.size),
                            value = stringResource(RaceSize.fromString(race.size).resId),
                            modifier = Modifier.weight(1f)
                        )
                        RaceStatCard(
                            label = stringResource(R.string.char_speed),
                            value = "${race.speed} ${stringResource(R.string.feets)}",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (race.abilityBonuses.isNotEmpty()) {
                    item {
                        RaceDetailsSection(title = stringResource(R.string.ability_score_bonuses)) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing.Small),
                                verticalArrangement = Arrangement.spacedBy(Dimens.Spacing.XSmall)
                            ) {
                                race.abilityBonuses.forEach { (ability, bonus) ->
                                    AbilityBonusChip(ability = ability, bonus = bonus)
                                }
                            }
                        }
                    }
                }

                if (race.grantedSkills.isNotEmpty()) {
                    item {
                        RaceDetailsSection(title = stringResource(R.string.granted_skills)) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing.Small),
                                verticalArrangement = Arrangement.spacedBy(Dimens.Spacing.XSmall)
                            ) {
                                race.grantedSkills.forEach { skill ->
                                    InfoChip(
                                        text = stringResource(skill.nameRes)
                                    )
                                }
                            }
                        }
                    }
                }

                if (race.traits.isNotEmpty()) {
                    item {
                        RaceDetailsSection(title = stringResource(R.string.traits)) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing.Small),
                                verticalArrangement = Arrangement.spacedBy(Dimens.Spacing.XSmall)
                            ) {
                                race.traits.forEach { trait ->
                                    InfoChip(
                                        text = trait
                                    )
                                }
                            }
                        }
                    }
                }

                if (race.description.isNotBlank()) {
                    item {
                        RaceDetailsSection(title = stringResource(R.string.description)) {
                            Text(
                                text = race.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.4
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RaceStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.Spacing.Large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.Spacing.XSmall)
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                letterSpacing = 1.sp
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun RaceDetailsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.Spacing.Small)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        content()
    }
}

@Composable
fun AbilityBonusChip(
    ability: Ability,
    bonus: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = Color.Transparent,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.padding()
    ) {
        Row(
            Modifier.padding(
                horizontal = Dimens.Spacing.Medium,
                vertical = Dimens.Spacing.Small
            )
        ) {
            Text(
                text = stringResource(ability.nameRes).take(3).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "+$bonus",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Preview
@Composable
private fun RaceDetailsScreenPreview() {
    PreviewThemeWrapper.Preview {
        RaceDetailsScreen(
            uiState = PreviewUtils.detailsState,
            snackbarHostState = SnackbarHostState(),
            onIntent = {}
        )
    }
}