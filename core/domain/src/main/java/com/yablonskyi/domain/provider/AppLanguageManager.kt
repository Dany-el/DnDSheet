package com.yablonskyi.domain.provider

interface AppLanguageManager {
    fun currentLanguageCode(): String
    fun applyLanguage(languageCode: String)
}
