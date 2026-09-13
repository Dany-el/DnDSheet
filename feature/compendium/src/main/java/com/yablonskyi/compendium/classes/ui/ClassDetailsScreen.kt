package com.yablonskyi.compendium.classes.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.window.core.layout.WindowSizeClass
import com.yablonskyi.compendium.classes.utils.PreviewUtils
import com.yablonskyi.compendium.classes.viewmodel.ClassDetailsIntent
import com.yablonskyi.compendium.classes.viewmodel.ClassDetailsUiState
import com.yablonskyi.ui.R
import com.yablonskyi.ui.theme.Dimens
import com.yablonskyi.ui.utils.EditFAB
import com.yablonskyi.ui.utils.InfoChip
import com.yablonskyi.ui.utils.OutlinedInfoChip
import com.yablonskyi.ui.utils.PreviewThemeWrapper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassDetailsScreen(
    uiState: ClassDetailsUiState,
    snackbarHostState: SnackbarHostState,
    onIntent: (ClassDetailsIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val characterClass = uiState.selectedClass ?: return

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
                            text = characterClass.name,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            textAlign = if (isWideScreen) TextAlign.Center else TextAlign.Left,
                            modifier = Modifier.weight(1f)
                        )
                        if (characterClass.isHomebrew) {
                            InfoChip(
                                text = stringResource(R.string.homebrew),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(end = Dimens.Spacing.Small)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onIntent(ClassDetailsIntent.NavigateBack) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    if (characterClass.isHomebrew) {
                        IconButton(
                            onClick = { onIntent(ClassDetailsIntent.ShareRequested) }
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
            if (characterClass.isHomebrew) {
                EditFAB(
                    onClick = { onIntent(ClassDetailsIntent.Edit) }
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
                modifier = Modifier
                    .widthIn(max = Dimens.Content.MaxWidth)
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
            ) {
                // Stat cards – Hit Dice / Primary Ability
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(Dimens.Spacing.Medium),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing.Medium),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ClassStatCard(
                                label = stringResource(R.string.hit_dice),
                                value = characterClass.hitDice,
                                modifier = Modifier.weight(1f)
                            )

                            ClassStatCard(
                                label = stringResource(R.string.skill_choices),
                                value = characterClass.skillChoiceCount.toString(),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        ClassStatCard(
                            label = stringResource(R.string.primary_ability),
                            value = stringResource(characterClass.primaryAbility.nameRes).uppercase(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Saving Throws
                if (characterClass.savingThrows.isNotEmpty()) {
                    item {
                        ClassDetailsSection(title = stringResource(R.string.saving_throws)) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing.Small),
                                verticalArrangement = Arrangement.spacedBy(Dimens.Spacing.XSmall)
                            ) {
                                characterClass.savingThrows.forEach { ability ->
                                    OutlinedInfoChip(
                                        text = stringResource(ability.nameRes)
                                            .take(3).uppercase()
                                    )
                                }
                            }
                        }
                    }
                }

                // Spellcasting Ability
                characterClass.spellcastingAbility?.let { spellAbility ->
                    item {
                        ClassDetailsSection(title = stringResource(R.string.spellcasting_ability)) {
                            InfoChip(
                                text = stringResource(spellAbility.nameRes)
                            )
                        }
                    }
                }

                // Available Skills
                if (characterClass.availableSkills.isNotEmpty()) {
                    item {
                        ClassDetailsSection(title = stringResource(R.string.available_skills)) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing.Small),
                                verticalArrangement = Arrangement.spacedBy(Dimens.Spacing.XSmall)
                            ) {
                                characterClass.availableSkills.forEach { skill ->
                                    InfoChip(
                                        text = stringResource(skill.nameRes)
                                    )
                                }
                            }
                        }
                    }
                }

                // Description
                if (characterClass.description.isNotBlank()) {
                    item {
                        ClassDetailsSection(title = stringResource(R.string.description)) {
                            Text(
                                text = characterClass.description,
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
private fun ClassStatCard(
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
private fun ClassDetailsSection(
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

@Preview
@Composable
private fun ClassDetailsScreenPreview() {
    PreviewThemeWrapper.Preview {
        ClassDetailsScreen(
            uiState = PreviewUtils.detailsState,
            snackbarHostState = SnackbarHostState(),
            onIntent = {}
        )
    }
}