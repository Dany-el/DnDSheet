package com.yablonskyi.settings.language

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yablonskyi.domain.provider.AppLanguageManager
import com.yablonskyi.domain.provider.NetworkStatusProvider
import com.yablonskyi.domain.repository.LanguageDownloadRepository
import com.yablonskyi.model.language.LanguageDeliveryState
import com.yablonskyi.model.language.LanguageDownloadOperation
import com.yablonskyi.model.language.NetworkStatus
import com.yablonskyi.settings.utils.AppLanguage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class LanguagesViewModel @Inject constructor(
    private val repository: LanguageDownloadRepository,
    private val languageManager: AppLanguageManager,
    networkStatusProvider: NetworkStatusProvider,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        LanguagesUiState(
            selectedLanguageCode = languageManager.currentLanguageCode(),
            deliveryState = repository.state.value,
        ),
    )
    val uiState: StateFlow<LanguagesUiState> = _uiState.asStateFlow()

    private val _events = Channel<LanguagesEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val networkStatus = networkStatusProvider.status.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = NetworkStatus.UNKNOWN,
    )
    private var stallJob: Job? = null
    private var offlineReported = false

    init {
        repository.refresh()
        viewModelScope.launch {
            repository.state.collect { state ->
                _uiState.update { currentState ->
                    val installed = state.operation as? LanguageDownloadOperation.Installed
                    currentState.copy(
                        selectedLanguageCode = installed
                            ?.takeIf { it.shouldApply }
                            ?.languageCode
                            ?: currentState.selectedLanguageCode,
                        deliveryState = state,
                    )
                }
                watchForStall(state.operation)
            }
        }
        viewModelScope.launch {
            networkStatus.collect { status ->
                if (status == NetworkStatus.OFFLINE && uiState.value.isDownloadActive) {
                    if (!offlineReported) _events.send(LanguagesEvent.NoInternet)
                    offlineReported = true
                } else if (status == NetworkStatus.ONLINE) {
                    offlineReported = false
                }
            }
        }
    }

    fun selectLanguage(language: AppLanguage) {
        if (uiState.value.isDownloadActive) return
        if (language.code != AppLanguage.SYSTEM.code && networkStatus.value == NetworkStatus.OFFLINE &&
            language.code !in repository.state.value.installedLanguageCodes
        ) {
            viewModelScope.launch { _events.send(LanguagesEvent.NoInternet) }
            return
        }
        repository.clearTerminalState()
        if (language == AppLanguage.SYSTEM) {
            val effectiveLanguage = Locale.getDefault().language
                .takeIf { code -> AppLanguage.entries.any { it.code == code } }
                ?: AppLanguage.ENGLISH.code
            repository.requestLanguage(
                languageCode = effectiveLanguage,
                applyLanguageCode = AppLanguage.SYSTEM.code,
            )
        } else {
            repository.requestLanguage(language.code)
        }
    }

    fun syncSelectedLanguage() {
        val languageCode = languageManager.currentLanguageCode()
        _uiState.update { it.copy(selectedLanguageCode = languageCode) }
    }

    fun cancel() = repository.cancelDownload()

    fun retry() {
        val failed = uiState.value.deliveryState.operation as? LanguageDownloadOperation.Failed
            ?: return
        repository.clearTerminalState()
        repository.requestLanguage(failed.languageCode, failed.applyLanguageCode)
    }

    fun launchConfirmation(activity: Activity) {
        repository.launchConfirmation(activity)
    }

    private fun watchForStall(operation: LanguageDownloadOperation) {
        stallJob?.cancel()
        val downloading = operation as? LanguageDownloadOperation.Downloading ?: return
        stallJob = viewModelScope.launch {
            delay(SLOW_CONNECTION_THRESHOLD_MS)
            val current = repository.state.value.operation
            if (
                current is LanguageDownloadOperation.Downloading &&
                current.languageCode == downloading.languageCode &&
                current.bytesDownloaded == downloading.bytesDownloaded &&
                networkStatus.value == NetworkStatus.ONLINE
            ) {
                _events.send(LanguagesEvent.SlowConnection)
            }
        }
    }

    private companion object {
        const val SLOW_CONNECTION_THRESHOLD_MS = 15_000L
    }
}

data class LanguagesUiState(
    val selectedLanguageCode: String,
    val deliveryState: LanguageDeliveryState,
) {
    val isDownloadActive: Boolean
        get() = when (deliveryState.operation) {
            is LanguageDownloadOperation.Pending,
            is LanguageDownloadOperation.RequiresConfirmation,
            is LanguageDownloadOperation.Downloading,
            is LanguageDownloadOperation.Installing,
            is LanguageDownloadOperation.Canceling -> true
            else -> false
        }
}

enum class LanguagesEvent {
    SlowConnection,
    NoInternet,
}
