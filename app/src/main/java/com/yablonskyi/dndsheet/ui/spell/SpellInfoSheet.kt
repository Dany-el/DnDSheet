package com.yablonskyi.dndsheet.ui.spell

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yablonskyi.dndsheet.R
import com.yablonskyi.dndsheet.data.model.character.Component
import com.yablonskyi.dndsheet.data.model.character.Spell
import com.yablonskyi.dndsheet.ui.character.slides.SpellTag
import com.yablonskyi.dndsheet.ui.theme.DnDSheetTheme
import com.yablonskyi.dndsheet.ui.utils.UiUtils

@Suppress("SimplifiableCallChain")
@Composable
fun SpellInfoSheet(
    spell: Spell,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 48.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = spell.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        Text(
            text = "${stringResource(spell.level.resId)}, ${
                stringResource(spell.school.resId)
            }",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (spell.isConcentration || spell.isRitual) {
            Row(
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

        HorizontalDivider()

        StatRow(
            label = stringResource(R.string.msg_casting_time),
            value = stringResource(spell.castTime.resId)
        )

        val rangeStr = if (spell.rangeValue != null && spell.rangeValue > 0) {
            "${spell.rangeValue} ${stringResource(R.string.feets)}"
        } else {
            stringResource(spell.rangeType.resId)
        }
        StatRow(label = stringResource(R.string.range_distance), value = rangeStr)

        val compLetters = spell.components.map { component ->
            stringResource(component.resId).first().uppercase()
        }.joinToString(", ")

        val componentsStr =
            if (spell.components.contains(Component.MATERIAL) && !spell.material.isNullOrBlank()) {
                "$compLetters (${spell.material})"
            } else {
                compLetters
            }
        StatRow(label = stringResource(R.string.msg_components), value = componentsStr)

        HorizontalDivider()


        if (spell.description.isNotBlank()) {
            Text(
                text = stringResource(R.string.spell_description),
                style = MaterialTheme.typography.titleMedium,
            )
            Surface(
                color = OutlinedTextFieldDefaults.colors().unfocusedContainerColor,
                shape = MaterialTheme.shapes.extraSmall,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                val paragraphs = remember(spell.description) {
                    spell.description.split("\n").filter { it.isNotBlank() }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .heightIn(max = 200.dp)
                ) {
                    items(paragraphs) { paragraph ->
                        Text(
                            text = paragraph,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Justify,
                        )
                    }
                }
            }
        }


        if (!spell.higherLevels.isNullOrBlank()) {
            Text(
                text = stringResource(R.string.spell_higher_levels),
                style = MaterialTheme.typography.titleMedium,
            )
            Surface(
                color = OutlinedTextFieldDefaults.colors().unfocusedContainerColor,
                shape = MaterialTheme.shapes.extraSmall,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                val paragraphs = remember(spell.higherLevels) {
                    spell.higherLevels.split("\n").filter { it.isNotBlank() }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .heightIn(max = 150.dp)
                ) {
                    items(paragraphs) { paragraph ->
                        Text(
                            text = paragraph,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Justify,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Text(
        text = buildAnnotatedString {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append("$label: ")
            }
            append(value)
        },
        style = MaterialTheme.typography.bodyLarge
    )
}

@Preview
@Composable
private fun SpellInfoSheetPreview() {
    DnDSheetTheme {
        SpellInfoSheet(
            spell = UiUtils.sampleSpells.first { it.higherLevels != null },
            onDismiss = {}
        )
    }
}