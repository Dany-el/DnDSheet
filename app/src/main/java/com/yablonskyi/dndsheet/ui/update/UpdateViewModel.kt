package com.yablonskyi.dndsheet.ui.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yablonskyi.dndsheet.data.update.AppUpdate
import com.yablonskyi.dndsheet.data.update.UpdateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val repo: UpdateRepository
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
                repo.installApk(file)
            } else {
                _state.value = UpdateState.Error
            }
        }
    }

    fun installApk() {
        downloadedFile?.let { file ->
            repo.installApk(file)
        }
    }

    fun dismiss() {
        _state.value = UpdateState.Idle
    }
}