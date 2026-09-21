package com.yablonskyi.model.language

data class LanguageDeliveryState(
    val installedLanguageCodes: Set<String> = emptySet(),
    val operation: LanguageDownloadOperation = LanguageDownloadOperation.Idle,
)

sealed interface LanguageDownloadOperation {
    data object Idle : LanguageDownloadOperation
    data class Pending(val languageCode: String) : LanguageDownloadOperation
    data class RequiresConfirmation(val languageCode: String) : LanguageDownloadOperation
    data class Downloading(
        val languageCode: String,
        val bytesDownloaded: Long,
        val totalBytes: Long,
    ) : LanguageDownloadOperation
    data class Installing(val languageCode: String) : LanguageDownloadOperation
    data class Canceling(val languageCode: String) : LanguageDownloadOperation
    data class Installed(val languageCode: String, val shouldApply: Boolean) : LanguageDownloadOperation
    data class Canceled(val languageCode: String) : LanguageDownloadOperation
    data class Failed(
        val languageCode: String,
        val reason: LanguageDownloadFailure,
        val applyLanguageCode: String = languageCode,
    ) : LanguageDownloadOperation
}

enum class LanguageDownloadFailure {
    NETWORK,
    STORAGE,
    PLAY_UNAVAILABLE,
    APP_NOT_OWNED,
    UNKNOWN,
}

enum class NetworkStatus {
    ONLINE,
    OFFLINE,
    UNKNOWN,
}
