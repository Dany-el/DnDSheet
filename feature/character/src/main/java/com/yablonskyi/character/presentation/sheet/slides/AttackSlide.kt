package com.yablonskyi.character.presentation.sheet.slides

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Grid
import androidx.compose.foundation.layout.GridTrackSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.unit.dp
import com.yablonskyi.character.presentation.common.UiUtils
import com.yablonskyi.character.presentation.sheet.model.AttackUiModel
import com.yablonskyi.model.character.Attack
import com.yablonskyi.ui.R
import com.yablonskyi.ui.theme.Dimens
import com.yablonskyi.ui.utils.PreviewThemeWrapper

@Composable
fun AttackSlide(
    attacks: List<AttackUiModel>,
    onAdd: () -> Unit,
    onUpdate: (Attack) -> Unit,
    onDamageRoll: (Attack, String) -> Unit,
    onAttackBonusRoll: (Attack, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val groupedByType = remember(attacks) {
        attacks.groupBy { attackUiModel -> attackUiModel.calculator.attack.attackType }
    }

    Box (
        modifier = modifier
    ) {
        LazyColumn(
            contentPadding = PaddingValues(
                start = 8.dp,
                top = 8.dp,
                end = 8.dp,
                bottom = 120.dp
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                Grid(
                    config = {
                        column(0.4f)
                        column(0.2f)
                        columnGap(8.dp)
                        column(0.4f)
                        row(GridTrackSize.Auto)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.attack_title).uppercase(),
                        textAlign = TextAlign.Left,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = stringResource(R.string.attack_bonus_hit).uppercase(),
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = stringResource(R.string.attack_damage).uppercase(),
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                HorizontalDivider()
            }

            groupedByType.forEach { (type, uiModels) ->
                stickyHeader("key-$type") {
                    Surface(
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.Start,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = Dimens.Spacing.Medium)
                        ) {
                            Text(
                                text = stringResource(type.resId),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
                items(
                    items = uiModels,
                    key = { it.calculator.attack.attackId }
                ) { uiModel ->

                    val attack = uiModel.calculator.attack
                    val hitModifier = uiModel.calculator.getToHitModifier()
                    val damageString = uiModel.calculator.getDamageString()

                    AttackRow(
                        attack = uiModel,
                        onUpdate = { onUpdate(attack) },
                        onDamageRoll = { onDamageRoll(attack, damageString) },
                        onAttackBonusRoll = { onAttackBonusRoll(attack, hitModifier) },
                        modifier = Modifier.animateItem()
                    )
                    HorizontalDivider()
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                InsertAttackRow(
                    onAdd = onAdd,
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}

@Composable
fun InsertAttackRow(
    onAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedIconButton(
            onClick = onAdd,
            modifier = Modifier
                .sizeIn(48.dp, 48.dp)
                .align(Alignment.Center)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.add),
            )
        }
    }
}

@Composable
fun AttackRow(
    attack: AttackUiModel,
    onAttackBonusRoll: () -> Unit,
    onDamageRoll: () -> Unit,
    onUpdate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box (
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                onClick = onUpdate,
                role = Role.Button
            )
            .heightIn(48.dp)
            .padding(vertical = 8.dp)
    ) {
        Grid(
            config = {
                column(0.4f)
                column(0.2f)
                columnGap(8.dp)
                column(0.4f)
                row(GridTrackSize.Auto)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier.fillMaxHeight()
            ) {
                Text(
                    text = attack.name,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Left,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            AttackButton(
                text = attack.toHit,
                onClick = onAttackBonusRoll,
                modifier = Modifier.fillMaxSize()
            )
            AttackButton(
                text = attack.damage,
                onClick = onDamageRoll,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun AttackButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton (
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.tertiary),
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.tertiary
        ),
        modifier = modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@PreviewFontScale
@Composable
private fun AttackSlidePreview() {
    PreviewThemeWrapper.Preview {
        AttackSlide(
            attacks = UiUtils.sampleAttacks,
            onAdd = {},
            {},
            {_, _ ->},
            {_, _ ->}
        )
    }
}