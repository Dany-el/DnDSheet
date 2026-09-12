package com.yablonskyi.wizard

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Grid
import androidx.compose.foundation.layout.GridTrackSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.yablonskyi.model.character.Ability
import com.yablonskyi.model.rulebook.Race
import com.yablonskyi.ui.R
import com.yablonskyi.ui.theme.Dimens
import com.yablonskyi.ui.utils.PreviewThemeWrapper
import com.yablonskyi.ui.utils.formatModifier
import com.yablonskyi.wizard.utils.PreviewUtils
import com.yablonskyi.wizard.viewmodel.AbilityMethod
import com.yablonskyi.wizard.viewmodel.WizardAbilityRules.CORE_ABILITIES
import com.yablonskyi.wizard.viewmodel.WizardAbilityRules.POINT_BUY_BUDGET
import com.yablonskyi.wizard.viewmodel.WizardAbilityRules.POINT_BUY_COSTS
import kotlinx.coroutines.launch
import kotlin.math.floor

@Composable
fun WizardAbilitiesStep(
    method: AbilityMethod,
    selectedRace: Race,
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
    val raceAbilityBonuses = selectedRace.abilityBonuses

    val pagerState = rememberPagerState(
        initialPage = method.ordinal,
        pageCount = { AbilityMethod.entries.size }
    )

    val scope = rememberCoroutineScope()
    val currentMethod by rememberUpdatedState(method)
    val currentOnMethodChange by rememberUpdatedState(onMethodChange)

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .collect { page ->
                val selectedMethod = AbilityMethod.entries[page]
                if (selectedMethod != currentMethod) {
                    currentOnMethodChange(selectedMethod)
                }
            }
    }

    Column(modifier = modifier.fillMaxSize()) {
        PrimaryTabRow(
            selectedTabIndex = method.ordinal,
            modifier = Modifier.fillMaxWidth(),
            divider = {},
            indicator = {
                Box(
                    modifier = Modifier
                        .tabIndicatorOffset(method.ordinal)
                        .fillMaxHeight()
                        .padding(vertical = 4.dp, horizontal = 4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .zIndex(-1f)
                )
            },
        ) {
            CompositionLocalProvider(LocalRippleConfiguration provides null) {
                AbilityMethod.entries.forEachIndexed { index, method ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        selectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        text = {
                            Text(
                                when (method) {
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
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            when (page) {
                0 -> StandardArrayPage(
                    raceAbilityBonuses = raceAbilityBonuses,
                    standardArrayPool = standardArrayPool,
                    standardAssignments = standardAssignments,
                    usedPoolValues = usedPoolValues,
                    pendingPoolValue = pendingPoolValue,
                    onSelectPoolValue = onSelectPoolValue,
                    onAssignToAbility = onAssignToAbility,
                    onUnassignAbility = onUnassignAbility,
                )

                1 -> PointBuyPage(
                    raceAbilityBonuses = raceAbilityBonuses,
                    pointBuyScores = pointBuyScores,
                    pointsSpent = pointsSpent,
                    onIncrementPB = onIncrementPB,
                    onDecrementPB = onDecrementPB
                )

                2 -> RollDicePage(
                    raceAbilityBonuses = raceAbilityBonuses,
                    rolledResults = rolledResults,
                    rollIndexAssignments = rollIndexAssignments,
                    pendingRollIndex = pendingRollIndex,
                    onAssignToAbility = onAssignToAbility,
                    onUnassignAbility = onUnassignAbility,
                    onRollAll = onRollAll,
                    onSelectRollIndex = onSelectRollIndex,
                )
            }
        }
    }
}

@Composable
fun PointBuyPage(
    pointBuyScores: Map<Ability, Int>,
    raceAbilityBonuses: Map<Ability, Int>,
    pointsSpent: Int,
    onIncrementPB: (Ability) -> Unit,
    onDecrementPB: (Ability) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.padding(8.dp)
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
            val racial = raceAbilityBonuses[ability] ?: 0
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

@Composable
fun StandardArrayPage(
    raceAbilityBonuses: Map<Ability, Int>,
    standardArrayPool: List<Int>,
    standardAssignments: Map<Ability, Int>,
    usedPoolValues: Set<Int>,
    pendingPoolValue: Int?,
    onSelectPoolValue: (Int) -> Unit,
    onAssignToAbility: (Ability) -> Unit,
    onUnassignAbility: (Ability) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.padding(8.dp)
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
            val racial = raceAbilityBonuses[ability] ?: 0

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

@Composable
fun RollDicePage(
    raceAbilityBonuses: Map<Ability, Int>,
    rolledResults: List<Int>,
    rollIndexAssignments: Map<Ability, Int>,
    pendingRollIndex: Int?,
    onAssignToAbility: (Ability) -> Unit,
    onUnassignAbility: (Ability) -> Unit,
    onRollAll: () -> Unit,
    onSelectRollIndex: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.padding(8.dp)
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
                val racial = raceAbilityBonuses[ability] ?: 0

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
            else -> Color.Transparent
        },
        label = "abilityRowBg"
    )

    OutlinedCard(
        onClick = onClick,
        shape = shape,
        colors = CardDefaults.outlinedCardColors().copy(
            containerColor = containerColor
        ),
        modifier = modifier.heightIn(min = 48.dp)
    ) {
        Grid(
            config = {
                val fieldWidth = ((constraints.maxWidth.toDp() - 24.dp) / 5).coerceAtLeast(0.dp)
                repeat(5) { column(fieldWidth) }
                column(24.dp)
                row(GridTrackSize.Auto)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.Spacing.Medium)
                .heightIn(min = 32.dp)
        ) {
            Text(
                text = stringResource(ability.nameRes).take(3).uppercase(),
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.gridItem(row = 1, column = 1, alignment = Alignment.CenterStart)
            )
            if (racialBonus != 0) {
                Text(
                    text = "+$racialBonus",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.gridItem(row = 1, column = 2, alignment = Alignment.Center),
                    textAlign = TextAlign.Center
                )
            }
            val finalScore = baseScore?.plus(racialBonus)
            FinalScoreChip(
                score = finalScore?.toString() ?: "\u2014",
                modifier = Modifier.gridItem(row = 1, column = 3, alignment = Alignment.Center)
            )
            Text(
                text = finalScore?.let { formatModifier(floor((it - 10) / 2.0).toInt()) }
                    ?: "\u2014",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.gridItem(row = 1, column = 4, alignment = Alignment.Center),
                textAlign = TextAlign.Center
            )
            Text(
                text = baseScore?.toString() ?: "\u2014",
                modifier = Modifier.gridItem(row = 1, column = 5, alignment = Alignment.Center),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (isAssigned) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = stringResource(R.string.unassign),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .gridItem(row = 1, column = 6, alignment = Alignment.Center)
                        .size(24.dp)
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
    OutlinedCard(
        shape = shape,
        modifier = modifier.heightIn(min = 48.dp)
    ) {
        Grid(
            config = {
                val fieldWidth = ((constraints.maxWidth.toDp() - 96.dp) / 4).coerceAtLeast(0.dp)
                repeat(4) { column(fieldWidth) }
                repeat(3) { column(32.dp) }
                row(GridTrackSize.Auto)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .heightIn(min = 36.dp)
        ) {
            Text(
                text = stringResource(ability.nameRes).take(3).uppercase(),
                modifier = Modifier.gridItem(
                    row = 1,
                    column = 1,
                    alignment = Alignment.CenterStart
                ),
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium
            )
            if (racialBonus != 0) {
                Text(
                    text = "+$racialBonus",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.gridItem(row = 1, column = 2, alignment = Alignment.Center),
                    textAlign = TextAlign.Center
                )
            }

            FinalScoreChip(
                score = "${baseScore + racialBonus}",
                modifier = Modifier.gridItem(row = 1, column = 3, alignment = Alignment.Center)
            )

            val abilityMod = floor((baseScore + racialBonus - 10) / 2.0).toInt()

            Text(
                text = formatModifier(abilityMod),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.gridItem(row = 1, column = 4, alignment = Alignment.Center),
                textAlign = TextAlign.Center
            )
            IconButton(
                onClick = { onDecrementPB(ability) },
                enabled = canDec,
                modifier = Modifier
                    .gridItem(row = 1, column = 5, alignment = Alignment.Center)
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
                modifier = Modifier.gridItem(row = 1, column = 6, alignment = Alignment.Center),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            IconButton(
                onClick = { onIncrementPB(ability) },
                enabled = canInc,
                modifier = Modifier
                    .gridItem(row = 1, column = 7, alignment = Alignment.Center)
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

@Composable
fun FinalScoreChip(
    score: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.size(36.dp)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(6.dp)
        ) {
            Text(
                text = score,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Center),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(name = "Point-buy states", showBackground = true, widthDp = 360)
@Preview(
    name = "Point-buy states - narrow, large text",
    showBackground = true,
    widthDp = 320,
    fontScale = 1.5f
)
@Composable
private fun AbilityPointBuyRowPreview() {
    PreviewThemeWrapper.Preview {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            AbilityPointBuyRow(
                ability = Ability.STR, racialBonus = 2, baseScore = 15,
                shape = RoundedCornerShape(16.dp), canDec = true, canInc = false,
                onIncrementPB = {}, onDecrementPB = {}
            )
            AbilityPointBuyRow(
                ability = Ability.DEX, racialBonus = 0, baseScore = 8,
                shape = RoundedCornerShape(16.dp), canDec = false, canInc = true,
                onIncrementPB = {}, onDecrementPB = {}
            )
            AbilityPointBuyRow(
                ability = Ability.CON, racialBonus = 1, baseScore = 12,
                shape = RoundedCornerShape(16.dp), canDec = true, canInc = true,
                onIncrementPB = {}, onDecrementPB = {}
            )
        }
    }
}

@Preview(name = "Assignment states", showBackground = true, widthDp = 360)
@Preview(
    name = "Assignment states - narrow, large text",
    showBackground = true,
    widthDp = 320,
    fontScale = 1.5f
)
@Composable
private fun AbilityAssignRowPreview() {
    PreviewThemeWrapper.Preview {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            AbilityAssignRow(Ability.STR, 15, 2, false, RoundedCornerShape(16.dp), {})
            AbilityAssignRow(Ability.DEX, 12, 0, false, RoundedCornerShape(16.dp), {})
            AbilityAssignRow(Ability.CON, null, 1, false, RoundedCornerShape(16.dp), {})
            AbilityAssignRow(Ability.INT, null, 0, false, RoundedCornerShape(16.dp), {})
            AbilityAssignRow(Ability.WIS, null, 1, true, RoundedCornerShape(16.dp), {})
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WizardAbilitiesStepPreview_POINT_BUY() {
    PreviewThemeWrapper.Preview {
        WizardAbilitiesStep(
            method = AbilityMethod.POINT_BUY,
            selectedRace = PreviewUtils.origRaces.first(),
            standardAssignments = emptyMap(),
            pendingPoolValue = null,
            pointBuyScores = PreviewUtils.pointBuyScores,
            pointsSpent = PreviewUtils.pointsSpent,
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

@Preview(showBackground = true)
@Composable
private fun WizardAbilitiesStepPreview_STANDARD() {
    PreviewThemeWrapper.Preview {
        WizardAbilitiesStep(
            method = AbilityMethod.STANDARD_ARRAY,
            selectedRace = PreviewUtils.origRaces.first(),
            standardAssignments = mapOf(
                Ability.INT to 15,
                Ability.CHA to 8,
            ),
            pendingPoolValue = 14,
            pointBuyScores = PreviewUtils.pointBuyScores,
            pointsSpent = PreviewUtils.pointsSpent,
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

@Preview(showBackground = true)
@Composable
private fun WizardAbilitiesStepPreview_RANDOM() {
    PreviewThemeWrapper.Preview {
        WizardAbilitiesStep(
            method = AbilityMethod.ROLL,
            selectedRace = PreviewUtils.origRaces.first(),
            standardAssignments = emptyMap(),
            pendingPoolValue = null,
            pointBuyScores = PreviewUtils.pointBuyScores,
            pointsSpent = PreviewUtils.pointsSpent,
            rolledResults = PreviewUtils.rolledResults,
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