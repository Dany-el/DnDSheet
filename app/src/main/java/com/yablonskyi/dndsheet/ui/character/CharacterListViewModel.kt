package com.yablonskyi.dndsheet.ui.character

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yablonskyi.dndsheet.data.model.character.Character
import com.yablonskyi.dndsheet.data.model.character.CharacterSheet
import com.yablonskyi.dndsheet.domain.repository.CharacterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CharacterListViewModel @Inject constructor(
    private val repository: CharacterRepository
) : ViewModel() {

    private val _lastCreatedId = MutableStateFlow<Long?>(null)
    val lastCreatedId = _lastCreatedId.asStateFlow()

    val characterListState: StateFlow<CharacterListState> = repository.getAllCharacters()
        .map { characterList ->
            CharacterListState(
                characters = characterList,
                isLoading = false
            )
        }
        .stateIn(
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
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun createCharacter(character: Character) {
        _lastCreatedId.value = null
        viewModelScope.launch {
            val id = repository.insertCharacter(character)
            _lastCreatedId.value = id
        }
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

    fun clearLastCreatedId() {
        _lastCreatedId.value = null
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