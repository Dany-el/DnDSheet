package com.yablonskyi.dndsheet.ui.character.slides

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewDynamicColors
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.yablonskyi.dndsheet.R
import com.yablonskyi.dndsheet.data.model.character.Character
import com.yablonskyi.dndsheet.data.model.character.Spell
import com.yablonskyi.dndsheet.data.model.character.SpellLevel
import com.yablonskyi.dndsheet.data.model.character.SpellRangeType
import com.yablonskyi.dndsheet.data.model.character.SpellSlot
import com.yablonskyi.dndsheet.data.model.dice.DiceRoles
import com.yablonskyi.dndsheet.ui.spell.SpellFilter
import com.yablonskyi.dndsheet.ui.theme.DnDSheetTheme
import com.yablonskyi.dndsheet.ui.utils.UiUtils

@Composable
fun SpellSlide(
    character: Character,
    spells: List<Spell>,
    availableFilters: List<SpellFilter>,
    currentFilter: SpellFilter,
    onFilterChange: (SpellFilter) -> Unit,
    onRollClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    onManageSpellsClick: (Long) -> Unit,
    onSlotClick: (SpellLevel, Int) -> Unit,
    onSpellClick: (Spell) -> Unit
) {
    val groupedSpells = remember(spells) {
        spells.groupBy { it.level }.toSortedMap()
    }

    Box(
        modifier = modifier
    ) {
        LazyColumn(
            contentPadding = PaddingValues(
                start = 4.dp,
                top = 0.dp,
                end = 4.dp,
                bottom = 120.dp
            ),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = modifier.fillMaxWidth()
                ) {
                    SpellFiltersRow(
                        filters = availableFilters,
                        selectedFilter = currentFilter,
                        onFilterChange = onFilterChange,
                    )
                    SpellCastingRow(
                        savingThrow = character.getSpellSaveDC(),
                        attackBonus = character.getSpellAttackBonus(),
                        onRollClick = {
                            onRollClick(
                                "${DiceRoles.D20.roll}${formatModifier(character.getSpellAttackBonus())}"
                            )
                        },
                        onNavigate = { onManageSpellsClick(character.id) }
                    )
                }
            }

            if (spells.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.msg_no_spells),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                groupedSpells.forEach { (level, levelSpells) ->
                    stickyHeader {
                        SpellLevelHeader(
                            level = level,
                            slot = character.spellSettings.spellSlots[level] ?: SpellSlot(),
                            onSlotClick = { delta ->
                                onSlotClick(level, delta)
                            }
                        )
                    }

                    itemsIndexed(
                        items = levelSpells,
                        key = { _, item -> item.spellId }
                    ) { index, spell ->

                        val itemShape = when {
                            levelSpells.size == 1 -> RoundedCornerShape(16.dp)
                            index == 0 -> RoundedCornerShape(
                                topStart = 16.dp, topEnd = 16.dp,
                                bottomStart = 4.dp, bottomEnd = 4.dp
                            )

                            index == levelSpells.lastIndex -> RoundedCornerShape(
                                topStart = 4.dp, topEnd = 4.dp,
                                bottomStart = 16.dp, bottomEnd = 16.dp
                            )

                            else -> MaterialTheme.shapes.extraSmall
                        }

                        SpellCard(
                            spell = spell,
                            shape = itemShape,
                            spellSaveDC = character.getSpellSaveDC(),
                            onRollClick = { dice ->
                                onRollClick(dice)
                            },
                            onUseSpellClick = { onSlotClick(level, 1) },
                            onSpellClick = { spell ->
                                onSpellClick(spell)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SpellCastingRow(
    savingThrow: Int,
    attackBonus: Int,
    onRollClick: () -> Unit,
    onNavigate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        SpellStatPill(
            label = stringResource(R.string.msg_attack),
            modifier = Modifier.weight(1f),
            trailingContent = {
                TextButton(
                    onClick = onRollClick,
                    shape = MaterialTheme.shapes.large.copy(
                        topStart = CornerSize(4.dp),
                        bottomStart = CornerSize(4.dp)
                    ),
                    colors = ButtonDefaults.buttonColors().copy(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    )
                ) {
                    Text(
                        text = formatModifier(attackBonus),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        )

        SpellStatPill(
            label = stringResource(R.string.saving_throw_short),
            modifier = Modifier.weight(1f),
            trailingContent = {
                TextButton(
                    onClick = { },
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline
                    ),
                    enabled = false,
                    shape = MaterialTheme.shapes.large.copy(
                        topStart = CornerSize(0.dp),
                        bottomStart = CornerSize(0.dp)
                    ),
                    colors = ButtonDefaults.buttonColors().copy(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        disabledContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        disabledContainerColor = Color.Transparent
                    )
                ) {
                    Text(
                        text = savingThrow.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        )

        OutlinedIconButton(
            onClick = onNavigate,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = stringResource(R.string.manage_spells),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun SpellStatPill(
    label: String,
    modifier: Modifier = Modifier,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        color = Color.Transparent,
        modifier = modifier.height(40.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(start = 12.dp)
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            if (trailingContent != null) {
                trailingContent()
            }
        }
    }
}

@Composable
fun SpellLevelHeader(
    level: SpellLevel,
    slot: SpellSlot,
    onSlotClick: (Int) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            SpellSlotTracker(
                level = level,
                slot = slot,
                onSlotClick = onSlotClick
            )
        }
    }
}

@Composable
fun SpellSlotTracker(
    level: SpellLevel,
    slot: SpellSlot,
    onSlotClick: (Int) -> Unit // Pass +1 for consume, -1 for restore
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Text(
            text = stringResource(level.resId),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
        )
        LazyRow {
            items(count = slot.max) { index ->
                val isSpent = index < slot.current

                IconButton(
                    onClick = {
                        onSlotClick(if (isSpent) -1 else 1)
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isSpent) Icons.Default.Circle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (isSpent) CheckboxDefaults.colors().checkedBoxColor else MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SpellFiltersRow(
    filters: List<SpellFilter>,
    selectedFilter: SpellFilter,
    onFilterChange: (SpellFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        items(filters) { filter ->
            val isSelected = filter == selectedFilter

            FilterChip(
                selected = isSelected,
                onClick = { onFilterChange(filter) },
                label = {
                    Text(
                        text = stringResource(filter.getLabelResId())
                    )
                },
                leadingIcon = if (isSelected) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else null
            )
        }
    }
}

@Composable
fun SpellCard(
    spell: Spell,
    shape: Shape,
    spellSaveDC: Int,
    onRollClick: (String) -> Unit,
    onUseSpellClick: () -> Unit,
    onSpellClick: (Spell) -> Unit,
    modifier: Modifier = Modifier
) {
    val lowerTextStyle = MaterialTheme.typography.labelLarge.copy(
        fontWeight = FontWeight.SemiBold
    )

    Card(
        shape = shape,
        colors = CardDefaults.cardColors().copy(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                onClick = { onSpellClick(spell) }
            ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = spell.name,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(spell.castTime.clippedResId),
                        style = lowerTextStyle,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(0.2f)
                    )
                    Text(
                        text = spell.rangeType.let {
                            if (it == SpellRangeType.DISTANCE) "${spell.rangeValue ?: 0} ${
                                stringResource(
                                    R.string.feets
                                )
                            }"
                            else stringResource(spell.rangeType.resId)
                        },
                        style = lowerTextStyle,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(0.8f)
                    )
                    Text(
                        text = spell.saveStat?.let {
                            "${stringResource(it.nameRes).take(3)} $spellSaveDC"
                        } ?: "—",
                        style = lowerTextStyle,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(0.6f)
                    )
                    Text(
                        text = spell.damageDice?.let {
                            if (it.isBlank() || it.isEmpty()) "—"
                            else it
                        } ?: "—",
                        style = lowerTextStyle,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(0.6f)
                    )
                }
                if (spell.isConcentration || spell.isRitual) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (spell.isConcentration) {
                            SpellTag(
                                stringResource(R.string.concentration)
                            )
                        }
                        if (spell.isRitual) {
                            SpellTag(
                                stringResource(R.string.ritual)
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            SpellButton(
                onClick = {
                    spell.damageDice?.let {
                        onRollClick(it)
                    }
                    onUseSpellClick()
                }
            )
        }
    }
}

@Composable
fun SpellButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedIconButton(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
//        border = BorderStroke(width = 2.dp, color = MaterialTheme.colorScheme.onPrimary),
        /*colors = IconButtonDefaults.iconButtonColors().copy(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),*/
        modifier = modifier.size(56.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_spell_book),
            contentDescription = null,
            modifier = Modifier.size(42.dp)
        )
    }
}

@Composable
fun SpellTag(
    text: String,
) {
    Surface(
        color = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(8.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
            )
        }
    }
}

@PreviewLightDark
@PreviewDynamicColors
@Composable
private fun SpellSlidePreview() {
    DnDSheetTheme {
        Surface {
            SpellSlide(
                character = UiUtils.sampleCharacters.first(),
                spells = UiUtils.sampleSpells,
                availableFilters = UiUtils.availableFilters,
                currentFilter = UiUtils.currentFilter,
                onFilterChange = {},
                onRollClick = {},
                onSlotClick = { _, _ -> },
                onManageSpellsClick = {},
                onSpellClick = {}
            )
        }
    }
}

@Preview(locale = "ru", group = "language")
@Composable
private fun SpellSlidePreview_RU() {
    DnDSheetTheme {
        Surface {
            SpellSlide(
                character = UiUtils.sampleCharacters.first(),
                spells = UiUtils.sampleSpells,
                availableFilters = UiUtils.availableFilters,
                currentFilter = UiUtils.currentFilter,
                onFilterChange = {},
                onRollClick = {},
                onSlotClick = { _, _ -> },
                onManageSpellsClick = {},
                onSpellClick = {}
            )
        }
    }
}

@Preview(locale = "uk", group = "language")
@Composable
private fun SpellSlidePreview_UK() {
    DnDSheetTheme {
        Surface {
            SpellSlide(
                character = UiUtils.sampleCharacters.first(),
                spells = UiUtils.sampleSpells,
                availableFilters = UiUtils.availableFilters,
                currentFilter = UiUtils.currentFilter,
                onFilterChange = {},
                onRollClick = {},
                onSlotClick = { _, _ -> },
                onManageSpellsClick = {},
                onSpellClick = {}
            )
        }
    }
}