package com.yablonskyi.character.presentation.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.yablonskyi.character.navigation.CharacterSettingsRoute
import com.yablonskyi.character.presentation.common.CharacterUiError
import com.yablonskyi.domain.character.*
import com.yablonskyi.domain.repository.CharacterRepository
import com.yablonskyi.domain.repository.CharacterImageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CharacterSettingsViewModel @Inject constructor(
    private val repository: CharacterRepository,
    private val images: CharacterImageRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val id = savedStateHandle.toRoute<CharacterSettingsRoute>().id
    private val mutableState = MutableStateFlow(CharacterSettingsState(drafts = CharacterTextField.entries.mapNotNull { field ->
        savedStateHandle.get<String>("draft:${field.name}")?.let { field to it }
    }.toMap()))
    val state = mutableState.asStateFlow()
    private val effectChannel = Channel<CharacterSettingsEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()
    private val writes = Channel<CharacterSettingsIntent>(Channel.UNLIMITED)
    private val retryWrite = Channel<Boolean>(Channel.CONFLATED)
    private var waitingForRetry = false
    private var observation: Job? = null

    init {
        observe()
        viewModelScope.launch {
            for (intent in writes) {
                try {
                    var retry: Boolean
                    do {
                        retry = false
                        try {
                            when (intent) {
                                is CharacterSettingsIntent.Change -> repository.applyChange(id, intent.change)
                                is CharacterSettingsIntent.ImageSelected -> intent.uri?.let { images.replace(id, it) }
                                else -> Unit
                            }
                        } catch (cancelled: CancellationException) { throw cancelled }
                        catch (_: Exception) {
                            waitingForRetry = true
                            reduce(CharacterSettingsMutation.Failed(if (intent is CharacterSettingsIntent.ImageSelected) CharacterUiError.IMAGE else CharacterUiError.SAVE))
                            retry = retryWrite.receive()
                            waitingForRetry = false
                            if (!retry && intent is CharacterSettingsIntent.Change && intent.change is CharacterChange.Text) {
                                val change = intent.change
                                if (state.value.drafts[change.field] == change.value) savedStateHandle.remove<String>("draft:${change.field.name}")
                                reduce(CharacterSettingsMutation.DiscardDraft(change))
                                // Reload the persisted value after the user cancels this failed change.
                                observe()
                            }
                        }
                    } while (retry)
                } finally {
                    reduce(CharacterSettingsMutation.WriteCount(-1))
                    if (intent is CharacterSettingsIntent.ImageSelected) reduce(CharacterSettingsMutation.SavingImage(false))
                }
            }
        }
    }

    fun onIntent(intent: CharacterSettingsIntent) {
        when (intent) {
            is CharacterSettingsIntent.Change -> {
                if (state.value.character == null) return
                if (intent.change is CharacterChange.Text) {
                    savedStateHandle["draft:${intent.change.field.name}"] = intent.change.value
                    reduce(CharacterSettingsMutation.Draft(intent.change))
                }
                enqueue(intent)
            }
            CharacterSettingsIntent.ImagePickerClicked -> if (!state.value.isPickingImage && !state.value.isSavingImage) {
                reduce(CharacterSettingsMutation.PickingImage(true))
                emit(CharacterSettingsEffect.LaunchImagePicker)
            }
            is CharacterSettingsIntent.ImageSelected -> {
                if (!state.value.isPickingImage) return
                reduce(CharacterSettingsMutation.PickingImage(false))
                if (intent.uri != null) {
                    reduce(CharacterSettingsMutation.SavingImage(true))
                    enqueue(intent)
                }
            }
            CharacterSettingsIntent.BackClicked -> emit(CharacterSettingsEffect.Back)
            CharacterSettingsIntent.Retry -> {
                reduce(CharacterSettingsMutation.ClearErrors)
                if (waitingForRetry) retryWrite.trySend(true)
                observe()
            }
            CharacterSettingsIntent.DismissError -> {
                reduce(CharacterSettingsMutation.ClearErrors)
                if (waitingForRetry) retryWrite.trySend(false)
            }
        }
    }

    private fun enqueue(intent: CharacterSettingsIntent) {
        reduce(CharacterSettingsMutation.WriteCount(1))
        writes.trySend(intent)
    }
    private fun observe() {
        observation?.cancel()
        observation = viewModelScope.launch {
            try { repository.getCharacterById(id).collect { reduce(CharacterSettingsMutation.Loaded(it)) } }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { reduce(CharacterSettingsMutation.Failed(CharacterUiError.LOAD)) }
        }
    }
    private fun emit(effect: CharacterSettingsEffect) { viewModelScope.launch { effectChannel.send(effect) } }
    private fun reduce(mutation: CharacterSettingsMutation) { mutableState.update { reduceCharacterSettings(it, mutation) } }
}
