package com.yablonskyi.dndsheet.ui.wizard

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yablonskyi.dndsheet.R
import com.yablonskyi.dndsheet.data.model.character.Ability
import com.yablonskyi.dndsheet.data.model.rulebook.Race
import com.yablonskyi.dndsheet.ui.character.slides.formatModifier
import com.yablonskyi.dndsheet.ui.theme.DnDSheetTheme
import com.yablonskyi.dndsheet.ui.wizard.CharacterCreationWizardViewModel.Companion.CORE_ABILITIES
import com.yablonskyi.dndsheet.ui.wizard.CharacterCreationWizardViewModel.Companion.POINT_BUY_BUDGET
import com.yablonskyi.dndsheet.ui.wizard.CharacterCreationWizardViewModel.Companion.POINT_BUY_COSTS
import kotlin.math.floor

@Composable
fun WizardAbilitiesStep(
    method: AbilityMethod,
    selectedRace: Race?,
    // Standard Array
    standardAssignments: Map<Ability, Int>,
    pendingPoolValue: Int?,
    // Point Buy
    pointBuyScores: Map<Ability, Int>,
    pointsSpent: Int,
    // Roll
    rolledResults: List<Int>,
    rollIndexAssignments: Map<Ability, Int>,
    pendingRollIndex: Int?,
    // Actions
    onMethodChange: (AbilityMethod) -> Unit,
    onSelectPoolValue: (Int) -> Unit,
    onAssignToAbility: (Ability) -> Unit,
    onUnassignAbility: (Ability) -> Unit,
    onIncrementPB: (Ability) -> Unit,
    onDecrementPB: (Ability) -> Unit,
    onRollAll: () -> Unit,
    onSelectRollIndex: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val standardArrayPool = listOf(15, 14, 13, 12, 10, 8)
    val usedPoolValues = standardAssignments.values.toSet()

    Column(modifier = modifier.fillMaxSize()) {

        // ── Method selector tabs ───────────────────────────────────────────
        SecondaryTabRow(
            selectedTabIndex = method.ordinal,
            modifier = Modifier.fillMaxWidth()
        ) {
            AbilityMethod.entries.forEach { m ->
                Tab(
                    selected = method == m,
                    onClick = { onMethodChange(m) },
                    text = {
                        Text(
                            when (m) {
                                AbilityMethod.STANDARD_ARRAY -> stringResource(R.string.ability_method_standard)
                                AbilityMethod.POINT_BUY -> stringResource(R.string.ability_method_pointbuy)
                                AbilityMethod.ROLL -> stringResource(R.string.ability_method_roll)
                            },
                            maxLines = 1
                        )
                    }
                )
            }
        }

        when (method) {

            AbilityMethod.STANDARD_ARRAY -> {

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(8.dp)
                ) {
                    item {
                        Text(
                            text = stringResource(R.string.ability_pool_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            standardArrayPool.forEach { value ->
                                val isUsed = value in usedPoolValues
                                val isSelected = value == pendingPoolValue
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { if (!isUsed) onSelectPoolValue(value) },
                                    enabled = !isUsed,
                                    label = { Text("$value") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }
                    }
                    itemsIndexed(items = CORE_ABILITIES) { index, ability ->
                        val assigned = standardAssignments[ability]
                        val racial = selectedRace?.abilityBonuses?.get(ability) ?: 0

                        val topCorners = if (index == 0) 16.dp else 4.dp
                        val bottomCorners = if (index == CORE_ABILITIES.lastIndex) 16.dp else 4.dp

                        val shape = RoundedCornerShape(
                            topStart = topCorners,
                            topEnd = topCorners,
                            bottomStart = bottomCorners,
                            bottomEnd = bottomCorners
                        )

                        AbilityAssignRow(
                            ability = ability,
                            baseScore = assigned,
                            racialBonus = racial,
                            isPending = pendingPoolValue != null,
                            shape = shape,
                            onClick = {
                                if (assigned != null) onUnassignAbility(ability)
                                else if (pendingPoolValue != null) onAssignToAbility(ability)
                            }
                        )
                    }
                }
            }

            AbilityMethod.POINT_BUY -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(8.dp)
                ) {
                    item {
                        val remaining = POINT_BUY_BUDGET - pointsSpent
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.points_remaining, remaining),
                                style = MaterialTheme.typography.titleSmall,
                                color = if (remaining == 0) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface
                            )
                            LinearProgressIndicator(
                                progress = { pointsSpent / POINT_BUY_BUDGET.toFloat() },
                                modifier = Modifier
                                    .width(100.dp)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )
                        }
                    }

                    itemsIndexed(items = CORE_ABILITIES) { index, ability ->
                        val score = pointBuyScores[ability] ?: 8
                        val racial = selectedRace?.abilityBonuses?.get(ability) ?: 0
                        val canInc = score < 15 && (POINT_BUY_BUDGET - pointsSpent) >=
                                ((POINT_BUY_COSTS[score + 1] ?: 0) - (POINT_BUY_COSTS[score] ?: 0))
                        val canDec = score > 8

                        val topCorners = if (index == 0) 16.dp else 4.dp
                        val bottomCorners = if (index == CORE_ABILITIES.lastIndex) 16.dp else 4.dp

                        val shape = RoundedCornerShape(
                            topStart = topCorners,
                            topEnd = topCorners,
                            bottomStart = bottomCorners,
                            bottomEnd = bottomCorners
                        )

                        AbilityPointBuyRow(
                            ability = ability,
                            racialBonus = racial,
                            canInc = canInc,
                            canDec = canDec,
                            baseScore = score,
                            shape = shape,
                            onDecrementPB = onDecrementPB,
                            onIncrementPB = onIncrementPB
                        )
                    }
                }
            }

            AbilityMethod.ROLL -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(8.dp)
                ) {
                    item {
                        Button(
                            onClick = onRollAll,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Icon(
                                Icons.Default.Casino, contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (rolledResults.isEmpty()) stringResource(R.string.roll_abilities)
                                else stringResource(R.string.reroll_all)
                            )
                        }
                    }
                    if (rolledResults.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.roll_select_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            val usedIndices = rollIndexAssignments.values.toSet()
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                rolledResults.forEachIndexed { index, value ->
                                    val isUsed = index in usedIndices
                                    val isSelected = index == pendingRollIndex
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { if (!isUsed) onSelectRollIndex(index) },
                                        enabled = !isUsed,
                                        label = { Text("$value") },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    )
                                }
                            }
                        }
                        // Result chips
                        itemsIndexed(items = CORE_ABILITIES) { index, ability ->
                            val assigned = if (rollIndexAssignments.containsKey(ability))
                                rolledResults.getOrNull(rollIndexAssignments[ability]!!) else null
                            val racial = selectedRace?.abilityBonuses?.get(ability) ?: 0

                            val topCorners = if (index == 0) 16.dp else 4.dp
                            val bottomCorners =
                                if (index == CORE_ABILITIES.lastIndex) 16.dp else 4.dp

                            val shape = RoundedCornerShape(
                                topStart = topCorners,
                                topEnd = topCorners,
                                bottomStart = bottomCorners,
                                bottomEnd = bottomCorners
                            )

                            AbilityAssignRow(
                                ability = ability,
                                baseScore = assigned,
                                racialBonus = racial,
                                isPending = pendingRollIndex != null,
                                shape = shape,
                                onClick = {
                                    if (assigned != null) onUnassignAbility(ability) // pass through
                                    else if (pendingRollIndex != null) onAssignToAbility(ability)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AbilityAssignRow(
    ability: Ability,
    baseScore: Int?,       // null = unassigned
    racialBonus: Int,
    isPending: Boolean,    // a value is selected and awaiting assignment
    shape: RoundedCornerShape,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isAssigned = baseScore != null
    val containerColor by animateColorAsState(
        targetValue = when {
            isAssigned -> MaterialTheme.colorScheme.secondaryContainer
            isPending -> MaterialTheme.colorScheme.surfaceContainerHigh
            else -> MaterialTheme.colorScheme.surfaceContainerLow
        },
        label = "abilityRowBg"
    )

    Surface(
        shape = shape,
        color = containerColor,
        modifier = modifier
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(ability.nameRes).take(3).uppercase(),
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            if (isAssigned) {
                if (racialBonus != 0) {
                    Text(
                        text = "+$racialBonus",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .weight(1f),
                        textAlign = TextAlign.Center
                    )
                } else {
                    Spacer(
                        Modifier.weight(1f)
                    )
                }
                Text(
                    text = "${baseScore + racialBonus}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .weight(1f),
                    textAlign = TextAlign.Center
                )

                val abilityMod = floor((baseScore + racialBonus - 10) / 2.0).toInt()

                Text(
                    text = formatModifier(abilityMod),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .weight(1f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "$baseScore",
                    modifier = Modifier
                        .weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    Icons.Default.Close,
                    contentDescription = stringResource(R.string.unassign),
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                if (!isPending) {
                    if (racialBonus != 0) {
                        Text(
                            text = "+$racialBonus",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .weight(1f),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Spacer(
                            Modifier
                                .weight(1f)
                        )
                    }
                    repeat(3) {
                        Spacer(Modifier.weight(1f))
                    }
                }
                Text(
                    text = if (isPending) stringResource(R.string.tap_to_assign) else "—",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(min = 16.dp)
                )
            }
        }
    }
}

@Composable
fun AbilityPointBuyRow(
    ability: Ability,
    racialBonus: Int,
    baseScore: Int,
    shape: RoundedCornerShape,
    canDec: Boolean,
    canInc: Boolean,
    onIncrementPB: (Ability) -> Unit,
    onDecrementPB: (Ability) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(ability.nameRes).take(3).uppercase(),
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium
            )
            if (racialBonus != 0) {
                Text(
                    text = "+$racialBonus",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .weight(1f),
                    textAlign = TextAlign.Center
                )
            } else {
                Spacer(
                    Modifier
                        .weight(1f)
                )
            }
            Text(
                text = "${baseScore + racialBonus}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .weight(1f),
                textAlign = TextAlign.Center
            )

            val abilityMod = floor((baseScore + racialBonus - 10) / 2.0).toInt()

            Text(
                text = formatModifier(abilityMod),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .weight(1f),
                textAlign = TextAlign.Center
            )
            IconButton(
                onClick = { onDecrementPB(ability) },
                enabled = canDec,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        CircleShape
                    )
                    .size(32.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = null)
            }
            Text(
                text = "$baseScore",
                modifier = Modifier.width(32.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            IconButton(
                onClick = { onIncrementPB(ability) },
                enabled = canInc,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        CircleShape
                    )
                    .size(32.dp)
            ) {
                Icon(
                    Icons.Default.Add, contentDescription = null
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = false, backgroundColor = 0xFFE7E2E2)
@Composable
private fun WizardAbilitiesStepPreview_POINT_BUY() {
    DnDSheetTheme {
        WizardAbilitiesStep(
            method = AbilityMethod.POINT_BUY,
            selectedRace = Race(
                id = "human",
                name = "Human",
                abilityBonuses = mapOf(
                    Ability.CON to 2
                )
            ),
            standardAssignments = emptyMap(),
            pendingPoolValue = null,
            pointBuyScores = mapOf(
                Ability.INT to 15
            ),
            pointsSpent = 5,
            rolledResults = emptyList(),
            rollIndexAssignments = emptyMap(),
            pendingRollIndex = null,
            onMethodChange = { },
            onSelectPoolValue = {},
            onAssignToAbility = {},
            onUnassignAbility = {},
            onIncrementPB = {},
            onDecrementPB = {},
            onRollAll = {},
            onSelectRollIndex = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = false, backgroundColor = 0xFFE7E2E2)
@Composable
private fun WizardAbilitiesStepPreview_STANDARD() {
    DnDSheetTheme {
        WizardAbilitiesStep(
            method = AbilityMethod.STANDARD_ARRAY,
            selectedRace = Race(
                id = "human",
                name = "Human",
                abilityBonuses = mapOf(
                    Ability.INT to 2,
                    Ability.DEX to 1,
                )
            ),
            standardAssignments = mapOf(
                Ability.INT to 15,
                Ability.CHA to 8,
            ),
            pendingPoolValue = 2,
            pointBuyScores = mapOf(
                Ability.INT to 15
            ),
            pointsSpent = 5,
            rolledResults = emptyList(),
            rollIndexAssignments = emptyMap(),
            pendingRollIndex = null,
            onMethodChange = { },
            onSelectPoolValue = {},
            onAssignToAbility = {},
            onUnassignAbility = {},
            onIncrementPB = {},
            onDecrementPB = {},
            onRollAll = {},
            onSelectRollIndex = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = false, backgroundColor = 0xFFE7E2E2)
@Composable
private fun WizardAbilitiesStepPreview_RANDOM() {
    DnDSheetTheme {
        WizardAbilitiesStep(
            method = AbilityMethod.ROLL,
            selectedRace = Race(
                id = "human",
                name = "Human",
                abilityBonuses = mapOf(
                    Ability.INT to 2
                )
            ),
            standardAssignments = emptyMap(),
            pendingPoolValue = null,
            pointBuyScores = mapOf(
                Ability.INT to 15
            ),
            pointsSpent = 5,
            rolledResults = listOf(
                10, 15, 8, 10, 12, 16
            ),
            rollIndexAssignments = emptyMap(),
            pendingRollIndex = null,
            onMethodChange = { },
            onSelectPoolValue = {},
            onAssignToAbility = {},
            onUnassignAbility = {},
            onIncrementPB = {},
            onDecrementPB = {},
            onRollAll = {},
            onSelectRollIndex = {},
        )
    }
}