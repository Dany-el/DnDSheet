package com.yablonskyi.data.language

import android.app.Activity
import android.content.Context
import com.google.android.play.core.splitinstall.SplitInstallException
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallSessionState
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallErrorCode
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import com.yablonskyi.domain.repository.LanguageDownloadRepository
import com.yablonskyi.model.language.LanguageDeliveryState
import com.yablonskyi.model.language.LanguageDownloadFailure
import com.yablonskyi.model.language.LanguageDownloadOperation
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayLanguageDownloadRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : LanguageDownloadRepository {
    private val manager: SplitInstallManager = SplitInstallManagerFactory.create(context)
    private val preferences = context.getSharedPreferences(STORE_NAME, Context.MODE_PRIVATE)
    private val _state = MutableStateFlow(
        LanguageDeliveryState(installedLanguageCodes = installedLanguages()),
    )
    override val state: StateFlow<LanguageDeliveryState> = _state.asStateFlow()

    private var confirmationState: SplitInstallSessionState? = null
    private val listener = SplitInstallStateUpdatedListener(::handleSessionState)

    init {
        manager.registerListener(listener)
        refresh()
    }

    override fun refresh() {
        val pendingCode = preferences.getString(KEY_LANGUAGE, null)
        val sessionId = preferences.getInt(KEY_SESSION, NO_SESSION)
        _state.value = _state.value.copy(installedLanguageCodes = installedLanguages())

        if (pendingCode == null) return
        manager.sessionStates
            .addOnSuccessListener { sessions ->
                val session = sessions.firstOrNull { it.sessionId() == sessionId }
                if (session != null) {
                    handleSessionState(session)
                } else if (isLanguageInstalled(pendingCode)) {
                    completeInstalled(pendingCode)
                } else {
                    val applyCode = preferences.getString(KEY_APPLY_LANGUAGE, pendingCode)
                        ?: pendingCode
                    clearPersistedRequest()
                    _state.value = _state.value.copy(
                        operation = LanguageDownloadOperation.Failed(
                            pendingCode,
                            LanguageDownloadFailure.UNKNOWN,
                            applyCode,
                        ),
                    )
                }
            }
            .addOnFailureListener {
                val applyCode = preferences.getString(KEY_APPLY_LANGUAGE, pendingCode)
                    ?: pendingCode
                _state.value = _state.value.copy(
                    operation = LanguageDownloadOperation.Failed(
                        pendingCode,
                        mapFailure(it),
                        applyCode,
                    ),
                )
            }
    }

    override fun requestLanguage(languageCode: String, applyLanguageCode: String) {
        if (isLanguageInstalled(languageCode)) {
            _state.value = _state.value.copy(
                operation = LanguageDownloadOperation.Installed(applyLanguageCode, shouldApply = true),
            )
            return
        }
        if (preferences.getString(KEY_LANGUAGE, null) != null) return

        persistRequest(languageCode, applyLanguageCode)
        _state.value = _state.value.copy(
            operation = LanguageDownloadOperation.Pending(languageCode),
        )
        val request = SplitInstallRequest.newBuilder()
            .addLanguage(Locale.forLanguageTag(languageCode))
            .build()
        manager.startInstall(request)
            .addOnSuccessListener { sessionId ->
                preferences.edit().putInt(KEY_SESSION, sessionId).apply()
                if (preferences.getBoolean(KEY_CANCELED, false)) {
                    manager.cancelInstall(sessionId)
                }
            }
            .addOnFailureListener { error ->
                val applyCode = preferences.getString(KEY_APPLY_LANGUAGE, applyLanguageCode)
                    ?: applyLanguageCode
                clearPersistedRequest()
                _state.value = _state.value.copy(
                    operation = LanguageDownloadOperation.Failed(
                        languageCode,
                        mapFailure(error),
                        applyCode,
                    ),
                )
            }
    }

    override fun cancelDownload() {
        val languageCode = preferences.getString(KEY_LANGUAGE, null) ?: return
        preferences.edit().putBoolean(KEY_CANCELED, true).apply()
        _state.value = _state.value.copy(
            operation = LanguageDownloadOperation.Canceling(languageCode),
        )
        val sessionId = preferences.getInt(KEY_SESSION, NO_SESSION)
        if (sessionId != NO_SESSION) {
            manager.cancelInstall(sessionId).addOnFailureListener {
                refresh()
            }
        }
    }

    override fun launchConfirmation(activity: Activity): Boolean {
        val state = confirmationState ?: return false
        return manager.startConfirmationDialogForResult(state, activity, CONFIRMATION_REQUEST_CODE)
    }

    override fun clearTerminalState() {
        when (_state.value.operation) {
            is LanguageDownloadOperation.Canceled,
            is LanguageDownloadOperation.Failed,
            is LanguageDownloadOperation.Installed -> {
                _state.value = _state.value.copy(operation = LanguageDownloadOperation.Idle)
            }
            else -> Unit
        }
    }

    private fun handleSessionState(session: SplitInstallSessionState) {
        val trackedSession = preferences.getInt(KEY_SESSION, NO_SESSION)
        if (trackedSession != NO_SESSION && session.sessionId() != trackedSession) return
        val languageCode = preferences.getString(KEY_LANGUAGE, null)
            ?: session.languages().firstOrNull()
            ?: return
        if (trackedSession == NO_SESSION) {
            preferences.edit().putInt(KEY_SESSION, session.sessionId()).apply()
        }

        val operation = when (session.status()) {
            SplitInstallSessionStatus.PENDING -> LanguageDownloadOperation.Pending(languageCode)
            SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION -> {
                confirmationState = session
                LanguageDownloadOperation.RequiresConfirmation(languageCode)
            }
            SplitInstallSessionStatus.DOWNLOADING -> LanguageDownloadOperation.Downloading(
                languageCode = languageCode,
                bytesDownloaded = session.bytesDownloaded(),
                totalBytes = session.totalBytesToDownload(),
            )
            SplitInstallSessionStatus.DOWNLOADED,
            SplitInstallSessionStatus.INSTALLING -> LanguageDownloadOperation.Installing(languageCode)
            SplitInstallSessionStatus.CANCELING -> LanguageDownloadOperation.Canceling(languageCode)
            SplitInstallSessionStatus.CANCELED -> {
                clearPersistedRequest()
                LanguageDownloadOperation.Canceled(languageCode)
            }
            SplitInstallSessionStatus.FAILED -> {
                val applyCode = preferences.getString(KEY_APPLY_LANGUAGE, languageCode)
                    ?: languageCode
                clearPersistedRequest()
                LanguageDownloadOperation.Failed(
                    languageCode,
                    mapFailure(session.errorCode()),
                    applyCode,
                )
            }
            SplitInstallSessionStatus.INSTALLED -> {
                completeInstalled(languageCode)
                return
            }
            else -> return
        }
        _state.value = LanguageDeliveryState(installedLanguages(), operation)
    }

    private fun completeInstalled(languageCode: String) {
        val shouldApply = !preferences.getBoolean(KEY_CANCELED, false)
        val applyLanguageCode = preferences.getString(KEY_APPLY_LANGUAGE, languageCode) ?: languageCode
        clearPersistedRequest()
        confirmationState = null
        _state.value = LanguageDeliveryState(
            installedLanguageCodes = installedLanguages() + languageCode,
            operation = LanguageDownloadOperation.Installed(applyLanguageCode, shouldApply),
        )
    }

    private fun installedLanguages(): Set<String> {
        return if (context.applicationInfo.splitSourceDirs.isNullOrEmpty()) {
            SUPPORTED_LANGUAGES
        } else {
            manager.installedLanguages.orEmpty() + DEFAULT_LANGUAGE
        }
    }

    private fun isLanguageInstalled(languageCode: String): Boolean =
        languageCode == SYSTEM_LANGUAGE || languageCode in installedLanguages()

    private fun persistRequest(languageCode: String, applyLanguageCode: String) {
        preferences.edit()
            .putString(KEY_LANGUAGE, languageCode)
            .putString(KEY_APPLY_LANGUAGE, applyLanguageCode)
            .putInt(KEY_SESSION, NO_SESSION)
            .putBoolean(KEY_CANCELED, false)
            .apply()
    }

    private fun clearPersistedRequest() {
        preferences.edit().clear().apply()
    }

    private fun mapFailure(error: Throwable): LanguageDownloadFailure =
        mapFailure((error as? SplitInstallException)?.errorCode ?: SplitInstallErrorCode.INTERNAL_ERROR)

    private fun mapFailure(errorCode: Int): LanguageDownloadFailure = when (errorCode) {
        SplitInstallErrorCode.NETWORK_ERROR -> LanguageDownloadFailure.NETWORK
        SplitInstallErrorCode.INSUFFICIENT_STORAGE -> LanguageDownloadFailure.STORAGE
        SplitInstallErrorCode.API_NOT_AVAILABLE -> LanguageDownloadFailure.PLAY_UNAVAILABLE
        SplitInstallErrorCode.APP_NOT_OWNED -> LanguageDownloadFailure.APP_NOT_OWNED
        else -> LanguageDownloadFailure.UNKNOWN
    }

    private companion object {
        const val STORE_NAME = "language_download"
        const val KEY_LANGUAGE = "language"
        const val KEY_APPLY_LANGUAGE = "apply_language"
        const val KEY_SESSION = "session"
        const val KEY_CANCELED = "canceled"
        const val NO_SESSION = 0
        const val CONFIRMATION_REQUEST_CODE = 9102
        const val DEFAULT_LANGUAGE = "en"
        const val SYSTEM_LANGUAGE = "system"
        val SUPPORTED_LANGUAGES = setOf("en", "ru", "uk")
    }
}
