package com.yablonskyi.character.presentation.sheet.slides

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.yablonskyi.character.presentation.sheet.mapper.SkillUiModel
import com.yablonskyi.model.character.ProficiencyLevel
import com.yablonskyi.model.character.Skill
import com.yablonskyi.ui.theme.Dimens
import com.yablonskyi.ui.utils.formatModifier
import com.yablonskyi.ui.utils.listItemShape
import com.yablonskyi.ui.utils.PreviewThemeWrapper
import com.yablonskyi.ui.utils.sortedByTranslatedName

@Composable
fun SkillsSlide(
    skills: List<SkillUiModel>,
    onProficiencyChange: (Skill, ProficiencyLevel) -> Unit,
    onSkillRoll: (Skill, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val sortedSkills = skills.sortedByTranslatedName(LocalResources.current) { it.skill.nameRes }

    LazyColumn(
        contentPadding = PaddingValues(
            start = 8.dp,
            top = 8.dp,
            end = 8.dp,
            bottom = Dimens.Fab.BottomPadding
        ),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        itemsIndexed(
            items = sortedSkills,
            key = { _, model -> model.skill.ordinal }) { index, model ->

            val skill = model.skill
            val proficiency = model.profLevel
            val mod = model.modifier

            val shape = listItemShape(index, skills.size)

            SkillRow(
                skillName = stringResource(skill.nameRes),
                shape = shape,
                proficiencyLevel = proficiency,
                modifierValue = mod,
                onProficiencyChange = { onProficiencyChange(skill, it) },
                onRoll = { onSkillRoll(skill, mod) }
            )
        }
    }
}

@Composable
fun SkillRow(
    skillName: String,
    shape: Shape,
    proficiencyLevel: ProficiencyLevel,
    modifierValue: Int,
    onProficiencyChange: (ProficiencyLevel) -> Unit,
    onRoll: () -> Unit
) {
    val (icon, tint) = when (proficiencyLevel) {
        ProficiencyLevel.NONE -> Pair(
            Icons.Outlined.RadioButtonUnchecked,
            MaterialTheme.colorScheme.outline
        )

        ProficiencyLevel.HALF -> Pair(
            Icons.Outlined.Contrast,
            MaterialTheme.colorScheme.tertiary
        )

        ProficiencyLevel.PROFICIENT -> Pair(
            Icons.Filled.RadioButtonChecked,
            MaterialTheme.colorScheme.tertiary
        )

        ProficiencyLevel.EXPERT -> Pair(
            Icons.Filled.Stars,
            MaterialTheme.colorScheme.tertiary
        )
    }

    Box(
        modifier = Modifier
            .height(40.dp)
    ) {
        Surface(
            shape = shape,
            color = Color.Transparent,
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.matchParentSize()
        ) { }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
        ) {
            ProficiencyToggle(
                level = proficiencyLevel,
                icon = icon,
                tint = tint,
                onLevelChange = onProficiencyChange,
                modifier = Modifier.padding(start = 4.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = skillName.uppercase(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            ModifierButton(
                onClick = onRoll,
                text = formatModifier(modifierValue),
                shape = shape as CornerBasedShape
            )
        }
    }
}

@Composable
fun ProficiencyToggle(
    level: ProficiencyLevel,
    icon: ImageVector,
    tint: Color,
    onLevelChange: (ProficiencyLevel) -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = {
            val nextLevel = when (level) {
                ProficiencyLevel.NONE -> ProficiencyLevel.PROFICIENT
                ProficiencyLevel.PROFICIENT -> ProficiencyLevel.EXPERT
                ProficiencyLevel.EXPERT -> ProficiencyLevel.NONE
                else -> ProficiencyLevel.PROFICIENT
            }
            onLevelChange(nextLevel)
        },
        modifier = modifier.size(40.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Proficiency Level: ${level.name}",
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
    }
}

@PreviewLightDark
@Composable
private fun SkillsSlidePreview() {
    PreviewThemeWrapper.Preview {
        SkillsSlide(
            skills = Skill.entries.mapIndexed { index, skill ->
                SkillUiModel(
                    skill = skill,
                    profLevel = ProficiencyLevel.entries[index % ProficiencyLevel.entries.size],
                    modifier = listOf(-1, 0, 3, 6)[index % 4],
                )
            },
            onProficiencyChange = { _, _ -> },
            onSkillRoll = { _, _ -> },
            modifier = Modifier.padding(8.dp),
        )
    }
}