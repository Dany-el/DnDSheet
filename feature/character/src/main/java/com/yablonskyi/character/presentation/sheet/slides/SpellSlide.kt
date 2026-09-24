package com.yablonskyi.character.presentation.sheet.slides

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Grid
import androidx.compose.foundation.layout.GridTrackSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.yablonskyi.character.presentation.common.UiUtils
import com.yablonskyi.character.presentation.sheet.model.SpellFilter
import com.yablonskyi.model.character.Spell
import com.yablonskyi.model.character.SpellLevel
import com.yablonskyi.model.character.SpellRangeType
import com.yablonskyi.model.character.SpellSettings
import com.yablonskyi.model.character.SpellSlot
import com.yablonskyi.ui.R
import com.yablonskyi.ui.preview.FontScalePreviews
import com.yablonskyi.ui.theme.Dimens
import com.yablonskyi.ui.theme.DnDSheetTheme
import com.yablonskyi.ui.utils.formatModifier
import com.yablonskyi.ui.utils.spell.SpellTag

@Composable
fun SpellSlide(
    spells: List<Spell>,
    spellSettings: SpellSettings,
    availableFilters: List<SpellFilter>,
    spellSaveDC: Int,
    spellAttackBonus: Int,
    currentFilter: SpellFilter,
    onFilterChange: (SpellFilter) -> Unit,
    onCastSpell: (Spell) -> Unit,
    onSpellAttackRoll: (Int) -> Unit,
    onManageSpellsClick: () -> Unit,
    onSlotClick: (SpellLevel, Int) -> Unit,
    onSpellClick: (Spell) -> Unit,
    modifier: Modifier = Modifier,
) {
    val groupedSpells = remember(spells) {
        spells.groupBy { it.level }.toSortedMap()
    }

    Box(modifier = modifier) {
        LazyColumn(
            contentPadding = PaddingValues(
                start = 4.dp,
                top = 4.dp,
                end = 4.dp,
                bottom = Dimens.Fab.BottomPadding
            ),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (spells.isNotEmpty()) {
                        SpellFiltersRow(
                            filters = availableFilters,
                            selectedFilter = currentFilter,
                            onFilterChange = onFilterChange,
                        )
                    }
                    SpellCastingRow(
                        savingThrow = spellSaveDC,
                        attackBonus = spellAttackBonus,
                        onSpellAttackRoll = { onSpellAttackRoll(spellAttackBonus) },
                        onNavigate = onManageSpellsClick
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
                    stickyHeader(key = "spell-level-${level.name}") {
                        SpellLevelHeader(
                            level = level,
                            slot = spellSettings.spellSlots[level] ?: SpellSlot(),
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
                            spellSaveDC = spellSaveDC,
                            onCastSpell = { onCastSpell(spell) },
                            onSpellClick = onSpellClick,
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
    onSpellAttackRoll: () -> Unit,
    onNavigate: () -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        itemVerticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .heightIn(min = 48.dp)
                .weight(1f)
        ) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = Color.Transparent,
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.matchParentSize()
            ) { }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clip(MaterialTheme.shapes.large)
            ) {
                Text(
                    text = stringResource(R.string.msg_attack).uppercase(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                )
                ModifierButton(
                    onClick = onSpellAttackRoll,
                    text = formatModifier(attackBonus),
                    shape = MaterialTheme.shapes.large.copy(
                        topStart = CornerSize(0.dp),
                        bottomStart = CornerSize(0.dp)
                    ),
                    modifier = Modifier.heightIn(min = 48.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .heightIn(min = 48.dp)
                .weight(1f)
        ) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = Color.Transparent,
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.matchParentSize()
            ) { }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clip(MaterialTheme.shapes.large)
            ) {
                Text(
                    text = stringResource(R.string.saving_throw_short).uppercase(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                )
                Surface(
                    border = BorderStroke(
                        width = 1.5.dp,
                        color = MaterialTheme.colorScheme.outline
                    ),
                    shape = MaterialTheme.shapes.large.copy(
                        topStart = CornerSize(4.dp),
                        bottomStart = CornerSize(4.dp)
                    ),
                    color = Color.Transparent,
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .widthIn(min = ButtonDefaults.MinWidth)
                            .heightIn(min = 48.dp)
                            .padding(horizontal = 12.dp),
                    ) {
                        Text(
                            text = savingThrow.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }

        OutlinedIconButton(
            onClick = onNavigate,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.size(48.dp)
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
            modifier = Modifier.semantics { heading() },
        )
        LazyRow {
            items(count = slot.max) { index ->
                val isSpent = index < slot.current
                val slotState = stringResource(
                    if (isSpent) R.string.spell_slot_spent else R.string.spell_slot_available
                )
                val slotAction = stringResource(
                    if (isSpent) R.string.restore_spell_slot else R.string.consume_spell_slot,
                    stringResource(level.resId),
                    index + 1,
                )

                IconButton(
                    onClick = {
                        onSlotClick(if (isSpent) -1 else 1)
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .semantics { stateDescription = slotState }
                ) {
                    Icon(
                        imageVector = if (isSpent) Icons.Default.Circle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = slotAction,
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
    onCastSpell: () -> Unit,
    onSpellClick: (Spell) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = { onSpellClick(spell) },
        shape = shape,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
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
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                }
                SpellDetailsGrid(spell = spell, spellSaveDC = spellSaveDC)
                if (spell.isConcentration || spell.isRitual) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
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
                onClick = onCastSpell,
                spellName = spell.name,
            )
        }
    }
}

@Composable
private fun SpellDetailsGrid(
    spell: Spell,
    spellSaveDC: Int,
    modifier: Modifier = Modifier,
) {
    val details = listOf(
        stringResource(spell.castTime.clippedResId),
        if (spell.rangeType == SpellRangeType.DISTANCE) {
            "${spell.rangeValue ?: 0} ${stringResource(R.string.feets)}"
        } else {
            stringResource(spell.rangeType.resId)
        },
        spell.saveStat?.let {
            "${stringResource(it.nameRes).take(3)} $spellSaveDC"
        } ?: "—",
        spell.damageDice?.takeIf { it.isNotBlank() } ?: "—",
    )
    Grid(
        config = {
            val fieldWidth = constraints.maxWidth.toDp() / 4
            repeat(4) { column(fieldWidth) }
            row(GridTrackSize.Auto)
        },
        modifier = modifier.fillMaxWidth(),
    ) {
        details.forEachIndexed { index, detail ->
            Text(
                text = detail,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.gridItem(
                    row = 1,
                    column = index + 1,
                    alignment = Alignment.Center,
                ),
            )
        }
    }
}

@Composable
fun SpellButton(
    onClick: () -> Unit,
    spellName: String,
    modifier: Modifier = Modifier
) {
    OutlinedIconButton(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.size(56.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_spell_book),
            contentDescription = stringResource(R.string.cast_named_spell, spellName),
            modifier = Modifier.size(42.dp)
        )
    }
}

@PreviewLightDark
@FontScalePreviews
@Preview(locale = "ru", group = "Language")
@Preview(locale = "uk", group = "Language")
@Composable
private fun SpellSlidePreview() {
    SpellSlidePreviewContent(UiUtils.sampleSpells)
}

@Preview(name = "Empty", showBackground = true)
@Composable
private fun EmptySpellSlidePreview() {
    SpellSlidePreviewContent(emptyList())
}

@Preview(name = "Long name, large text", widthDp = 320, fontScale = 2f)
@Composable
private fun LongNameSpellSlidePreview() {
    SpellSlidePreviewContent(
        listOf(UiUtils.sampleSpells.first().copy(name = "Protection from Evil and Good"))
    )
}

@Composable
private fun SpellSlidePreviewContent(spells: List<Spell>) {
    val character = UiUtils.sampleCharacters.first()
    DnDSheetTheme {
        Surface {
            SpellSlide(
                spellSettings = character.spellSettings,
                spellSaveDC = character.getSpellSaveDC(),
                spellAttackBonus = character.getSpellAttackBonus(),
                spells = spells,
                availableFilters = UiUtils.availableFilters,
                currentFilter = UiUtils.currentFilter,
                onFilterChange = {},
                onCastSpell = {},
                onSpellAttackRoll = {},
                onSlotClick = { _, _ -> },
                onManageSpellsClick = {},
                onSpellClick = {},
            )
        }
    }
}