package com.yablonskyi.dndsheet.ui.wizard

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yablonskyi.dndsheet.R
import com.yablonskyi.dndsheet.data.model.rulebook.Race
import com.yablonskyi.dndsheet.ui.theme.DnDSheetTheme
import com.yablonskyi.dndsheet.ui.utils.UiUtils

@Composable
fun WizardRaceStep(
    origRaces: List<Race>,
    homebrewRaces: List<Race>,
    query: String,
    onQueryChange: (String) -> Unit,
    selectedRace: Race?,
    onRaceSelected: (Race) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        stickyHeader {
            Surface(
                modifier = modifier
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    placeholder = { Text(stringResource(R.string.search)) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = null)
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = MaterialTheme.shapes.medium,
                )
            }
        }

        item { WizardSectionHeader(title = stringResource(R.string.player_handbook)) }

        itemsIndexed(items = origRaces, key = { _, r -> r.id }) { index, race ->

            val topCorners = if (origRaces.size == 1 || index == 0) 16.dp else 4.dp
            val bottomCorners =
                if (origRaces.size == 1 || index == origRaces.lastIndex) 16.dp else 4.dp

            val shape = RoundedCornerShape(
                topStart = topCorners,
                topEnd = topCorners,
                bottomStart = bottomCorners,
                bottomEnd = bottomCorners
            )

            RaceCard(
                race = race,
                isSelected = race == selectedRace,
                shape = shape,
                onClick = { onRaceSelected(race) })
        }

        item { WizardSectionHeader(title = stringResource(R.string.homebrew)) }

        if (homebrewRaces.isEmpty()) {
            item {
                Text(
                    text = if (query.isBlank()) stringResource(R.string.wizard_no_homebrew_races)
                    else stringResource(R.string.no_results),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                )
            }
        } else {
            itemsIndexed(items = homebrewRaces, key = { _, r -> r.id }) { index, race ->
                val topCorners = if (homebrewRaces.size == 1 || index == 0) 16.dp else 4.dp
                val bottomCorners =
                    if (homebrewRaces.size == 1 || index == homebrewRaces.lastIndex) 16.dp else 4.dp

                val shape = RoundedCornerShape(
                    topStart = topCorners,
                    topEnd = topCorners,
                    bottomStart = bottomCorners,
                    bottomEnd = bottomCorners
                )

                RaceCard(
                    race = race,
                    isSelected = race == selectedRace,
                    shape = shape,
                    onClick = { onRaceSelected(race) })
            }
        }
    }
}

@Composable
fun RaceCard(
    race: Race,
    isSelected: Boolean,
    shape: RoundedCornerShape,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "raceBorder"
    )
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceContainerLow,
        label = "raceContainer"
    )

    val resources = LocalResources.current

    Card(
        onClick = onClick,
        shape = shape,
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = borderColor,
                shape = shape
            ),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = race.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            // Size + Speed chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 6.dp)
            ) {
                WizardChip(label = race.size)
                WizardChip(label = "${race.speed}${stringResource(R.string.feets)}")
                if (race.isHomebrew) WizardChip(label = stringResource(R.string.homebrew))
            }
            // Ability bonuses
            if (race.abilityBonuses.isNotEmpty()) {
                val bonusText = race.abilityBonuses.entries
                    .joinToString("  ") { (ability, bonus) ->
                        "${
                            resources.getText(ability.nameRes).take(3).toString().uppercase()
                        } +$bonus"
                    }
                Text(
                    text = bonusText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            // Granted skills
            if (race.grantedSkills.isNotEmpty()) {
                val skillText = race.grantedSkills
                    .joinToString(", ") { resources.getText(it.nameRes) }
                Text(
                    text = stringResource(R.string.wizard_granted_skills, skillText),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            // Traits
            if (race.traits.isNotEmpty()) {
                Text(
                    text = race.traits.joinToString(" - "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Preview
@Composable
private fun WizardRaceStepPreview() {
    DnDSheetTheme {
        WizardRaceStep(
            origRaces = UiUtils.sampleRaces.filter { !it.isHomebrew },
            homebrewRaces = UiUtils.sampleRaces.filter { it.isHomebrew },
            query = "",
            onQueryChange = { },
            selectedRace = UiUtils.sampleRaces.first(),
            onRaceSelected = { }
        )
    }
}