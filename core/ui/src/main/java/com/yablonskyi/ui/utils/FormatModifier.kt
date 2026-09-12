package com.yablonskyi.ui.utils

fun formatModifier(value: Int): String {
    return if (value >= 0) "+$value" else "$value"
}