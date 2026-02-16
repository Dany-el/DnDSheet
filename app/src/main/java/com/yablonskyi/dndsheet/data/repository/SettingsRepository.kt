package com.yablonskyi.dndsheet.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.yablonskyi.dndsheet.ui.settings.AppSettingsState
import com.yablonskyi.dndsheet.ui.utils.AppTheme
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object Keys {
        val THEME = stringPreferencesKey("app_theme")
    }

    val appSettings: Flow<AppSettingsState> = context.dataStore.data
        .map { preferences ->
            val themeString = preferences[Keys.THEME] ?: AppTheme.SYSTEM.name

            AppSettingsState(
                theme = AppTheme.valueOf(themeString),
                isLoading = false
            )
        }

    suspend fun saveTheme(theme: AppTheme) {
        context.dataStore.edit { preferences ->
            preferences[Keys.THEME] = theme.name
        }
    }
}