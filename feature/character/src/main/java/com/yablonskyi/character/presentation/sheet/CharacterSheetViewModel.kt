package com.yablonskyi.character.presentation.sheet

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.yablonskyi.character.navigation.CharacterSheetRoute
import com.yablonskyi.character.presentation.common.CharacterLoadStatus
import com.yablonskyi.character.presentation.common.CharacterTransitionCache
import com.yablonskyi.character.presentation.common.CharacterUiError
import com.yablonskyi.character.presentation.sheet.mapper.*
import com.yablonskyi.character.presentation.sheet.model.*
import com.yablonskyi.domain.repository.*
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
class CharacterSheetViewModel @Inject constructor(
    private val characters: CharacterRepository,
    private val spells: SpellRepository,
    private val attacks: AttackRepository,
    transitionCache: CharacterTransitionCache,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val id = savedStateHandle.toRoute<CharacterSheetRoute>().id
    private val initialCharacter = transitionCache.take(id)
    private val mutableState = MutableStateFlow(CharacterSheetState(
        character = initialCharacter,
        status = if (initialCharacter != null) {
            CharacterLoadStatus.CONTENT
        } else {
            CharacterLoadStatus.LOADING
        },
        leftSelectedTab = restoredTab("left", CharacterTab.ABILITIES),
        rightSelectedTab = restoredTab("right", CharacterTab.SPELLS),
        lessDetails = savedStateHandle["lessDetails"] ?: false,
        currentFilter = restoreSpellFilter(savedStateHandle["filter"]),
        editor = restoreEditor(savedStateHandle["editor"]),
    ))
    val state = mutableState.asStateFlow()
    private val effectChannel = Channel<CharacterSheetEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()
    private val writes = Channel<CharacterSheetIntent>(Channel.UNLIMITED)
    private val observations = mutableListOf<Job>()
    private val retryWrite = Channel<Boolean>(Channel.CONFLATED)
    private var waitingForRetry = false

    init {
        observe()
        viewModelScope.launch {
            for (intent in writes) {
                try {
                    var accepted = false
                    var retry: Boolean
                    do {
                        retry = false
                        try {
                    when (intent) {
                        is CharacterSheetIntent.Change -> characters.applyChange(id, intent.change)
                        is CharacterSheetIntent.AttackSaved -> {
                            val attack = intent.attack.copy(characterId = id)
                            if (attack.attackId == 0L) attacks.insertAttack(attack) else attacks.updateAttack(attack)
                        }
                        is CharacterSheetIntent.AttackDeleted -> attacks.deleteAttack(intent.attack.copy(characterId = id))
                        else -> Unit
                    }
                            accepted = true
                        } catch (cancelled: CancellationException) { throw cancelled }
                        catch (_: Exception) {
                            waitingForRetry = true
                            reduce(CharacterSheetMutation.Failed(CharacterUiError.SAVE))
                            retry = retryWrite.receive()
                            waitingForRetry = false
                        }
                    } while (retry)
                    if (intent is CharacterSheetIntent.Change && intent.formWrite != null) {
                        val persisted = try { characters.getCharacterById(id).first() }
                            catch (cancelled: CancellationException) { throw cancelled }
                            catch (_: Exception) { state.value.character }
                        mutableState.update { it.copy(formWriteResult = com.yablonskyi.character.presentation.common.FormWriteResult(intent.formWrite, intent.change, accepted, persisted)) }
                    }
                } finally { reduce(CharacterSheetMutation.WriteCount(-1)) }
            }
        }
    }

    fun onIntent(intent: CharacterSheetIntent) {
        when (intent) {
            is CharacterSheetIntent.Change, is CharacterSheetIntent.AttackSaved, is CharacterSheetIntent.AttackDeleted -> {
                if (state.value.character == null) return
                reduce(CharacterSheetMutation.WriteCount(1))
                writes.trySend(intent)
            }
            is CharacterSheetIntent.FilterChanged -> {
                savedStateHandle["filter"] = intent.filter.savedValue()
                reduce(CharacterSheetMutation.Filter(intent.filter))
            }
            is CharacterSheetIntent.LeftTabSelected -> { reduce(CharacterSheetMutation.LeftTab(intent.tab)); saveTabs() }
            is CharacterSheetIntent.RightTabSelected -> { reduce(CharacterSheetMutation.RightTab(intent.tab)); saveTabs() }
            is CharacterSheetIntent.EditorChanged -> {
                reduce(CharacterSheetMutation.Editor(intent.editor))
                savedStateHandle["editor"] = saveEditor(intent.editor)
            }
            CharacterSheetIntent.ToggleDetails -> {
                reduce(CharacterSheetMutation.ToggleDetails)
                savedStateHandle["lessDetails"] = state.value.lessDetails
            }
            CharacterSheetIntent.OpenSettings -> emit(CharacterSheetEffect.OpenSettings(id))
            CharacterSheetIntent.ManageSpells -> emit(CharacterSheetEffect.ManageSpells(id))
            CharacterSheetIntent.OpenDiceHistory -> emit(CharacterSheetEffect.OpenDiceHistory(id))
            CharacterSheetIntent.BackClicked -> emit(CharacterSheetEffect.Back)
            CharacterSheetIntent.DismissError -> {
                reduce(CharacterSheetMutation.ClearErrors)
                if (waitingForRetry) retryWrite.trySend(false)
            }
            CharacterSheetIntent.Retry -> {
                reduce(CharacterSheetMutation.ClearErrors)
                observe()
                if (waitingForRetry) retryWrite.trySend(true)
            }
        }
    }

    private fun observe() {
        observations.forEach { it.cancel() }
        observations.clear()
        observations += viewModelScope.launch {
            try { characters.getCharacterById(id).collect { reduce(CharacterSheetMutation.CharacterLoaded(it)) } }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { reduce(CharacterSheetMutation.Failed(CharacterUiError.LOAD)) }
        }
        observations += viewModelScope.launch {
            try { spells.getCharacterSpells(id).collect { reduce(CharacterSheetMutation.SpellsLoaded(it)) } }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { reduce(CharacterSheetMutation.Failed(CharacterUiError.SPELLS)) }
        }
        observations += viewModelScope.launch {
            try { attacks.getAttacksForCharacter(id).collect { reduce(CharacterSheetMutation.AttacksLoaded(it)) } }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { reduce(CharacterSheetMutation.Failed(CharacterUiError.ATTACKS)) }
        }
    }

    private fun saveEditor(editor: CharacterSheetEditor?): String? = when (editor) {
        null -> null
        CharacterSheetEditor.EditHealth -> "health"
        is CharacterSheetEditor.EditAbility -> "ability:${editor.ability.name}"
        is CharacterSheetEditor.EditAttack -> "attack:${editor.attackId}"
        is CharacterSheetEditor.ViewSpell -> "spell:${editor.spellId}"
    }

    private fun restoreEditor(value: String?): CharacterSheetEditor? = when {
        value == "health" -> CharacterSheetEditor.EditHealth
        value?.startsWith("ability:") == true -> com.yablonskyi.model.character.Ability.entries
            .firstOrNull { it.name == value.substringAfter(':') }?.let { CharacterSheetEditor.EditAbility(it) }
        value?.startsWith("attack:") == true -> value.substringAfter(':').toLongOrNull()?.let { CharacterSheetEditor.EditAttack(it) }
        value?.startsWith("spell:") == true -> value.substringAfter(':').toLongOrNull()?.let { CharacterSheetEditor.ViewSpell(it) }
        else -> null
    }

    private fun restoredTab(key: String, fallback: CharacterTab) = CharacterTab.entries.firstOrNull { it.name == savedStateHandle.get<String>(key) } ?: fallback
    private fun saveTabs() { savedStateHandle["left"] = state.value.leftSelectedTab.name; savedStateHandle["right"] = state.value.rightSelectedTab.name }
    private fun emit(effect: CharacterSheetEffect) { viewModelScope.launch { effectChannel.send(effect) } }
    private fun reduce(mutation: CharacterSheetMutation) { mutableState.update { reduceCharacterSheet(it, mutation) } }
}