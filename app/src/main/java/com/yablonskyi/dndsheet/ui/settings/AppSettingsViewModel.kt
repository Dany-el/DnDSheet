package com.yablonskyi.dndsheet.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yablonskyi.dndsheet.data.repository.SettingsRepository
import com.yablonskyi.dndsheet.ui.settings.LanguageChangeHelper.getActiveLanguageCode
import com.yablonskyi.dndsheet.ui.utils.AppLanguage
import com.yablonskyi.dndsheet.ui.utils.AppTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppSettingsViewModel @Inject constructor(
    private val repository: SettingsRepository
) : ViewModel() {

    private val _language = MutableStateFlow(getActiveLanguageCode())
    val language: StateFlow<String> = _language

    init {
        _language.value = getActiveLanguageCode()
    }

    val uiState: StateFlow<AppSettingsState> =
        combine(repository.appSettings, _language) { state, lang ->
            state.copy(languageCode = lang)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            AppSettingsState(isLoading = true)
        )

    fun updateTheme(theme: AppTheme) {
        viewModelScope.launch {
            repository.saveTheme(theme)
        }
    }

    fun updateLanguage(code: String) {
        val localeList = if (code == AppLanguage.SYSTEM.code) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(code)
        }

        AppCompatDelegate.setApplicationLocales(localeList)

        _language.value = code
    }

    fun syncLanguageWithSystem() {
        _language.value = getActiveLanguageCode()
    }
}

data class AppSettingsState(
    val theme: AppTheme = AppTheme.SYSTEM,
    val languageCode: String = AppLanguage.ENGLISH.code,
    val isLoading: Boolean
)