package com.yablonskyi.settings

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.yablonskyi.domain.repository.CharacterRepository
import com.yablonskyi.model.character.CharacterSheet
import com.yablonskyi.data.utils.CharacterBackupCodec
import com.yablonskyi.data.utils.decodeBase64ToImage
import com.yablonskyi.data.utils.encodeImageToBase64
import com.yablonskyi.domain.provider.AppVersionProvider
import com.yablonskyi.domain.provider.AppLanguageManager
import com.yablonskyi.domain.repository.LanguageDownloadRepository
import com.yablonskyi.model.language.LanguageDownloadOperation
import com.yablonskyi.settings.repository.SettingsRepository
import com.yablonskyi.settings.utils.AppLanguage
import com.yablonskyi.settings.utils.AppTheme
import com.yablonskyi.settings.utils.GoogleDriveSyncManager
import com.yablonskyi.ui.settings.ListView
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
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
    private val appLanguageManager: AppLanguageManager,
    private val languageDownloadRepository: LanguageDownloadRepository,
    versionProvider: AppVersionProvider
) : ViewModel() {

    private val _language = MutableStateFlow(appLanguageManager.currentLanguageCode())
    val language: StateFlow<String> = _language

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing

    private val _isBackupAvailable = MutableStateFlow(false)
    val isBackupAvailable: StateFlow<Boolean> = _isBackupAvailable.asStateFlow()

    val appVersion: String = versionProvider.versionName

    init {
        _language.value = appLanguageManager.currentLanguageCode()
        viewModelScope.launch {
            languageDownloadRepository.state.collectLatest { deliveryState ->
                val installed = deliveryState.operation as? LanguageDownloadOperation.Installed
                if (installed?.shouldApply == true) {
                    appLanguageManager.applyLanguage(installed.languageCode)
                    _language.value = installed.languageCode
                    languageDownloadRepository.clearTerminalState()
                }
            }
        }
    }

    val uiState: StateFlow<AppSettingsState> =
        combine(repository.appSettings, _language, _isSyncing) { state, lang, syncing ->
            state.copy(
                languageCode = lang,
                googleAuthState = state.googleAuthState.copy(isSyncing = syncing)
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

    fun syncLanguageWithSystem() {
        _language.value = appLanguageManager.currentLanguageCode()
    }

    fun updateListView(listView: ListView) {
        viewModelScope.launch {
            repository.saveListView(listView)
        }
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
                    CharacterBackupCodec.encode(sheetsForExport)
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
                        val downloadedSheets: List<CharacterSheet> =
                            CharacterBackupCodec.decode(jsonString)

                        val restoredSheets = downloadedSheets.map { sheet ->
                            val newLocalPath =
                                decodeBase64ToImage(context, sheet.character.imagePath)

                            sheet.copy(
                                character = sheet.character.copy(imagePath = newLocalPath)
                            )
                        }
                        charRepository.restoreCharacters(restoredSheets)
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

@Immutable
data class AppSettingsState(
    val theme: AppTheme = AppTheme.SYSTEM,
    val languageCode: String = AppLanguage.ENGLISH.code,
    val listView: ListView = ListView.LIST,
    val googleAuthState: GoogleAuthState = GoogleAuthState(),
    val isLoading: Boolean = false,
)

@Immutable
data class GoogleAuthState(
    val isLoggedIn: Boolean = false,
    val userEmail: String? = null,
    val lastSyncTime: String? = null,
    val isSyncing: Boolean = false
)
