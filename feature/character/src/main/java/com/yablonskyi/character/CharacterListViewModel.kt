package com.yablonskyi.character

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yablonskyi.domain.CharacterSheetHtmlRenderer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import com.yablonskyi.domain.repository.CharacterRepository
import com.yablonskyi.model.character.Character
import com.yablonskyi.model.character.CharacterSheet
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CharacterListViewModel @Inject constructor(
    private val repository: CharacterRepository,
    private val htmlRenderer: CharacterSheetHtmlRenderer,
) : ViewModel() {

    private val _printState = MutableStateFlow<CharacterPrintState>(CharacterPrintState.Idle)
    val printState = _printState.asStateFlow()
    private val effects = Channel<CharacterPrintEffect>(
        capacity = Channel.BUFFERED,
        onUndeliveredElement = { effect ->
            if (effect is CharacterPrintEffect.LaunchPrint) onPrintLaunchCancelled(effect.requestId)
        },
    )
    val printEffects = effects.receiveAsFlow()
    private var nextPrintRequestId = 0L

    fun prepareCharacterSheetPrint(characterId: Long, languageCode: String) {
        val preparing = CharacterPrintState.Preparing(characterId)
        if (!_printState.compareAndSet(CharacterPrintState.Idle, preparing)) return
        val language = normalizePrintLanguage(languageCode)
        viewModelScope.launch {
            var error = CharacterPrintError.LOAD_FAILED
            try {
                val sheet = repository.getCharacterSheetById(characterId)
                error = CharacterPrintError.RENDER_FAILED
                val rendered = htmlRenderer.render(sheet, language).getOrThrow()
                val requestId = ++nextPrintRequestId
                _printState.value = CharacterPrintState.Launching(requestId, characterId)
                effects.send(CharacterPrintEffect.LaunchPrint(requestId, rendered.html, rendered.jobName))
            } catch (cancelled: CancellationException) {
                _printState.value = CharacterPrintState.Idle
                throw cancelled
            } catch (_: Exception) {
                _printState.value = CharacterPrintState.Idle
                effects.send(CharacterPrintEffect.Failed(error))
            }
        }
    }

    fun claimPrintRequest(requestId: Long): Boolean {
        val current = _printState.value as? CharacterPrintState.Launching ?: return false
        return current.requestId == requestId && !current.claimed &&
            _printState.compareAndSet(current, current.copy(claimed = true))
    }

    fun onPrintLaunchResult(requestId: Long, result: Result<Unit>) {
        if (!finishPrintRequest(requestId)) return
        if (result.exceptionOrNull() is CancellationException) return
        viewModelScope.launch {
            effects.send(if (result.isSuccess) CharacterPrintEffect.PrintRequestAccepted
                else CharacterPrintEffect.Failed(CharacterPrintError.PRINT_LAUNCH_FAILED))
        }
    }

    fun onPrintLaunchCancelled(requestId: Long) { finishPrintRequest(requestId) }

    private fun finishPrintRequest(requestId: Long): Boolean {
        val current = _printState.value as? CharacterPrintState.Launching ?: return false
        return current.requestId == requestId && _printState.compareAndSet(current, CharacterPrintState.Idle)
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val characterListState: StateFlow<CharacterListState> = combine(
        repository.getAllCharacters(),
        _searchQuery
    ) { characters, query ->
        val filteredList = if (query.isBlank()) {
            characters
        } else {
            characters.filter { char ->
                char.name.contains(query, ignoreCase = true)
            }
        }

        CharacterListState(
            characters = filteredList,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CharacterListState(isLoading = true)
    )

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode = _isSelectionMode.asStateFlow()

    private val _selectedCharacters = MutableStateFlow<Set<Character>>(emptySet())
    val selectedCharacters = _selectedCharacters.asStateFlow()

    val isAllSelected: StateFlow<Boolean> =
        combine(_selectedCharacters, characterListState) { selected, state ->
            selected.size == state.characters.size && state.characters.isNotEmpty()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun importSheets(importedSheets: List<CharacterSheet>) {
        viewModelScope.launch {
            try {
                repository.insertCharacters(importedSheets)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun getSheetsForExport(): List<CharacterSheet> {
        val selectedIds = _selectedCharacters.value.map { it.id }
        if (selectedIds.isEmpty()) return emptyList()

        return repository.getCharacterSheetsByIds(selectedIds)
    }

    fun deleteCharacter(character: Character) {
        viewModelScope.launch {
            repository.deleteCharacter(character)
        }
    }

    fun toggleSelection(character: Character) {
        if (!_isSelectionMode.value) {
            _isSelectionMode.value = true
        }

        _selectedCharacters.update { currentSelection ->
            if (currentSelection.contains(character)) {
                currentSelection - character
            } else {
                currentSelection + character
            }
        }
    }

    fun toggleSelectAll() {
        if (isAllSelected.value) {
            _selectedCharacters.value = emptySet()
        } else {
            _selectedCharacters.value = characterListState.value.characters.toSet()
        }
    }

    fun closeSelection() {
        _isSelectionMode.value = false
        _selectedCharacters.value = emptySet()
    }

    fun deleteSelectedCharacters() {
        viewModelScope.launch {
            val charsToDelete = _selectedCharacters.value.toList()
            repository.deleteCharacters(charsToDelete)
            closeSelection()
        }
    }
}

data class CharacterListState(
    val characters: List<Character> = emptyList(),
    val isLoading: Boolean = false
)
