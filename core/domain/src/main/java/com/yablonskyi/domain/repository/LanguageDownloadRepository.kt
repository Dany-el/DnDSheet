package com.yablonskyi.domain.repository

import android.app.Activity
import com.yablonskyi.model.language.LanguageDeliveryState
import kotlinx.coroutines.flow.StateFlow

interface LanguageDownloadRepository {
    val state: StateFlow<LanguageDeliveryState>

    fun refresh()
    fun requestLanguage(languageCode: String, applyLanguageCode: String = languageCode)
    fun cancelDownload()
    fun launchConfirmation(activity: Activity): Boolean
    fun clearTerminalState()
}
