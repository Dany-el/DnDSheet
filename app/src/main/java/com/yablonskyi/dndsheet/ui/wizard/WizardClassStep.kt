package com.yablonskyi.dndsheet.ui.wizard

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yablonskyi.dndsheet.R
import com.yablonskyi.dndsheet.data.model.rulebook.CharacterClass
import com.yablonskyi.dndsheet.ui.theme.DnDSheetTheme
import com.yablonskyi.dndsheet.ui.utils.UiUtils

@Composable
fun WizardClassStep(
    origClasses: List<CharacterClass>,
    homebrewClasses: List<CharacterClass>,
    query: String,
    onQueryChange: (String) -> Unit,
    selectedClass: CharacterClass?,
    onClassSelected: (CharacterClass) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        stickyHeader {
            Surface(
                modifier = modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    placeholder = { Text(stringResource(R.string.search)) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = null)
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = MaterialTheme.shapes.medium,
                )
            }
        }

        item { WizardSectionHeader(title = stringResource(R.string.player_handbook)) }

        itemsIndexed(origClasses, key = { _, c -> c.id }) { index, cls ->

            val topCorners = if (origClasses.size == 1 || index == 0) 16.dp else 4.dp
            val bottomCorners =
                if (origClasses.size == 1 || index == origClasses.lastIndex) 16.dp else 4.dp

            val shape = RoundedCornerShape(
                topStart = topCorners,
                topEnd = topCorners,
                bottomStart = bottomCorners,
                bottomEnd = bottomCorners
            )

            ClassCard(
                cls = cls,
                shape = shape,
                isSelected = cls == selectedClass,
                onClick = { onClassSelected(cls) })
        }

        item { WizardSectionHeader(title = stringResource(R.string.homebrew)) }

        if (homebrewClasses.isEmpty()) {
            item {
                Text(
                    text = if (query.isBlank()) stringResource(R.string.wizard_no_homebrew_classes)
                    else stringResource(R.string.no_results),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                )
            }
        } else {
            itemsIndexed(homebrewClasses, key = { _, c -> c.id }) { index, cls ->

                val topCorners = if (homebrewClasses.size == 1 || index == 0) 16.dp else 4.dp
                val bottomCorners =
                    if (homebrewClasses.size == 1 || index == homebrewClasses.lastIndex) 16.dp else 4.dp

                val shape = RoundedCornerShape(
                    topStart = topCorners,
                    topEnd = topCorners,
                    bottomStart = bottomCorners,
                    bottomEnd = bottomCorners
                )

                ClassCard(
                    cls = cls,
                    shape = shape,
                    isSelected = cls == selectedClass,
                    onClick = { onClassSelected(cls) })
            }
        }
    }
}

@Composable
fun ClassCard(
    cls: CharacterClass,
    isSelected: Boolean,
    shape: RoundedCornerShape,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "classBorder"
    )

    val containerColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceContainerLow,
        label = "classContainer"
    )

    val resources = LocalResources.current

    Card(
        onClick = onClick,
        shape = shape,
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = borderColor,
                shape = shape
            ),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = cls.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 6.dp)
            ) {
                WizardChip(label = cls.hitDice)
                val skillLabel = pluralStringResource(
                    R.plurals.wizard_skill_choices, cls.skillChoiceCount, cls.skillChoiceCount
                )
                WizardChip(label = skillLabel)
                cls.spellcastingAbility?.let {
                    WizardChip(label = stringResource(R.string.wizard_spellcaster))
                }
                if (cls.isHomebrew) WizardChip(label = stringResource(R.string.homebrew))
            }
            // Saving throws
            if (cls.savingThrows.isNotEmpty()) {
                val savesText = cls.savingThrows.joinToString(", ") {
                    resources.getText(it.nameRes).toString().take(3).uppercase()
                }
                Text(
                    text = stringResource(R.string.wizard_saving_throws, savesText),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            // Description
            if (cls.description.isNotBlank()) {
                Text(
                    text = cls.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Preview
@Composable
private fun WizardClassStepPreview() {
    DnDSheetTheme {
        WizardClassStep(
            origClasses = UiUtils.sampleClasses.filter { !it.isHomebrew },
            homebrewClasses = UiUtils.sampleClasses.filter { it.isHomebrew },
            query = "",
            onQueryChange = { },
            selectedClass = UiUtils.sampleClasses.first(),
            onClassSelected = { },
        )
    }
}