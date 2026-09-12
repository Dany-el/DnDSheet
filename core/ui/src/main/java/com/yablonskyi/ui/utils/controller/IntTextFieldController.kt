package com.yablonskyi.ui.utils.controller

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Stable
class IntTextFieldController(initialValue: Int) {
    var text by mutableStateOf(initialValue.toString())
        private set
    var isFocused by mutableStateOf(false)
        private set

    // Returns the parsed Int only when input is valid — null means ignore
    fun onTextChange(newText: String): Int? {
        if (newText.isEmpty() || newText.all { it.isDigit() }) {
            text = newText
            return newText.toIntOrNull()
        }
        return null  // invalid input — don't update, don't propagate
    }

    fun onFocusChange(focused: Boolean, currentValue: Int) {
        isFocused = focused
        if (!focused && text.isEmpty()) text = currentValue.toString()
    }

    fun syncIfUnfocused(value: Int) {
        if (!isFocused) text = value.toString()
    }
}

@Composable
fun rememberIntTextFieldController(initialValue: Int): IntTextFieldController =
    remember { IntTextFieldController(initialValue) }