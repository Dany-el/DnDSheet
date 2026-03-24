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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewDynamicColors
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.yablonskyi.dndsheet.R
import com.yablonskyi.dndsheet.data.model.character.Ability
import com.yablonskyi.dndsheet.data.model.character.Character
import com.yablonskyi.dndsheet.data.model.character.ProficiencyLevel
import com.yablonskyi.dndsheet.data.model.character.Skill
import com.yablonskyi.dndsheet.data.model.dice.DiceRoles
import com.yablonskyi.dndsheet.ui.theme.DnDSheetTheme
import com.yablonskyi.dndsheet.ui.utils.UiUtils

@Composable
fun AbilitySlide(
    character: Character,
    onRollClick: (String) -> Unit,
    onAbilityClick: (Ability) -> Unit,
    onProfSavingThrowClick: (Ability, Boolean) -> Unit,
    onProficiencyChange: (Skill, ProficiencyLevel) -> Unit,
    modifier: Modifier = Modifier
) {
    val abilities = Ability.entries.filter { it != Ability.NONE }

    Box(
        modifier = modifier.widthIn(max = 520.dp)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(items = abilities, key = { _, item -> item.ordinal }) { index, ability ->

                val itemShape = when {
                    abilities.size == 1 -> RoundedCornerShape(16.dp)
                    index == 0 -> RoundedCornerShape(
                        topStart = 16.dp, topEnd = 16.dp,
                        bottomStart = 4.dp, bottomEnd = 4.dp
                    )

                    index == abilities.lastIndex -> RoundedCornerShape(
                        topStart = 4.dp, topEnd = 4.dp,
                        bottomStart = 16.dp, bottomEnd = 16.dp
                    )

                    else -> MaterialTheme.shapes.extraSmall
                }

                AbilityCard(
                    ability = ability,
                    shape = itemShape,
                    character = character,
                    onRollClick = onRollClick,
                    onProfSavingThrowClick = onProfSavingThrowClick,
                    onAbilityClick = { onAbilityClick(ability) },
                    onProficiencyChange = onProficiencyChange,
                )
            }
            item {
                Spacer(modifier = Modifier.height(90.dp))
            }
        }
    }
}

@Composable
fun AbilityCard(
    ability: Ability,
    shape: Shape,
    character: Character,
    onRollClick: (String) -> Unit,
    onAbilityClick: () -> Unit,
    onProfSavingThrowClick: (Ability, Boolean) -> Unit,
    onProficiencyChange: (Skill, ProficiencyLevel) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = shape,
        colors = CardDefaults.cardColors().copy(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 16.dp)
        ) {
            AbilityTitle(
                ability = ability,
                value = character.abilityBlock.getScore(ability),
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .clickable(onClick = onAbilityClick)
            )
            ModifierRow(
                abilityMod = character.getAbilityMod(ability),
                savingThrowMod = character.getSavingThrowMod(ability),
                onProficiencyChange = { isProf -> onProfSavingThrowClick(ability, isProf) },
                isProficient = character.savingThrowProficiencies.contains(ability),
                onRollClick = onRollClick,
            )

            val relevantSkills = Skill.entries.filter { it.defaultAbility == ability }
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (relevantSkills.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                }
                relevantSkills.forEachIndexed { index, skill ->
                    val totalMod = character.getSkillMod(skill)
                    val proficiencyLevel =
                        character.skillProficiencies[skill] ?: ProficiencyLevel.NONE

                    val itemShape = when {
                        relevantSkills.size == 1 -> RoundedCornerShape(16.dp)
                        index == 0 -> RoundedCornerShape(
                            topStart = 16.dp, topEnd = 16.dp,
                            bottomStart = 4.dp, bottomEnd = 4.dp
                        )

                        index == relevantSkills.lastIndex -> RoundedCornerShape(
                            topStart = 4.dp, topEnd = 4.dp,
                            bottomStart = 16.dp, bottomEnd = 16.dp
                        )

                        else -> MaterialTheme.shapes.extraSmall
                    }

                    SkillRow(
                        skillName = stringResource(skill.nameRes),
                        proficiencyLevel = proficiencyLevel,
                        modifierValue = totalMod,
                        onProficiencyChange = { newLevel -> onProficiencyChange(skill, newLevel) },
                        onClick = { onRollClick("${DiceRoles.D20.roll}${formatModifier(totalMod)}") },
                        shape = itemShape
                    )
                }
            }
        }
    }
}

