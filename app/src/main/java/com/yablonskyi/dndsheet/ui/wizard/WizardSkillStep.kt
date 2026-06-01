package com.yablonskyi.dndsheet.ui.wizard

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yablonskyi.dndsheet.R
import com.yablonskyi.dndsheet.data.model.character.AbilityBlock
import com.yablonskyi.dndsheet.data.model.character.Skill
import com.yablonskyi.dndsheet.ui.character.slides.AbilityTitle
import com.yablonskyi.dndsheet.ui.theme.DnDSheetTheme

@Composable
fun WizardSkillsStep(
    availableSkills: List<Skill>,
    selectedSkills: Set<Skill>,
    abilityBlock: AbilityBlock,
    maxSkills: Int,
    onSkillToggle: (Skill) -> Unit,
    modifier: Modifier = Modifier
) {
    // Group by ability
    val grouped = remember(availableSkills) {
        availableSkills.groupBy { it.defaultAbility to abilityBlock.getScore(it.defaultAbility) }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.wizard_skills_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
        }

        stickyHeader {
            Surface(
                color = MaterialTheme.colorScheme.surface,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(
                            R.string.wizard_skills_selected, selectedSkills.size, maxSkills
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selectedSkills.size == maxSkills)
                            MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LinearProgressIndicator(
                        progress = { if (maxSkills > 0) selectedSkills.size / maxSkills.toFloat() else 0f },
                        drawStopIndicator = {},
                        modifier = Modifier
                            .width(120.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                }
            }
        }

        grouped.entries.forEachIndexed { index, entry ->

            val (ability, skills) = entry

            item(key = ability.first) {

                val shape = when {
                    grouped.size == 1 -> RoundedCornerShape(16.dp)
                    index == 0 -> RoundedCornerShape(
                        topStart = 16.dp, topEnd = 16.dp,
                        bottomStart = 4.dp, bottomEnd = 4.dp
                    )

                    index == grouped.entries.toList().lastIndex -> RoundedCornerShape(
                        topStart = 4.dp, topEnd = 4.dp,
                        bottomStart = 16.dp, bottomEnd = 16.dp
                    )

                    else -> MaterialTheme.shapes.extraSmall
                }

                Card(
                    shape = shape,
                    colors = CardDefaults.cardColors().copy(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp, vertical = 16.dp)
                    ) {
                        AbilityTitle(
                            ability = ability.first,
                            value = ability.second,
                        )

                        Spacer(Modifier.height(8.dp))

                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            skills.forEachIndexed { index, skill ->
                                val isChecked = skill in selectedSkills
                                val isEnabled = isChecked || selectedSkills.size < maxSkills

                                val shape = when {
                                    skills.size == 1 -> RoundedCornerShape(16.dp)
                                    index == 0 -> RoundedCornerShape(
                                        topStart = 16.dp, topEnd = 16.dp,
                                        bottomStart = 4.dp, bottomEnd = 4.dp
                                    )

                                    index == skills.lastIndex -> RoundedCornerShape(
                                        topStart = 4.dp, topEnd = 4.dp,
                                        bottomStart = 16.dp, bottomEnd = 16.dp
                                    )

                                    else -> MaterialTheme.shapes.extraSmall
                                }

                                SkillRow(
                                    skill = skill,
                                    shape = shape,
                                    isChecked = isChecked,
                                    isEnabled = isEnabled,
                                    onToggle = { onSkillToggle(skill) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SkillRow(
    skill: Skill,
    shape: CornerBasedShape,
    isChecked: Boolean,
    isEnabled: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        targetValue = if (isChecked) MaterialTheme.colorScheme.secondaryContainer
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        label = "skillRowColor"
    )

    Box(
        modifier = modifier
            .heightIn(min = 40.dp)
    ) {
        Surface(
            shape = shape,
            color = containerColor,
            modifier = Modifier.matchParentSize()
        ) {
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .clickable(enabled = isEnabled, onClick = onToggle)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(skill.nameRes).uppercase(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isEnabled) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
            Checkbox(
                checked = isChecked,
                onCheckedChange = null,
                enabled = isEnabled
            )
        }
    }
}

@Preview
@Composable
private fun WizardSkillStepPreview() {
    DnDSheetTheme {
        Surface(

        ) {
            WizardSkillsStep(
                availableSkills = Skill.entries.toList(),
                selectedSkills = Skill.entries.toSet(),
                abilityBlock = AbilityBlock(),
                maxSkills = 3,
                onSkillToggle = { },
            )
        }
    }
}