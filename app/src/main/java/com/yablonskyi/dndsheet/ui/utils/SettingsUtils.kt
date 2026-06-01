package com.yablonskyi.dndsheet.ui.utils

import androidx.annotation.StringRes
import com.yablonskyi.dndsheet.R

enum class AppTheme(@StringRes val label: Int) {
    SYSTEM(R.string.system_default),
    LIGHT(R.string.theme_light),
    DARK(R.string.theme_dark)
}

enum class AppLanguage(val code: String, @StringRes val label: Int) {
    SYSTEM("system", R.string.system_default),
    ENGLISH("en", R.string.language_english),
    RUSSIAN("ru", R.string.language_russian),
    UKRAINIAN("uk", R.string.language_ukrainian)
}