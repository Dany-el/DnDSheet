package com.yablonskyi.character.platform.print

import com.yablonskyi.character.platform.print.CharacterPrintEffect
import com.yablonskyi.character.platform.print.CharacterPrintError
import com.yablonskyi.character.platform.print.CharacterPrintState
import com.yablonskyi.character.platform.print.normalizePrintLanguage

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


import kotlinx.coroutines.CoroutineScope

internal class CharacterPrintCoordinator(
    private val repository: CharacterRepository,
    private val htmlRenderer: CharacterSheetHtmlRenderer,
    private val scope: CoroutineScope,
) {
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
        scope.launch {
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
        scope.launch {
            effects.send(if (result.isSuccess) CharacterPrintEffect.PrintRequestAccepted
                else CharacterPrintEffect.Failed(CharacterPrintError.PRINT_LAUNCH_FAILED))
        }
    }

    fun onPrintLaunchCancelled(requestId: Long) { finishPrintRequest(requestId) }

    private fun finishPrintRequest(requestId: Long): Boolean {
        val current = _printState.value as? CharacterPrintState.Launching ?: return false
        return current.requestId == requestId && _printState.compareAndSet(current, CharacterPrintState.Idle)
    }

}
