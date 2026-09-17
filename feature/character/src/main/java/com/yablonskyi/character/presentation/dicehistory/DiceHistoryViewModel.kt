package com.yablonskyi.character.presentation.dicehistory

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yablonskyi.dice.DiceIntent
import com.yablonskyi.domain.repository.DiceRollRepository
import com.yablonskyi.model.dice.SavedDiceRoll
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DiceHistoryViewModel @Inject constructor(
    private val repository: DiceRollRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val characterId = checkNotNull(savedStateHandle.get<Long>("characterId")) {
        "Dice history requires a characterId route argument"
    }
    private val refresh = MutableStateFlow(0)
    private val _state = MutableStateFlow(DiceHistoryState())
    val state = _state.asStateFlow()
    private var failedOperation: FailedOperation? = null

    init {
        viewModelScope.launch {
            refresh.flatMapLatest {
                repository.observeDiceRolls(characterId)
                    .onStart { _state.update { state -> state.copy(isLoading = true, error = null) } }
                    .catch { error ->
                        if (error is CancellationException) throw error
                        failedOperation = FailedOperation.LOAD
                        _state.update { state -> state.copy(isLoading = false, error = error) }
                    }
            }.collect { rolls ->
                failedOperation = null
                _state.value = DiceHistoryState(rolls = rolls, isLoading = false)
            }
        }
    }

    fun onIntent(intent: DiceIntent) {
        when (intent) {
            DiceIntent.ClearDiceRolls -> clearHistory()
            DiceIntent.RetryPersistence -> retry()
            DiceIntent.DismissPersistenceError -> {
                failedOperation = null
                _state.update { it.copy(error = null) }
            }
            else -> Unit
        }
    }

    private fun clearHistory() {
        if (_state.value.isClearing) return
        viewModelScope.launch {
            _state.update { it.copy(isClearing = true, error = null) }
            repository.clearDiceRolls(characterId).fold(
                onSuccess = {
                    failedOperation = null
                    _state.update { it.copy(isClearing = false) }
                },
                onFailure = { error ->
                    failedOperation = FailedOperation.CLEAR
                    _state.update { it.copy(isClearing = false, error = error) }
                },
            )
        }
    }

    private fun retry() {
        when (failedOperation) {
            FailedOperation.CLEAR -> clearHistory()
            FailedOperation.LOAD -> refresh.value += 1
            null -> Unit
        }
    }

    private enum class FailedOperation { LOAD, CLEAR }

}

@Immutable
data class DiceHistoryState(
    val rolls: List<SavedDiceRoll> = emptyList(),
    val isLoading: Boolean = true,
    val isClearing: Boolean = false,
    val error: Throwable? = null,
)
