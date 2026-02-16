package com.yablonskyi.dndsheet.ui.character.slides

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yablonskyi.dndsheet.R
import com.yablonskyi.dndsheet.data.model.character.Attack
import com.yablonskyi.dndsheet.data.model.dice.DiceRoles
import com.yablonskyi.dndsheet.ui.attack.AttackUiModel
import com.yablonskyi.dndsheet.ui.theme.DnDSheetTheme
import com.yablonskyi.dndsheet.ui.utils.UiUtils

@Composable
fun AttackSlide(
    attacks: List<AttackUiModel>,
    onAdd: () -> Unit,
    onUpdate: (Attack) -> Unit,
    onRollClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.attack_title).uppercase(),
                        textAlign = TextAlign.Left,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.weight(1.3f)
                    )
                    Text(
                        text = stringResource(R.string.attack_bonus_hit).uppercase(),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Text(
                        text = stringResource(R.string.attack_damage).uppercase(),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier
                            .weight(1f, false)
                            .width(100.dp)
                    )
                }
                HorizontalDivider()
            }

            items(
                items = attacks,
                key = { it.originalAttack.attackId }
            ) { attack ->
                AttackRow(
                    attack = attack,
                    onUpdate = { onUpdate(attack.originalAttack) },
                    onRollClick = onRollClick
                )
                HorizontalDivider()
            }
            item {
                Spacer(Modifier.height(16.dp))
                InsertAttackRow(
                    onAdd = onAdd
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
        Surface(
            color = MaterialTheme.colorScheme.background.copy(alpha = 0f),
            shape = MaterialTheme.shapes.extraSmall,
            border = BorderStroke(2.dp, color = MaterialTheme.colorScheme.outline),
            modifier = Modifier
                .clickable(
                    onClick = onAdd
                )
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun AttackRow(
    attack: AttackUiModel,
    onRollClick: (String) -> Unit,
    onUpdate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth().clickable(
            onClick = onUpdate
        )
    ) {
        Text(
            text = attack.name,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1.4f)
        )
        AttackButton(
            text = attack.toHit,
            onClick = { onRollClick("${DiceRoles.D20.roll}${attack.toHit}") },
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        AttackButton(
            text = attack.damage,
            onClick = { onRollClick(attack.damage) },
            modifier = Modifier
                .weight(1f, fill = false)
                .width(100.dp)
        )
    }
}

@Composable
fun AttackButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        border = BorderStroke(width = 2.dp, color = MaterialTheme.colorScheme.outline),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors().copy(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        modifier = modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Preview(locale = "uk")
@Composable
private fun AttackSlidePreview() {
    DnDSheetTheme {
        AttackSlide(
            attacks = UiUtils.sampleAttacks,
            onAdd = {},
            {},
            {},
        )
    }
}