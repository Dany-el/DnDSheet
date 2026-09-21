package com.yablonskyi.character.presentation.sheet.slides

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewDynamicColors
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.yablonskyi.character.presentation.common.UiUtils
import com.yablonskyi.character.presentation.sheet.components.SavingThrowGrid
import com.yablonskyi.character.presentation.sheet.mapper.AbilityUiModel
import com.yablonskyi.character.presentation.sheet.mapper.SavingThrowUiModel
import com.yablonskyi.character.presentation.sheet.mapper.toAbilityUiModel
import com.yablonskyi.character.presentation.sheet.mapper.toSavingThrows
import com.yablonskyi.model.character.Ability
import com.yablonskyi.ui.R
import com.yablonskyi.ui.components.StatFrame
import com.yablonskyi.ui.theme.Dimens
import com.yablonskyi.ui.utils.PreviewThemeWrapper
import com.yablonskyi.ui.utils.formatModifier

@Composable
fun AbilitySlide(
    abilities: List<AbilityUiModel>,
    savingThrows: List<SavingThrowUiModel>,
    onAbilityRoll: (Ability, Int) -> Unit,
    onSaveThrowRoll: (Ability, Int) -> Unit,
    onAbilityClick: (Ability) -> Unit,
    onProficiencyChange: (Ability, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
    ) {
        LazyColumn(
            contentPadding = PaddingValues(
                bottom = Dimens.Fab.BottomPadding
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item(key = "abilities") {
                AbilityGrid(
                    abilities = abilities,
                    onAbilityRoll = onAbilityRoll,
                    onAbilityClick = onAbilityClick
                )
            }
            item(key = "savingThrows") {
                SavingThrowGrid(
                    savingThrows = savingThrows,
                    onRollClick = onSaveThrowRoll,
                    onProficiencyChange = onProficiencyChange,
                )
            }
        }
    }
}

@Composable
fun AbilityGrid(
    abilities: List<AbilityUiModel>,
    onAbilityRoll: (Ability, Int) -> Unit,
    onAbilityClick: (Ability) -> Unit,
) {
    Column(
        modifier = Modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        abilities.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { model ->
                    AbilityItem(
                        ability = model.ability,
                        abilityMod = model.modifier,
                        score = model.score,
                        onRollClick = { onAbilityRoll(model.ability, model.modifier) },
                        onAbilityClick = { onAbilityClick(model.ability) },
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
fun AbilityItem(
    ability: Ability,
    score: Int,
    abilityMod: Int,
    onRollClick: () -> Unit,
    onAbilityClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    StatFrame(
        title = stringResource(ability.nameRes).take(3).uppercase(),
        score = "$score",
        value = formatModifier(abilityMod),
        onValueClick = onRollClick,
        onScoreClick = onAbilityClick,
        modifier = modifier
    )
}

@Composable
fun ModifierRow(
    abilityMod: Int,
    savingThrowMod: Int,
    isProficient: Boolean,
    onRollClick: () -> Unit,
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
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
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
                    onClick = onRollClick,
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
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.matchParentSize()
            ) { }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.clip(MaterialTheme.shapes.medium)
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
                    text = stringResource(R.string.saving_throw).uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                ModifierButton(
                    onClick = onRollClick,
                    text = formatModifier(savingThrowMod),
                )
            }
        }
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
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.tertiary),
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

@Preview(
    uiMode = Configuration.UI_MODE_TYPE_NORMAL, locale = "ru"
)
@Composable
private fun AbilitySlidePreview_Normal() {
    PreviewThemeWrapper.Preview {
        AbilitySlide(
            abilities = UiUtils.sampleCharacters.last().toAbilityUiModel(),
            savingThrows = UiUtils.sampleCharacters.last().toSavingThrows(),
            onAbilityClick = { },
            onAbilityRoll = { _, _ -> },
            onSaveThrowRoll = { _, _ -> },
            onProficiencyChange = { _, _ -> },
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
    PreviewThemeWrapper.Preview {
        AbilitySlide(
            abilities = UiUtils.sampleCharacters.last().toAbilityUiModel(),
            savingThrows = UiUtils.sampleCharacters.last().toSavingThrows(),
            onAbilityClick = { },
            onAbilityRoll = { _, _ -> },
            onSaveThrowRoll = { _, _ -> },
            onProficiencyChange = { _, _ -> },
        )
    }
}
