package com.yablonskyi.dndsheet.ui.settings

import android.content.Context
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yablonskyi.dndsheet.R
import com.yablonskyi.dndsheet.data.model.character.CharacterSheet
import com.yablonskyi.dndsheet.data.repository.SettingsRepository
import com.yablonskyi.dndsheet.data.rulebook.BuiltInRulebookLoader
import com.yablonskyi.dndsheet.domain.repository.CharacterRepository
import com.yablonskyi.dndsheet.ui.settings.LanguageChangeHelper.getActiveLanguageCode
import com.yablonskyi.dndsheet.ui.utils.AppLanguage
import com.yablonskyi.dndsheet.ui.utils.AppTheme
import com.yablonskyi.dndsheet.ui.utils.GoogleDriveSyncManager
import com.yablonskyi.dndsheet.ui.utils.decodeBase64ToImage
import com.yablonskyi.dndsheet.ui.utils.encodeImageToBase64
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class AppSettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
    private val charRepository: CharacterRepository,
    private val rulebookLoader: BuiltInRulebookLoader
) : ViewModel() {

    private val _language = MutableStateFlow(getActiveLanguageCode())
    val language: StateFlow<String> = _language

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing

    private val _isBackupAvailable = MutableStateFlow(false)
    val isBackupAvailable: StateFlow<Boolean> = _isBackupAvailable.asStateFlow()

    init {
        _language.value = getActiveLanguageCode()
    }

    val uiState: StateFlow<AppSettingsState> =
        combine(repository.appSettings, _language, _isSyncing) { state, lang, syncing ->
            state.copy(
                languageCode = lang,
                isSyncing = syncing
            )
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

        rulebookLoader.invalidateCache()
        AppCompatDelegate.setApplicationLocales(localeList)

        _language.value = code
    }

    fun syncLanguageWithSystem() {
        _language.value = getActiveLanguageCode()
    }

    fun updateListView(listView: ListView) {
        viewModelScope.launch {
            repository.saveListView(listView)
        }
    }

    fun toggleListView() {
        val view = when (uiState.value.listView) {
            ListView.LIST -> ListView.GRID
            ListView.GRID -> ListView.LIST
        }

        updateListView(view)
    }

    fun setLoggedInUser(email: String?) {
        if (email != null) {
            viewModelScope.launch {
                repository.saveUserEmail(email)
            }
        }
    }

    fun signOut(context: Context) {
        viewModelScope.launch {
            repository.clearSyncData()

            try {
                val credentialManager = CredentialManager.create(context)
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
                WorkManager.getInstance(context).cancelUniqueWork("HalfDayDriveBackup")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun syncWithDrive(context: Context) {
        if (_isSyncing.value) return

        viewModelScope.launch {
            _isSyncing.value = true

            try {
                val jsonString = withContext(Dispatchers.IO) {
                    val sheets = charRepository.getAllCharacterSheets()

                    val sheetsForExport = sheets.map { sheet ->
                        val base64String = encodeImageToBase64(sheet.character.imagePath)

                        sheet.copy(
                            character = sheet.character.copy(imagePath = base64String)
                        )
                    }
                    Gson().toJson(sheetsForExport)
                }

                val syncManager = GoogleDriveSyncManager(context)
                val result = syncManager.uploadBackup(jsonString)

                if (result.isSuccess) {
                    val currentTime = LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
                    )
                    repository.saveLastSyncTime(currentTime)
                    _isBackupAvailable.value = true
                } else {
                    val error = result.exceptionOrNull()
                    error?.printStackTrace()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun restoreFromDrive(context: Context) {
        if (_isSyncing.value) return

        viewModelScope.launch {
            _isSyncing.value = true

            try {
                val syncManager = GoogleDriveSyncManager(context)
                val result = syncManager.downloadBackup()

                if (result.isSuccess) {
                    val jsonString = result.getOrNull() ?: return@launch

                    withContext(Dispatchers.IO) {
                        val listType = object : TypeToken<List<CharacterSheet>>() {}.type
                        val downloadedSheets: List<CharacterSheet> =
                            Gson().fromJson(jsonString, listType)

                        val restoredSheets = downloadedSheets.map { sheet ->
                            val newLocalPath =
                                decodeBase64ToImage(context, sheet.character.imagePath)

                            sheet.copy(
                                character = sheet.character.copy(imagePath = newLocalPath)
                            )
                        }
                        charRepository.insertCharacters(restoredSheets)
                    }
                    val currentTime = LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
                    )
                    repository.saveLastSyncTime(currentTime)

                } else {
                    val error = result.exceptionOrNull()
                    error?.printStackTrace()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun checkIfBackupExists(context: Context) {
        viewModelScope.launch {
            try {
                val syncManager = GoogleDriveSyncManager(context)
                val result = syncManager.doesBackupExist()

                if (result.isSuccess) {
                    _isBackupAvailable.value = result.getOrDefault(false)
                } else {
                    _isBackupAvailable.value = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _isBackupAvailable.value = false
            }
        }
    }

    fun deleteBackupFromDrive(context: Context) {
        if (_isSyncing.value) return

        viewModelScope.launch {
            _isSyncing.value = true

            try {
                val syncManager = GoogleDriveSyncManager(context)
                val result = syncManager.deleteBackup()

                if (result.isSuccess) {
                    repository.saveLastSyncTime("Never synced")
                    _isBackupAvailable.value = false
                } else {
                    val error = result.exceptionOrNull()
                    error?.printStackTrace()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isSyncing.value = false
            }
        }
    }
}

data class AppSettingsState(
    val theme: AppTheme = AppTheme.SYSTEM,
    val languageCode: String = AppLanguage.ENGLISH.code,
    val listView: ListView = ListView.LIST,
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val userEmail: String? = null,
    val lastSyncTime: String? = null,
    val isSyncing: Boolean = false
)

enum class ListView(@StringRes val label: Int) {
    LIST(R.string.list),
    GRID(R.string.grid)
}