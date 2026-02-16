package com.yablonskyi.dndsheet.ui.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> MultiSelectDropdownChip(
    title: String,
    options: List<T>,
    selectedOptions: Set<T>,
    onToggle: (T) -> Unit,
    labelMapper: @Composable (T) -> String = { it.toString() }
) {
    var expanded by remember { mutableStateOf(false) }

    val labelText = when {
        selectedOptions.isEmpty() -> title
        selectedOptions.size == 1 -> labelMapper(selectedOptions.first())
        else -> "$title (${selectedOptions.size})"
    }

    val isSelected = selectedOptions.isNotEmpty()

    Box {
        FilterChip(
            selected = isSelected,
            onClick = { expanded = true },
            label = { Text(labelText) },
            trailingIcon = {
                Icon(
                    imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = null
                )
            }
        )

        val scrollState = rememberScrollState()

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            scrollState = scrollState,
            modifier = Modifier
                .heightIn(max = 180.dp)
                .simpleVerticalScrollbar(scrollState)
        ) {
            options.forEach { option ->
                val isOptionSelected = option in selectedOptions

                DropdownMenuItem(
                    text = { Text(labelMapper(option), style = MaterialTheme.typography.labelMedium) },
                    onClick = {
                        onToggle(option)
                    },
                    leadingIcon = {
                        Checkbox(
                            checked = isOptionSelected,
                            onCheckedChange = { onToggle(option) }
                        )
                    },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }
    }
}