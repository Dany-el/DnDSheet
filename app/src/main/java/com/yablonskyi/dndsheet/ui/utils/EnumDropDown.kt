package com.yablonskyi.dndsheet.ui.utils

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)
@Composable
fun <T> EnumDropdown(
    value: T,
    labelRes: Int,
    options: List<T>,
    modifier: Modifier = Modifier,
    isRequired: Boolean = false,
    enableSupportingText: Boolean = false,
    isError: Boolean = false,
    errorText: String = "",
    supportingText: String = "",
    nameMapper: @Composable (T) -> String,
    onSelected: (T) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            focusManager.clearFocus()
            expanded = it
        },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = nameMapper(value),
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            isError = isError,
            label = {
                Text(
                    stringResource(labelRes) + if (isRequired) "*" else "",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(
                    type = MenuAnchorType.PrimaryNotEditable,
                    enabled = true
                ),
            supportingText = if (enableSupportingText) {
                {
                    if (isError) Text(errorText, color = MaterialTheme.colorScheme.error)
                    else Text(supportingText)
                }
            } else null,
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 250.dp)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            nameMapper(option),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = if (option == value) FontWeight.SemiBold else null
                        )
                    },
                    onClick = {
                        expanded = false
                        onSelected(option)
                    },
                    leadingIcon = if (option == value) {
                        { Icon(Icons.Default.Check, contentDescription = null) }
                    } else null
                )
            }
        }
    }
}