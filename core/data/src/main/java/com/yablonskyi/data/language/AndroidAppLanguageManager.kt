package com.yablonskyi.data.language

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.yablonskyi.data.rulebook.BuiltInRulebookLoader
import com.yablonskyi.domain.provider.AppLanguageManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidAppLanguageManager @Inject constructor(
    private val rulebookLoader: BuiltInRulebookLoader,
) : AppLanguageManager {
    override fun currentLanguageCode(): String {
        val locales = AppCompatDelegate.getApplicationLocales()
        return if (locales.isEmpty) SYSTEM_LANGUAGE else locales[0]?.language ?: SYSTEM_LANGUAGE
    }

    override fun applyLanguage(languageCode: String) {
        val locales = if (languageCode == SYSTEM_LANGUAGE) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(languageCode)
        }
        rulebookLoader.invalidateCache()
        AppCompatDelegate.setApplicationLocales(locales)
    }

    private companion object {
        const val SYSTEM_LANGUAGE = "system"
    }
}