@Composable
fun AbilityTitle(
    ability: Ability,
    value: Int,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(ability.nameRes).uppercase(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .weight(1f)
        )
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ModifierRow(
    abilityMod: Int,
    savingThrowMod: Int,
    isProficient: Boolean,
    onRollClick: (String) -> Unit,
    onProficiencyChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val (icon, tint) = when (isProficient) {
        false -> Pair(
            Icons.Outlined.RadioButtonUnchecked,
            MaterialTheme.colorScheme.outline
        )

        true -> Pair(
            Icons.Filled.RadioButtonChecked,
            MaterialTheme.colorScheme.tertiary
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.height(40.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                modifier = Modifier.matchParentSize()
            ) { }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.check).uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                ModifierButton(
                    onClick = { onRollClick("${DiceRoles.D20.roll}${formatModifier(abilityMod)}") },
                    text = formatModifier(abilityMod)
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier.height(40.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                modifier = Modifier.matchParentSize()
            ) { }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .clickable(
                        onClick = {
                            onProficiencyChange(!isProficient)
                        }
                    )
            ) {
                IconButton(
                    onClick = { onProficiencyChange(!isProficient) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "Saving throw proficiency: $isProficient",
                        tint = tint,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = stringResource(R.string.saving_throw).uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                ModifierButton(
                    onClick = { onRollClick("${DiceRoles.D20.roll}${formatModifier(savingThrowMod)}") },
                    text = formatModifier(savingThrowMod),
                )
            }
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
    onClick: (String) -> Unit
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
            Icons.Filled.CheckCircle,
            MaterialTheme.colorScheme.tertiary
        )
    }

    Box(
        modifier = Modifier
            .height(40.dp)
    ) {
        Surface(
            shape = shape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
            modifier = Modifier.matchParentSize()
        ) {
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    onClick = {
                        val nextLevel = when (proficiencyLevel) {
                            ProficiencyLevel.NONE -> ProficiencyLevel.PROFICIENT
                            ProficiencyLevel.PROFICIENT -> ProficiencyLevel.EXPERT
                            ProficiencyLevel.EXPERT -> ProficiencyLevel.NONE
                            else -> ProficiencyLevel.PROFICIENT
                        }

                        onProficiencyChange(nextLevel)
                    }
                )
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
                modifier = Modifier.weight(1f)
            )
            ModifierButton(
                onClick = { onClick("${DiceRoles.D20.roll}${formatModifier(modifierValue)}") },
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

@Composable
fun ModifierButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    shape: CornerBasedShape = MaterialTheme.shapes.large
) {
    TextButton(
        onClick = onClick,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary),
        shape = shape.copy(
            topStart = CornerSize(4.dp),
            bottomStart = CornerSize(4.dp)
        ),
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.tertiary
        ),
        contentPadding = PaddingValues(horizontal = 12.dp),
        modifier = modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

fun formatModifier(value: Int): String {
    return if (value >= 0) "+$value" else "$value"
}

@Preview(
    uiMode = Configuration.UI_MODE_TYPE_NORMAL, locale = "ru"
)
@Composable
private fun AbilitySlidePreview_Normal() {
    DnDSheetTheme {
        AbilitySlide(
            character = UiUtils.sampleCharacters.first(),
            onRollClick = {},
            onAbilityClick = { },
            onProfSavingThrowClick = { _, _ -> },
            onProficiencyChange = { _, _ -> }
        )
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES, locale = "uk"
)
@PreviewScreenSizes
@PreviewDynamicColors
@Composable
private fun AbilitySlidePreview_Night() {
    DnDSheetTheme {
        AbilitySlide(
            character = UiUtils.sampleCharacters.last(),
            onRollClick = {},
            onAbilityClick = { },
            onProfSavingThrowClick = { _, _ -> },
            onProficiencyChange = { _, _ -> }
        )
    }
}