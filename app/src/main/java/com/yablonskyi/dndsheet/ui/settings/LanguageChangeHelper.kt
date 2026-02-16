package com.yablonskyi.dndsheet.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import com.yablonskyi.dndsheet.ui.utils.AppLanguage

object LanguageChangeHelper {

    fun getActiveLanguageCode(): String {
        val locales = AppCompatDelegate.getApplicationLocales()

        if (locales.isEmpty) return AppLanguage.SYSTEM.code

        val currentCode = locales[0]?.language ?: AppLanguage.SYSTEM.code

        return if (AppLanguage.entries.any { it.code == currentCode }) {
            currentCode
        } else {
            AppLanguage.SYSTEM.code
        }
    }
}