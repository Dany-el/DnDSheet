package com.yablonskyi.dndsheet.ui.utils

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun IntTextField(
    value: Int,
    label: String,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    validate: (String) -> Boolean = { true },
    isError: Boolean = false,
    errorText: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Number,
        imeAction = ImeAction.Next
    ),
    enableSupportingText: Boolean = false,
    supportingText: String = "",
    keyboardActions: KeyboardActions = KeyboardActions()
) {

    var text by remember(value) { mutableStateOf(value.toString()) }

    OutlinedTextField(
        value = text,
        onValueChange = { newText ->
            if (newText.all { it.isDigit() } && validate(newText)) {
                text = newText
                val intValue = newText.toIntOrNull() ?: value // if null, pass old value
                onValueChange(intValue)
            }
        },
        label = {
            Text(
                label,
                maxLines = 2,
                overflow = TextOverflow.MiddleEllipsis
            )
        },
        supportingText = if (enableSupportingText) {
            {
                if (isError) Text(errorText, color = MaterialTheme.colorScheme.error)
                else Text(supportingText)
            }
        } else null,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = true,
        isError = isError,
        modifier = modifier
    )
}