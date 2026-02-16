package com.yablonskyi.dndsheet.ui.character.slides

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import com.yablonskyi.dndsheet.R
import com.yablonskyi.dndsheet.data.model.character.Character
import com.yablonskyi.dndsheet.data.model.character.MagicSchool
import com.yablonskyi.dndsheet.data.model.character.Spell
import com.yablonskyi.dndsheet.data.model.character.SpellCastTime
import com.yablonskyi.dndsheet.data.model.character.SpellDuration
import com.yablonskyi.dndsheet.data.model.character.SpellLevel
import com.yablonskyi.dndsheet.data.model.character.SpellRangeType
import com.yablonskyi.dndsheet.data.model.character.SpellSlot
import com.yablonskyi.dndsheet.data.model.dice.DiceRoles
import com.yablonskyi.dndsheet.ui.spell.SpellFilter
import com.yablonskyi.dndsheet.ui.theme.DnDSheetTheme
import com.yablonskyi.dndsheet.ui.utils.MultiSelectDropdownChip
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

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.fillMaxSize()
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
                )
                ManageSpellRow(
                    onManageSpellsClick = { onManageSpellsClick(character.id) }
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
            item {
                Spacer(modifier = Modifier.height(120.dp))
            }
        }
    }
}

@Composable
fun SpellCastingRow(
    savingThrow: Int,
    attackBonus: Int,
    onRollClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
            color = Color.Transparent,
            modifier = Modifier
                .height(40.dp)
                .defaultMinSize(minWidth = 170.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.saving_throw).uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(end = 8.dp)
                )
                TextButton(
                    onClick = { },
                    border = BorderStroke(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    enabled = false,
                    shape = MaterialTheme.shapes.medium,
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
                    )
                }
            }
        }
        Surface(
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
            color = Color.Transparent,
            modifier = Modifier
                .height(40.dp)
                .defaultMinSize(minWidth = 160.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.msg_attack).uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(end = 8.dp)
                )
                TextButton(
                    onClick = onRollClick,
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors().copy(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                ) {
                    Text(
                        text = formatModifier(attackBonus),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
fun ManageSpellRow(
    onManageSpellsClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        TextButton(
            onClick = onManageSpellsClick,
            border = BorderStroke(width = 2.dp, color = MaterialTheme.colorScheme.primary),
            shape = MaterialTheme.shapes.extraSmall,
            colors = ButtonDefaults.buttonColors().copy(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .defaultMinSize(200.dp)
        ) {
            Text(
                text = stringResource(R.string.manage_spells).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
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
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            SpellSlotTracker(
                level = level,
                slot = slot,
                onSlotClick = onSlotClick
            )
            /*HorizontalDivider(thickness = 2.dp)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_hourglass),
                    contentDescription = null,
                    modifier = Modifier.weight(0.2f),
                    tint = MaterialTheme.colorScheme.onBackground
                )
                Icon(
                    painter = painterResource(R.drawable.ic_ruler),
                    contentDescription = null,
                    modifier = Modifier.weight(0.8f),
                    tint = MaterialTheme.colorScheme.onBackground
                )
                Icon(
                    painter = painterResource(R.drawable.ic_swords),
                    contentDescription = null,
                    modifier = Modifier.weight(0.6f),
                    tint = MaterialTheme.colorScheme.onBackground
                )
                Icon(
                    painter = painterResource(R.drawable.dice_d20),
                    contentDescription = null,
                    modifier = Modifier.weight(0.6f),
                    tint = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.width(64.dp))
            }*/
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
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
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
                        tint = if (isSpent) MaterialTheme.colorScheme.primary else Color.Gray,
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
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

@Composable
fun SpellFiltersRow(
    // Filters
    selectedSchool: Set<MagicSchool>,
    selectedLevels: Set<SpellLevel>,
    selectedDurations: Set<SpellDuration>,
    selectedCastTimes: Set<SpellCastTime>,
    selectedConcentration: Boolean,
    selectedRitual: Boolean,
    // Toggles
    toggleSchoolFilter: (MagicSchool) -> Unit,
    toggleLevelFilter: (SpellLevel) -> Unit,
    toggleDurationFilter: (SpellDuration) -> Unit,
    toggleCastTimeFilter: (SpellCastTime) -> Unit,
    toggleRitual: () -> Unit,
    toggleConcentration: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                MultiSelectDropdownChip(
                    title = stringResource(R.string.spell_level),
                    options = SpellLevel.entries,
                    selectedOptions = selectedLevels,
                    onToggle = { toggleLevelFilter(it) },
                    labelMapper = { stringResource(it.resId) }
                )
            }
            item {
                MultiSelectDropdownChip(
                    title = stringResource(R.string.msg_school),
                    options = MagicSchool.entries,
                    selectedOptions = selectedSchool,
                    onToggle = { toggleSchoolFilter(it) },
                    labelMapper = { stringResource(it.resId) }
                )
            }
            item {
                MultiSelectDropdownChip(
                    title = stringResource(R.string.msg_duration),
                    options = SpellDuration.entries,
                    selectedOptions = selectedDurations,
                    onToggle = { toggleDurationFilter(it) },
                    labelMapper = { stringResource(it.resId) }
                )
            }
        }
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                MultiSelectDropdownChip(
                    title = stringResource(R.string.msg_casting_time),
                    options = SpellCastTime.entries,
                    selectedOptions = selectedCastTimes,
                    onToggle = { toggleCastTimeFilter(it) },
                    labelMapper = { stringResource(it.resId) }
                )
            }
            item {
                FilterChip(
                    selected = selectedConcentration,
                    onClick = toggleConcentration,
                    label = { Text(stringResource(R.string.concentration)) },
                    leadingIcon = {
                        if (selectedConcentration) Icon(Icons.Default.Check, null)
                    }
                )
            }
            item {
                FilterChip(
                    selected = selectedRitual,
                    onClick = toggleRitual,
                    label = { Text(stringResource(R.string.ritual)) },
                    leadingIcon = {
                        if (selectedRitual) Icon(Icons.Default.Check, null)
                    }
                )
            }
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
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
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
        border = BorderStroke(width = 2.dp, color = MaterialTheme.colorScheme.onPrimary),
        colors = IconButtonDefaults.iconButtonColors().copy(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
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
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
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

@Preview
@Composable
private fun SpellSlidePreview_EN() {
    DnDSheetTheme {
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

@Preview(locale = "uk")
@Composable
private fun SpellSlidePreview_UA() {
    DnDSheetTheme {
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

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    showSystemUi = false, showBackground = false,
    wallpaper = Wallpapers.NONE, backgroundColor = 0xFF212121
)
@Composable
private fun SpellSlidePreview_EN_Night() {
    DnDSheetTheme {
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