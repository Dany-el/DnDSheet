package com.yablonskyi.character.presentation.sheet.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RadioButtonChecked
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yablonskyi.character.presentation.sheet.mapper.SavingThrowUiModel
import com.yablonskyi.character.presentation.sheet.slides.ModifierButton
import com.yablonskyi.model.character.Ability
import com.yablonskyi.ui.R
import com.yablonskyi.ui.utils.formatModifier

@Composable
fun SavingThrowGrid(
    savingThrows: List<SavingThrowUiModel>,
    onRollClick: (Ability, Int) -> Unit,
    onProficiencyChange: (Ability, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.padding(horizontal = 8.dp)
    ) {
        Text(
            text = stringResource(R.string.saving_throws),
            style = MaterialTheme.typography.titleMedium,
        )

        savingThrows.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { saveThrow ->
                    SavingThrowItem(
                        text = stringResource(saveThrow.ability.nameRes),
                        value = formatModifier(saveThrow.modifier),
                        isProficient = saveThrow.isProficient,
                        onRollClick = { onRollClick(saveThrow.ability, saveThrow.modifier) },
                        onProficiencyChange = { onProficiencyChange(saveThrow.ability, it) },
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(2 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
fun SavingThrowItem(
    text: String,
    value: String,
    isProficient: Boolean,
    onRollClick: () -> Unit,
    onProficiencyChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
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
    Box(
        modifier = modifier.height(40.dp)
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = Color.Transparent,
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.matchParentSize()
        ) { }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
        ) {
            IconButton(
                onClick = { onProficiencyChange(!isProficient) },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = stringResource(R.string.saving_throw_proficiency, isProficient),
                    tint = tint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            ModifierButton(
                onClick = onRollClick,
                text = value,
            )
        }
    }
}
