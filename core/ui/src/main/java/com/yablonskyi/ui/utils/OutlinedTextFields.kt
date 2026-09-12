package com.yablonskyi.ui.utils

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yablonskyi.ui.utils.controller.rememberIntTextFieldController
import com.yablonskyi.ui.validation.FieldState
import com.yablonskyi.ui.validation.IntFieldState

@Composable
fun DnDSheetOutlinedTextField(
    fieldState: FieldState,
    onValueChange: (String) -> Unit,
    label: String,
    onFocusChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.Next,
        capitalization = KeyboardCapitalization.Sentences
    ),
    keyboardActions: KeyboardActions = KeyboardActions(),
    minLines: Int = 1,
    maxLines: Int = 1,
    isRequired: Boolean = false,
) {
    OutlinedTextField(
        value = fieldState.text,
        onValueChange = onValueChange,
        label = {
            Text(text = "$label${if (isRequired) "*" else ""}")
        },
        isError = fieldState.error != null,
        supportingText = {
            when {
                fieldState.error != null ->
                    Text(
                        text = stringResource(fieldState.error),
                        color = MaterialTheme.colorScheme.error
                    )
                supportingText != null ->
                    Text(text = supportingText)
                else ->
                    Spacer(Modifier.height(0.dp))
            }
        },
        keyboardActions = keyboardActions,
        keyboardOptions = keyboardOptions,
        maxLines = maxLines,
        minLines = minLines,
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                onFocusChanged(focusState.isFocused)
            }
    )
}

@Composable
fun DnDSheetOutlinedTextField(
    fieldState: IntFieldState,
    onValueChange: (Int) -> Unit,
    label: String,
    onFocusChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    errorText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Number,
        imeAction = ImeAction.Next
    ),
) {
    val controller = rememberIntTextFieldController(fieldState.value)

    LaunchedEffect(fieldState.value) {
        controller.syncIfUnfocused(fieldState.value)
    }

    OutlinedTextField(
        value = controller.text,
        onValueChange = { controller.onTextChange(it)?.let(onValueChange) },
        label = { Text(label) },
        isError = fieldState.error != null,
        suffix = {
            Text(
                text = "/ ${fieldState.maxValue}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        supportingText = {
            when {
                errorText != null ->
                    Text(
                        text = errorText,
                        color = MaterialTheme.colorScheme.error
                    )
                supportingText != null ->
                    Text(text = supportingText)
                else ->
                    Spacer(Modifier.height(0.dp))
            }
        },
        keyboardOptions = keyboardOptions,
        singleLine = true,
        modifier = modifier.onFocusChanged { focusState ->
            controller.onFocusChange(focusState.isFocused, fieldState.value)
            onFocusChanged(focusState.isFocused)
        }
    )
}