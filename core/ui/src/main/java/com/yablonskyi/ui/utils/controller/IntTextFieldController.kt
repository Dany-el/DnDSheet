package com.yablonskyi.ui.utils.controller

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Stable
class IntTextFieldController(initialValue: Int, private val allowSigned: Boolean = false) {
    var text by mutableStateOf(initialValue.toString())
        private set
    var isFocused by mutableStateOf(false)
        private set

    fun onTextChange(newText: String): Int? {
        if (newText.isEmpty() || (allowSigned && newText == "-")) {
            text = newText
            return null
        }
        val digits = if (allowSigned) newText.removePrefix("-") else newText
        if (digits.isEmpty() || !digits.all { it in '0'..'9' }) return null
        val parsed = newText.toIntOrNull() ?: return null
        text = newText
        return parsed
    }

    fun onFocusChange(focused: Boolean, currentValue: Int) {
        isFocused = focused
        if (!focused) text = currentValue.toString()
    }

    fun syncIfUnfocused(value: Int) {
        if (!isFocused) text = value.toString()
    }
}

@Composable
fun rememberIntTextFieldController(initialValue: Int, allowSigned: Boolean = false): IntTextFieldController =
    remember(allowSigned) { IntTextFieldController(initialValue, allowSigned) }