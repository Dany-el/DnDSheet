package com.yablonskyi.dndsheet.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.yablonskyi.dndsheet.ui.settings.AppSettingsState
import com.yablonskyi.dndsheet.ui.settings.ListView
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
        val LIST_VIEW = stringPreferencesKey("list_view")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val LAST_SYNC_TIME = stringPreferencesKey("last_sync_time")
    }

    val appSettings: Flow<AppSettingsState> = context.dataStore.data
        .map { preferences ->
            val themeString = preferences[Keys.THEME] ?: AppTheme.SYSTEM.name
            val listViewString = preferences[Keys.LIST_VIEW] ?: ListView.LIST.name
            val email = preferences[Keys.USER_EMAIL]
            val lastSync = preferences[Keys.LAST_SYNC_TIME]

            AppSettingsState(
                theme = AppTheme.valueOf(themeString),
                listView = ListView.valueOf(listViewString),
                isLoading = false,
                isLoggedIn = email != null,
                userEmail = email,
                lastSyncTime = lastSync,
                isSyncing = false
            )
        }

    suspend fun saveTheme(theme: AppTheme) {
        context.dataStore.edit { preferences ->
            preferences[Keys.THEME] = theme.name
        }
    }

    suspend fun saveListView(listView: ListView) {
        context.dataStore.edit { preferences ->
            preferences[Keys.LIST_VIEW] = listView.name
        }
    }

    suspend fun saveUserEmail(email: String?) {
        context.dataStore.edit { preferences ->
            if (email != null) {
                preferences[Keys.USER_EMAIL] = email
            } else {
                preferences.remove(Keys.USER_EMAIL)
            }
        }
    }

    suspend fun saveLastSyncTime(time: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.LAST_SYNC_TIME] = time
        }
    }

    suspend fun clearSyncData() {
        context.dataStore.edit { preferences ->
            preferences.remove(Keys.USER_EMAIL)
            preferences.remove(Keys.LAST_SYNC_TIME)
        }
    }
}