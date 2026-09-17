package com.yablonskyi.ui.utils

import android.content.res.Resources
import java.text.Collator

/** Sorts by translated string resources using the current resource locale's alphabet rules. */
fun <T> Iterable<T>.sortedByTranslatedName(
    resources: Resources,
    nameRes: (T) -> Int,
): List<T> {
    val collator = Collator.getInstance(resources.configuration.locales[0])
    return map { item -> item to resources.getString(nameRes(item)) }
        .sortedWith { first, second -> collator.compare(first.second, second.second) }
        .map { it.first }
}
