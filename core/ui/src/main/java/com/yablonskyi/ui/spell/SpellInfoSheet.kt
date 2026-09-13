package com.yablonskyi.ui.spell

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.yablonskyi.model.character.Component
import com.yablonskyi.model.character.MagicSchool
import com.yablonskyi.model.character.Spell
import com.yablonskyi.model.character.SpellCastTime
import com.yablonskyi.model.character.SpellDuration
import com.yablonskyi.model.character.SpellLevel
import com.yablonskyi.model.character.SpellRangeType
import com.yablonskyi.ui.R
import com.yablonskyi.ui.theme.Dimens
import com.yablonskyi.ui.utils.PreviewThemeWrapper
import com.yablonskyi.ui.utils.spell.SpellTag

@Suppress("SimplifiableCallChain")
@Composable
fun SpellInfoSheet(
    spell: Spell,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Dimens.Spacing.Small),
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
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close))
            }
        }
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(Dimens.Spacing.Small)
        ) {
            item {
                Text(
                    text = "${stringResource(spell.level.resId)}, ${
                        stringResource(spell.school.resId)
                    }",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                if (spell.isConcentration || spell.isRitual) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing.Small),
                        modifier = Modifier.padding(bottom = Dimens.Spacing.Small)
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
            }
            item {
                StatRow(
                    label = stringResource(R.string.msg_casting_time),
                    value = stringResource(spell.castTime.resId)
                )
            }
            item {
                val rangeStr = if (spell.rangeValue != null && spell.rangeValue!! > 0) {
                    "${spell.rangeValue} ${stringResource(R.string.feets)}"
                } else {
                    stringResource(spell.rangeType.resId)
                }
                StatRow(label = stringResource(R.string.range_distance), value = rangeStr)
            }
            item {
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
            }
            item {
                HorizontalDivider()
            }
            item {
                if (spell.description.isNotBlank()) {
                    Text(
                        text = spell.description,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Start,
                    )
                    Spacer(Modifier.height(Dimens.Spacing.Small))
                }
            }
            item {
                if (!spell.higherLevels.isNullOrBlank()) {
                    HorizontalDivider()
                    Spacer(Modifier.height(Dimens.Spacing.Small))
                    Text(
                        text = stringResource(R.string.spell_higher_levels),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.height(Dimens.Spacing.Small))
                    Text(
                        text = spell.higherLevels!!,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Start,
                    )
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
    PreviewThemeWrapper.Preview {
        SpellInfoSheet(
            spell = Spell(
                name = "some long spell name [1234556]",
                school = MagicSchool.EVOCATION,
                level = SpellLevel.LEVEL_3,
                castTime = SpellCastTime.ACTION,
                rangeType = SpellRangeType.SELF,
                rangeValue = 30,
                components = listOf(Component.VERBAL, Component.MATERIAL),
                material = "a pinch of ash",
                isConcentration = true,
                duration = SpellDuration.TEN_MINUTES,
                description = "A sample spell used to preview the spell info sheet.",
                higherLevels = "At higher levels, this preview spell improves further.",
            ),
            onDismiss = {}
        )
    }
}