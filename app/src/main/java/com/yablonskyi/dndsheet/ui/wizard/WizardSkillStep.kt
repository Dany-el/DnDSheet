package com.yablonskyi.dndsheet.ui.wizard

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yablonskyi.dndsheet.R
import com.yablonskyi.dndsheet.data.model.character.Skill

@Composable
fun WizardSkillsStep(
    availableSkills: List<Skill>,
    selectedSkills: Set<Skill>,
    maxSkills: Int,
    onSkillToggle: (Skill) -> Unit,
    modifier: Modifier = Modifier
) {
    // Group by ability
    val grouped = remember(availableSkills) {
        availableSkills.groupBy { it.defaultAbility }
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
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
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

        grouped.forEach { (ability, skills) ->
            item(key = ability.name) {
                Text(
                    text = stringResource(ability.nameRes),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                )
            }
            items(skills, key = { it.name }) { skill ->
                val isChecked = skill in selectedSkills
                val isEnabled = isChecked || selectedSkills.size < maxSkills
                SkillRow(
                    skill = skill,
                    isChecked = isChecked,
                    isEnabled = isEnabled,
                    onToggle = { onSkillToggle(skill) }
                )
            }
        }
    }
}

@Composable
fun SkillRow(
    skill: Skill,
    isChecked: Boolean,
    isEnabled: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        targetValue = if (isChecked) MaterialTheme.colorScheme.secondaryContainer
        else Color.Transparent,
        label = "skillRowColor"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(containerColor)
            .clickable(enabled = isEnabled, onClick = onToggle)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(skill.nameRes),
            style = MaterialTheme.typography.bodyMedium,
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