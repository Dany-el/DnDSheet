package com.yablonskyi.dndsheet.ui.compendium.classes

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.window.core.layout.WindowSizeClass
import com.yablonskyi.dndsheet.R
import com.yablonskyi.dndsheet.data.model.rulebook.CharacterClass
import com.yablonskyi.dndsheet.ui.theme.DnDSheetTheme
import com.yablonskyi.dndsheet.ui.utils.UiUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassDetailsScreen(
    characterClass: CharacterClass,
    onEdit: (String) -> Unit,
    onExportRequested: (String) -> String?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var jsonToWrite by remember { mutableStateOf<String?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { targetUri ->
            jsonToWrite?.let { json ->
                scope.launch(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(targetUri)?.use { outputStream ->
                        outputStream.write(json.toByteArray())
                    }
                }
            }
        }
    }

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val isWideScreen =
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                            SuggestionChip(
                                onClick = {},
                                label = {
                                    Text(
                                        text = stringResource(R.string.homebrew),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    if (characterClass.isHomebrew) {
                        IconButton(
                            onClick = {
                                jsonToWrite = onExportRequested(characterClass.id)
                                if (jsonToWrite != null) {
                                    val fileName = "${characterClass.name.replace(" ", "_")}.json"
                                    exportLauncher.launch(fileName)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Export"
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        floatingActionButton = {
            if (characterClass.isHomebrew) {
                FloatingActionButton(onClick = { onEdit(characterClass.id) }) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null)
                }
            }
        },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 8.dp,
                end = 8.dp,
                top = 12.dp,
                bottom = innerPadding.calculateBottomPadding() + 88.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            // Stat cards – Hit Dice / Primary Ability
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
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
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            characterClass.savingThrows.forEach { ability ->
                                SuggestionChip(
                                    onClick = {},
                                    label = {
                                        Text(
                                            stringResource(ability.nameRes)
                                                .take(3).uppercase()
                                        )
                                    }
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
                        SuggestionChip(
                            onClick = {},
                            label = { Text(stringResource(spellAbility.nameRes)) }
                        )
                    }
                }
            }

            // Available Skills
            if (characterClass.availableSkills.isNotEmpty()) {
                item {
                    ClassDetailsSection(title = stringResource(R.string.available_skills)) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            characterClass.availableSkills.forEach { skill ->
                                AssistChip(
                                    onClick = {},
                                    label = { Text(stringResource(skill.nameRes)) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            modifier = Modifier.size(AssistChipDefaults.IconSize),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
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
        verticalArrangement = Arrangement.spacedBy(8.dp)
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
    DnDSheetTheme() {
        Surface() {
            ClassDetailsScreen(
                characterClass = UiUtils.sampleClasses.first(),
                onEdit = {},
                onExportRequested = { "" },
                onNavigateBack = {}
            )
        }
    }
}