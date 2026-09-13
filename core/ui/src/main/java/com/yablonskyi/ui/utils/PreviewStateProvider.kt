package com.yablonskyi.ui.utils

interface PreviewStateProvider<T> {
    val default: T
    val samples: List<T>
}
