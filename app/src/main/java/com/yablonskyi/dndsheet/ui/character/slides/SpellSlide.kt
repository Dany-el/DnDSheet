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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.yablonskyi.dndsheet.R
import com.yablonskyi.dndsheet.data.model.character.Character
import com.yablonskyi.dndsheet.data.model.character.Spell
import com.yablonskyi.dndsheet.data.model.character.SpellLevel
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
) {

    var selectedSpell by remember { mutableStateOf<Spell?>(null) }

    val groupedSpells = remember(spells) {
        spells.groupBy { it.level }.toSortedMap()
    }

    Column(modifier = modifier.fillMaxSize()) {

        SpellFiltersRow(
            filters = availableFilters,
            selectedFilter = currentFilter,
            onFilterChange = onFilterChange,
            modifier = Modifier.padding(vertical = 8.dp)
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

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
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
                        SpellLevelHeader(level = level)
                    }

                    items(
                        items = levelSpells,
                        key = { it.spellId }
                    ) { spell ->
                        SpellCard(
                            spell = spell,
                            spellSaveDC = character.getSpellSaveDC(),
                            onRollClick = onRollClick,
                            onSpellClick = { selectedSpell = spell }
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(90.dp))
                }
            }
        }
    }

    if (selectedSpell != null) {
        SpellDialog(
            spell = selectedSpell!!,
            onDismissRequest = {
                selectedSpell = null
            }
        )
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
            .padding(16.dp)
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
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
                )
                TextButton(
                    onClick = { },
                    border = BorderStroke(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.outline
                    ),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors().copy(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
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
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
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
                )
                TextButton(
                    onClick = onRollClick,
                    border = BorderStroke(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.outline
                    ),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors().copy(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
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
            modifier = Modifier.padding(horizontal = 16.dp).defaultMinSize(200.dp)
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
    level: SpellLevel
) {
    Surface(
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(level.resId),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
            HorizontalDivider(thickness = 2.dp)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Spacer(Modifier.width(90.dp))
                Icon(
                    painter = painterResource(R.drawable.ic_hourglass),
                    contentDescription = null,
                    modifier = Modifier.weight(0.6f),
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
                    modifier = Modifier.weight(1f),
                    tint = MaterialTheme.colorScheme.onBackground
                )
                Icon(
                    painter = painterResource(R.drawable.dice_d20),
                    contentDescription = null,
                    modifier = Modifier.weight(0.6f),
                    tint = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.width(85.dp))
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
        contentPadding = PaddingValues(horizontal = 16.dp),
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
fun SpellCard(
    spell: Spell,
    spellSaveDC: Int,
    onRollClick: (String) -> Unit,
    onSpellClick: (Spell) -> Unit,
    modifier: Modifier = Modifier
) {
    val lowerTextStyle = MaterialTheme.typography.labelLarge.copy(
        fontWeight = FontWeight.SemiBold
    )

    Card(
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
                        text = stringResource(spell.level.resId),
                        style = lowerTextStyle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1.2f)
                    )
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
                        text = spell.range,
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
                        text = spell.damageDice ?: "—",
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
                    // TODO After casting use spell cell
                }
            )
        }
    }
}

@Suppress("SimplifiableCallChain")
@Composable
fun SpellDialog(
    spell: Spell,
    onDismissRequest: () -> Unit,
) {
    val textStyle = MaterialTheme.typography.bodyLarge

    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = spell.name,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )
            Text(
                text = stringResource(spell.level.resId),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .fillMaxWidth()
            )
            if (spell.isConcentration || spell.isRitual) {
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
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
            Spacer(Modifier.height(8.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                append("${stringResource(R.string.msg_casting_time)}: ")
                            }
                            append(stringResource(spell.castTime.resId))
                        },
                    )
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                append("${stringResource(R.string.msg_distance)}: ")
                            }
                            append(spell.range)
                        }
                    )
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                append("${stringResource(R.string.msg_duration)}: ")
                            }
                            append(stringResource(spell.duration.resId))
                        }
                    )
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                append("${stringResource(R.string.msg_components)}: ")
                            }
                            append(spell.components.map {
                                stringResource(
                                    it.resId
                                )
                            }.joinToString())
                        },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                append("${stringResource(R.string.msg_school)}: ")
                            }
                            append(stringResource(spell.school.resId))
                        }
                    )
                }
            }
            Text(
                text = spell.description,
                style = textStyle,
                textAlign = TextAlign.Justify,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp)
            )
            spell.higherLevels?.let {
                Text(
                    text = it,
                    textAlign = TextAlign.Justify,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                TextButton(
                    onClick = onDismissRequest,
                ) {
                    Text(stringResource(R.string.close))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
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
        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.onTertiaryContainer),
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
            onManageSpellsClick = {}
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
            onManageSpellsClick = {}
        )
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    showSystemUi = false, showBackground = true,
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
            onManageSpellsClick = {}
        )
    }
}

@Preview
@Composable
private fun SpellCardPreview_Concentration() {
    DnDSheetTheme {
        SpellCard(
            spell = UiUtils.sampleSpells.first { it.isConcentration },
            spellSaveDC = UiUtils.sampleCharacters[1].getSpellSaveDC(),
            onRollClick = {},
            onSpellClick = {}
        )
    }
}

@Preview
@Composable
private fun SpellCardPreview_DamageDice() {
    DnDSheetTheme {
        SpellCard(
            spell = UiUtils.sampleSpells.first { it.damageDice != null },
            spellSaveDC = UiUtils.sampleCharacters[1].getSpellSaveDC(),
            onRollClick = {},
            onSpellClick = {}
        )
    }
}

@Preview
@Composable
private fun SpellCardPreview_SaveStat() {
    DnDSheetTheme {
        SpellCard(
            spell = UiUtils.sampleSpells.first { it.saveStat != null },
            spellSaveDC = UiUtils.sampleCharacters[1].getSpellSaveDC(),
            onRollClick = {},
            onSpellClick = {}
        )
    }
}

@Preview
@Composable
private fun SpellTagPreview() {
    DnDSheetTheme {
        SpellTag(
            "C"
        )
    }
}

@Preview
@Composable
private fun SpellDialogPreview() {
    DnDSheetTheme {
        SpellDialog(
            spell = UiUtils.sampleSpells.first { it.isConcentration },
            onDismissRequest = {}
        )
    }
}