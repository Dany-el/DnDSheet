package com.yablonskyi.compendium.classes.viewmodel

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yablonskyi.domain.repository.ClassRepository
import com.yablonskyi.model.rulebook.CharacterClass
import com.yablonskyi.ui.R
import com.yablonskyi.ui.utils.FileOperationEffect
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class CharacterClassesViewModel @Inject constructor(
    private val repo: ClassRepository,
) : ViewModel() {
    private val _effect = Channel<ClassesEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private val _fileEffect = Channel<FileOperationEffect>(Channel.BUFFERED)
    val fileEffect = _fileEffect.receiveAsFlow()

    private val _internalState = MutableStateFlow(InternalUiState())

    private val classContent = combine(
        repo.getAllClasses(),
        _internalState.map { it.searchQuery }.distinctUntilChanged()
    ) { allClasses, query ->
        val (homebrew, original) = allClasses.partition { it.isHomebrew }
        val filteredOriginal = if (query.isBlank()) original
        else original.filter { it.name.contains(query, ignoreCase = true) }
        val filteredHomebrew = if (query.isBlank()) homebrew
        else homebrew.filter { it.name.contains(query, ignoreCase = true) }

        ClassContent(
            original = filteredOriginal,
            homebrew = filteredHomebrew
        )
    }

    val uiState: StateFlow<ClassUiState> = combine(
        classContent,
        _internalState
    ) { content, internal ->
        ClassUiState(
            origClasses = content.original,
            homebrewClasses = content.homebrew,
            selectedClassesIds = internal.selectedClassIds,
            isSelectionMode = internal.isSelectionMode,
            isAllSelected = content.homebrewIds.isNotEmpty() &&
                    internal.selectedClassIds.containsAll(content.homebrewIds),
            searchQuery = internal.searchQuery,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = ClassUiState(isLoading = true)
    )

    fun onIntent(intent: ClassesIntent) {
        when (intent) {
            is ClassesIntent.SearchQueryChanged -> onSearchQueryChange(intent.value)
            is ClassesIntent.ToggleSelection -> toggleClassSelection(intent.classId)
            is ClassesIntent.ShareRequested -> shareRequested(intent.classId)
            is ClassesIntent.ImportRequested -> importClassesFromJson(intent.json)
            is ClassesIntent.Delete -> delete(intent.cls)
            is ClassesIntent.Edit -> sendEffect(ClassesEffect.NavigateToEdit(intent.classId))
            is ClassesIntent.Details -> sendEffect(ClassesEffect.NavigateToDetails(intent.classId))
            ClassesIntent.EnterSelectionMode -> _internalState.update { it.copy(isSelectionMode = true, selectedClassIds = emptySet()) }
            ClassesIntent.ClearSelection -> clearSelection()
            ClassesIntent.DeleteSelected -> deleteSelected()
            ClassesIntent.ToggleSelectAll -> toggleSelectAll()
            ClassesIntent.ExportAllSelected -> exportAllSelected()
            ClassesIntent.RequestFilePicker -> requestFilePicker()
            ClassesIntent.ShareFailed -> sendEffect(ClassesEffect.ShowSnackbar(R.string.failure_share))
            is ClassesIntent.ExportCompleted -> onExportCompleted(intent.success)
            ClassesIntent.CreateClass -> sendEffect(ClassesEffect.NavigateToCreate)
            ClassesIntent.NavigateBack -> sendEffect(ClassesEffect.NavigateBack)
        }
    }

    private fun sendEffect(effect: ClassesEffect) {
        viewModelScope.launch { _effect.send(effect) }
    }

    private fun onSearchQueryChange(query: String) {
        _internalState.update { it.copy(searchQuery = query) }
    }

    private fun toggleClassSelection(classId: String) {
        _internalState.value = _internalState.value.copy(
            isSelectionMode = true,
            selectedClassIds = _internalState.value.selectedClassIds.toMutableSet().apply {
                if (!add(classId)) remove(classId)
            }
        )
    }

    private fun clearSelection() {
        _internalState.update { it.copy(selectedClassIds = emptySet(), isSelectionMode = false) }
    }

    private fun toggleSelectAll() {
        val allHomebrewIds = uiState.value.homebrewClasses.map { it.id }.toSet()
        _internalState.update { internal ->
            val newIds = if (internal.selectedClassIds.containsAll(allHomebrewIds)) internal.selectedClassIds - allHomebrewIds
            else internal.selectedClassIds + allHomebrewIds
            internal.copy(selectedClassIds = newIds, isSelectionMode = true)
        }
    }

    private fun delete(cls: CharacterClass) {
        viewModelScope.launch { repo.delete(cls) }
    }

    private fun deleteSelected() {
        val idsToDelete = _internalState.value.selectedClassIds
        viewModelScope.launch {
            repo.getAllClasses().first().filter { it.isHomebrew }
                .asSequence()
                .filter { it.id in idsToDelete }
                .forEach { repo.delete(it) }
            clearSelection()
        }
    }

    private fun shareRequested(classId: String) {
        val cls = uiState.value.homebrewClasses.firstOrNull {
            it.id.trim().equals(classId.trim(), ignoreCase = true)
        } ?: return
        viewModelScope.launch {
            _fileEffect.send(
                FileOperationEffect.ShareReady(
                    fileName = "${cls.name.replace(" ", "_")}.json",
                    json = Json.encodeToString(listOf(cls))
                )
            )
        }
    }

    private fun exportAllSelected() {
        val selectedIds = _internalState.value.selectedClassIds
        if (selectedIds.isEmpty()) return
        viewModelScope.launch {
            val selected = repo.getAllClasses().first().filter { it.isHomebrew && it.id in selectedIds }
            if (selected.isEmpty()) return@launch
            _fileEffect.send(
                FileOperationEffect.ExportReady(
                    fileName = "classes_backup.json",
                    json = Json.encodeToString(selected)
                )
            )
        }
    }

    private fun requestFilePicker() {
        viewModelScope.launch { _fileEffect.send(FileOperationEffect.OpenImportPicker) }
    }

    private fun onExportCompleted(success: Boolean) {
        if (success) clearSelection()
        sendEffect(
            if (success) ClassesEffect.ShowSnackbar(R.string.success_export)
            else ClassesEffect.ShowSnackbar(R.string.failure_export)
        )
    }

    private fun importClassesFromJson(jsonString: String) {
        viewModelScope.launch {
            try {
                val importedClasses = Json.decodeFromString<List<CharacterClass>>(jsonString)
                if (importedClasses.isEmpty()) {
                    sendEffect(ClassesEffect.ShowSnackbar(R.string.import_file_empty))
                } else {
                    val preparedClasses = importedClasses.map { it.copy(isHomebrew = true) }
                    repo.insertAll(preparedClasses)
                    sendEffect(ClassesEffect.ShowSnackbar(R.string.success_import))
                }
            } catch (_: Exception) {
                sendEffect(ClassesEffect.ShowSnackbar(R.string.failure_import))
            }
        }
    }

    private data class InternalUiState(
        val isSelectionMode: Boolean = false,
        val selectedClassIds: Set<String> = emptySet(),
        val searchQuery: String = "",
    )

    private data class ClassContent(
        val original: List<CharacterClass>,
        val homebrew: List<CharacterClass>
    ) {
        val homebrewIds: Set<String> = homebrew.mapTo(mutableSetOf()) { it.id }
    }
}

sealed interface ClassesIntent {
    data class SearchQueryChanged(val value: String) : ClassesIntent
    data object EnterSelectionMode : ClassesIntent
    data object ClearSelection : ClassesIntent
    data object DeleteSelected : ClassesIntent
    data object ToggleSelectAll : ClassesIntent
    data class ToggleSelection(val classId: String) : ClassesIntent
    data class ShareRequested(val classId: String) : ClassesIntent
    data object ShareFailed : ClassesIntent
    data object ExportAllSelected : ClassesIntent
    data object RequestFilePicker : ClassesIntent
    data class ExportCompleted(val success: Boolean) : ClassesIntent
    data class ImportRequested(val json: String) : ClassesIntent
    data class Delete(val cls: CharacterClass) : ClassesIntent
    data object CreateClass : ClassesIntent
    data class Edit(val classId: String) : ClassesIntent
    data class Details(val classId: String) : ClassesIntent
    data object NavigateBack : ClassesIntent
}

sealed interface ClassesEffect {
    data object NavigateBack : ClassesEffect
    data object NavigateToCreate : ClassesEffect
    data class NavigateToEdit(val classId: String) : ClassesEffect
    data class NavigateToDetails(val classId: String) : ClassesEffect
    data class ShowSnackbar(@param:StringRes val messageRes: Int) : ClassesEffect
}

@Immutable
data class ClassUiState(
    val origClasses: List<CharacterClass> = emptyList(),
    val homebrewClasses: List<CharacterClass> = emptyList(),
    val selectedClassesIds: Set<String> = emptySet(),
    val isSelectionMode: Boolean = false,
    val isAllSelected: Boolean = false,
    val searchQuery: String = "",
    val isLoading: Boolean = true
) {
    val listIsEmpty = origClasses.isEmpty() && homebrewClasses.isEmpty()
    val anyListIsEmpty = origClasses.isEmpty() || homebrewClasses.isEmpty()
}