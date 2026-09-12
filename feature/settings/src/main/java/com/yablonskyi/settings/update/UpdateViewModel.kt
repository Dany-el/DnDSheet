package com.yablonskyi.settings.update

import android.app.Application
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yablonskyi.domain.repository.UpdateRepository
import com.yablonskyi.model.update.AppUpdate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val repo: UpdateRepository,
    private val app: Application
) : ViewModel() {

    sealed interface UpdateState {
        data object Idle : UpdateState
        data object UptoDate: UpdateState
        data class Available(val update: AppUpdate) : UpdateState
        data class Downloading(val progress: Float) : UpdateState
        data object ReadyToInstall : UpdateState
        data object Error : UpdateState
    }

    private val _state = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val state = _state.asStateFlow()

    private var downloadedFile: File? = null

    fun checkForUpdate() {
        viewModelScope.launch {
            val update = repo.fetchUpdate()
            _state.value = if (update != null) UpdateState.Available(update)
            else UpdateState.UptoDate
        }
    }

    fun download(update: AppUpdate) {
        viewModelScope.launch {
            _state.value = UpdateState.Downloading(0f)
            val file = repo.downloadApk(update.downloadUrl) { progress ->
                _state.value = UpdateState.Downloading(progress)
            }
            if (file != null) {
                downloadedFile = file
                _state.value = UpdateState.ReadyToInstall
            } else {
                _state.value = UpdateState.Error
            }
        }
    }

    fun installApk() {
        val file = downloadedFile ?: return
        val uri = FileProvider.getUriForFile(
            app,
            "${app.packageName}.provider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        app.startActivity(intent)
    }

    fun dismiss() {
        _state.value = UpdateState.Idle
    }
}
