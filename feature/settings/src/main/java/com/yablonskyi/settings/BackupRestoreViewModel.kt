package com.yablonskyi.settings

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yablonskyi.domain.repository.BackupError
import com.yablonskyi.domain.repository.BackupProgress
import com.yablonskyi.domain.repository.BackupResult
import com.yablonskyi.domain.repository.BackupSummary
import com.yablonskyi.domain.repository.FullBackupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class BackupRestoreViewModel @Inject constructor(
    private val repository: FullBackupRepository,
) : ViewModel() {
    private val workflow = MutableStateFlow(BackupWorkflowState())
    val uiState: StateFlow<BackupRestoreUiState> = combine(
        workflow,
        repository.progress,
    ) { state, progress ->
        BackupRestoreUiState(
            progress = progress,
            isOperationPending = state.isOperationPending,
            picker = state.picker,
            restoreSummary = state.restoreSummary,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = BackupRestoreUiState(),
    )

    private val mutableEvents = Channel<BackupRestoreEvent>(Channel.BUFFERED)
    val events = mutableEvents.receiveAsFlow()

    private var preparedToken: String? = null
    private var operationJob: Job? = null

    fun dismissConfirmation() {
        val token = preparedToken
        preparedToken = null
        workflow.update { it.copy(restoreSummary = null) }
        if (token != null) viewModelScope.launch { repository.discardPreparedRestore(token) }
    }

    fun requestCreateBackup() {
        if (isBusy()) return
        workflow.update { it.copy(restoreSummary = null, picker = BackupPicker.CREATE) }
        viewModelScope.launch {
            mutableEvents.send(BackupRestoreEvent.CreateDocument(defaultBackupFileName()))
        }
    }

    fun requestRestoreBackup() {
        if (isBusy()) return
        workflow.update { it.copy(restoreSummary = null, picker = BackupPicker.OPEN) }
        viewModelScope.launch { mutableEvents.send(BackupRestoreEvent.OpenDocument) }
    }

    fun pickerCancelled(picker: BackupPicker) {
        workflow.update { current ->
            if (current.picker == picker) current.copy(picker = null) else current
        }
    }

    fun pickerFailed(picker: BackupPicker) {
        pickerCancelled(picker)
        viewModelScope.launch {
            mutableEvents.send(BackupRestoreEvent.Message(BackupMessage.PICKER_UNAVAILABLE))
        }
    }

    fun createBackup(
        openDestination: () -> OutputStream,
        removePartialDestination: () -> Unit,
    ) {
        if (!consumePicker(BackupPicker.CREATE)) return
        launchOperation(
            operation = { repository.createBackup(openDestination, removePartialDestination) },
            onSuccess = {
                mutableEvents.send(BackupRestoreEvent.Message(BackupMessage.BACKUP_CREATED))
            },
        )
    }

    fun prepareRestore(openSource: () -> InputStream) {
        if (!consumePicker(BackupPicker.OPEN)) return
        launchOperation(
            operation = { repository.prepareRestore(openSource) },
            onSuccess = { prepared ->
                preparedToken = prepared.token
                workflow.update {
                    it.copy(restoreSummary = prepared.summary)
                }
            },
        )
    }

    fun confirmRestore() {
        if (isBusy()) return
        val token = preparedToken ?: return
        preparedToken = null
        workflow.update { it.copy(restoreSummary = null) }
        launchOperation(
            operation = { repository.restore(token) },
            onSuccess = {
                mutableEvents.send(BackupRestoreEvent.RestoreCompleted)
            },
        )
    }

    private fun consumePicker(expected: BackupPicker): Boolean {
        if (workflow.value.picker != expected || operationJob?.isActive == true) return false
        workflow.update { it.copy(picker = null) }
        return true
    }

    private fun <T> launchOperation(
        operation: suspend () -> BackupResult<T>,
        onSuccess: suspend (T) -> Unit,
    ) {
        if (operationJob?.isActive == true) return
        workflow.update { it.copy(isOperationPending = true) }
        operationJob = viewModelScope.launch {
            try {
                when (val result = operation()) {
                    is BackupResult.Success -> onSuccess(result.value)
                    is BackupResult.Failure -> mutableEvents.send(
                        BackupRestoreEvent.Message(result.error.toMessage()),
                    )
                }
            } finally {
                workflow.update { it.copy(isOperationPending = false) }
            }
        }
    }

    private fun isBusy(): Boolean = workflow.value.let {
        it.isOperationPending || it.picker != null
    } || repository.progress.value != BackupProgress.IDLE
}

@Immutable
data class BackupRestoreUiState(
    val progress: BackupProgress = BackupProgress.IDLE,
    val isOperationPending: Boolean = false,
    val picker: BackupPicker? = null,
    val restoreSummary: BackupSummary? = null,
) {
    val isBusy: Boolean
        get() = isOperationPending || picker != null || progress != BackupProgress.IDLE

    val isProcessing: Boolean
        get() = isOperationPending || progress != BackupProgress.IDLE
}

private data class BackupWorkflowState(
    val isOperationPending: Boolean = false,
    val picker: BackupPicker? = null,
    val restoreSummary: BackupSummary? = null,
)

enum class BackupPicker { CREATE, OPEN }

sealed interface BackupRestoreEvent {
    data class CreateDocument(val fileName: String) : BackupRestoreEvent
    data object OpenDocument : BackupRestoreEvent
    data object RestoreCompleted : BackupRestoreEvent
    data class Message(val message: BackupMessage) : BackupRestoreEvent
}

enum class BackupMessage {
    BACKUP_CREATED,
    PICKER_UNAVAILABLE,
    BUSY,
    UNSUPPORTED_VERSION,
    INVALID_ARCHIVE,
    MISSING_IMAGE,
    SIZE_LIMIT_EXCEEDED,
    PERMISSION_DENIED,
    INSUFFICIENT_STORAGE,
    IO_FAILURE,
    EXPIRED_RESTORE,
    RECOVERY_REQUIRED,
}

internal fun defaultBackupFileName(date: LocalDate = LocalDate.now()): String =
    "dnd-sheet_${date}.zip"

private fun BackupError.toMessage(): BackupMessage = when (this) {
    BackupError.BUSY -> BackupMessage.BUSY
    BackupError.UNSUPPORTED_VERSION -> BackupMessage.UNSUPPORTED_VERSION
    BackupError.INVALID_ARCHIVE -> BackupMessage.INVALID_ARCHIVE
    BackupError.MISSING_IMAGE -> BackupMessage.MISSING_IMAGE
    BackupError.SIZE_LIMIT_EXCEEDED -> BackupMessage.SIZE_LIMIT_EXCEEDED
    BackupError.PERMISSION_DENIED -> BackupMessage.PERMISSION_DENIED
    BackupError.INSUFFICIENT_STORAGE -> BackupMessage.INSUFFICIENT_STORAGE
    BackupError.IO_FAILURE -> BackupMessage.IO_FAILURE
    BackupError.EXPIRED_RESTORE -> BackupMessage.EXPIRED_RESTORE
    BackupError.RECOVERY_REQUIRED -> BackupMessage.RECOVERY_REQUIRED
}
