package com.yablonskyi.character.presentation.notes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.yablonskyi.character.navigation.NoteEditorRoute
import com.yablonskyi.character.presentation.notes.editor.RichTextEditor
import com.yablonskyi.domain.backup.BackupAccessGate
import com.yablonskyi.domain.backup.BackupAccessState
import com.yablonskyi.domain.backup.BackupRecoveryRequiredException
import com.yablonskyi.domain.character.CharacterChange
import com.yablonskyi.domain.repository.CharacterRepository
import com.yablonskyi.model.character.Character
import com.yablonskyi.model.character.Note
import com.yablonskyi.model.character.RichText
import dagger.hilt.android.lifecycle.HiltViewModel
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class NoteEditorStatus { LOADING, READY, MISSING_CHARACTER, MISSING_NOTE, ERROR }
enum class NoteEditorError { LOAD, DRAFT, SAVE, CONFLICT, TOPIC_REQUIRED, STALE_CHARACTER }
enum class NoteEditorEffect { BACK }

data class NoteEditorState(
    val status: NoteEditorStatus = NoteEditorStatus.LOADING,
    val draft: NoteDraft? = null,
    val saving: Boolean = false,
    val error: NoteEditorError? = null,
    val confirmDiscard: Boolean = false,
    val confirmDelete: Boolean = false,
) {
    val dirty: Boolean get() = draft?.let {
        if (it.original == null) it.current.topic.isNotEmpty() || it.current.text != RichText()
        else it.current != it.original
    } ?: false
}

@HiltViewModel
class NoteEditorViewModel @Inject constructor(
    private val characters: CharacterRepository,
    private val drafts: NoteDraftStorage,
    private val gate: BackupAccessGate,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val route = savedStateHandle.toRoute<NoteEditorRoute>()
    private val sessionId: String = savedStateHandle["noteDraftSession"]
        ?: UUID.randomUUID().toString().also { savedStateHandle["noteDraftSession"] = it }
    private val noteId: String = route.noteId ?: (savedStateHandle["newNoteId"]
        ?: UUID.randomUUID().toString().also { savedStateHandle["newNoteId"] = it })
    private val mutableState = MutableStateFlow(NoteEditorState())
    val state = mutableState.asStateFlow()
    private val effectChannel = Channel<NoteEditorEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()
    private var persistJob: Job? = null
    private var flushJob: Job? = null
    private var operationJob: Job? = null
    private var loaded = false
    private var sawReadyGate = false
    private var restoreStarted = false

    init {
        viewModelScope.launch {
            gate.state.collect { access ->
                if (access == BackupAccessState.READY) sawReadyGate = true
                else if (sawReadyGate) {
                    restoreStarted = true
                    operationJob?.cancel()
                    mutableState.update { it.copy(status = NoteEditorStatus.ERROR, error = NoteEditorError.STALE_CHARACTER) }
                }
            }
        }
        viewModelScope.launch {
            try {
                drafts.cleanupOrphans()
                characters.getCharacterById(route.characterId).collect { character ->
                    if (!loaded) initialize(character) else revalidate(character)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableState.update { it.copy(status = NoteEditorStatus.ERROR, error = NoteEditorError.LOAD) }
            }
        }
    }

    private suspend fun initialize(character: Character?) {
        if (character == null) {
            mutableState.update { it.copy(status = NoteEditorStatus.MISSING_CHARACTER) }
            return
        }
        val existing = character.notes.find { it.id == noteId }
        if (route.noteId != null && existing == null) {
            mutableState.update { it.copy(status = NoteEditorStatus.MISSING_NOTE) }
            return
        }
        val stored = try {
            drafts.read(sessionId, route.characterId, noteId)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            mutableState.update { it.copy(status = NoteEditorStatus.ERROR, error = NoteEditorError.DRAFT) }
            return
        }
        val fingerprint = character.fingerprint()
        if (stored != null && stored.characterFingerprint != fingerprint) {
            mutableState.update { it.copy(status = NoteEditorStatus.ERROR, draft = stored, error = NoteEditorError.STALE_CHARACTER) }
            return
        }
        if (stored != null && stored.current != stored.original && existing == stored.current) {
            drafts.delete(sessionId, route.characterId, noteId)
            loaded = true
            effectChannel.send(NoteEditorEffect.BACK)
            return
        }
        if (route.noteId == null && stored == null && existing != null) {
            loaded = true
            effectChannel.send(NoteEditorEffect.BACK)
            return
        }
        val draft = stored ?: NoteDraft(
            sessionId = sessionId,
            characterId = route.characterId,
            noteId = noteId,
            characterFingerprint = fingerprint,
            original = existing,
            current = existing ?: Note(noteId, ""),
        )
        loaded = true
        mutableState.update { it.copy(
            status = NoteEditorStatus.READY,
            draft = draft,
            error = if (stored?.original != null && existing != stored.original) NoteEditorError.CONFLICT else null,
        ) }
    }

    private fun revalidate(character: Character?) {
        val draft = state.value.draft ?: return
        when {
            character == null -> mutableState.update { it.copy(status = NoteEditorStatus.MISSING_CHARACTER) }
            restoreStarted || character.fingerprint() != draft.characterFingerprint ->
                mutableState.update { it.copy(status = NoteEditorStatus.ERROR, error = NoteEditorError.STALE_CHARACTER) }
            route.noteId != null && character.notes.none { it.id == noteId } ->
                mutableState.update { it.copy(status = NoteEditorStatus.MISSING_NOTE) }
            route.noteId != null && character.notes.first { it.id == noteId } != draft.original ->
                mutableState.update { it.copy(error = NoteEditorError.CONFLICT) }
        }
    }

    fun topicChanged(topic: String) = edit { it.copy(current = it.current.copy(topic = topic)) }

    fun bodyChanged(editor: RichTextEditor) = edit {
        it.copy(
            current = it.current.copy(text = editor.content),
            selectionStart = editor.selectionStart,
            selectionEnd = editor.selectionEnd,
            typingFormats = editor.typingFormats,
        )
    }

    private fun edit(change: (NoteDraft) -> NoteDraft) {
        if (state.value.status != NoteEditorStatus.READY || state.value.saving) return
        mutableState.update { current ->
            current.copy(draft = current.draft?.let(change), error = null)
        }
        persistJob?.cancel()
        val snapshot = state.value.draft ?: return
        persistJob = viewModelScope.launch {
            delay(250)
            try {
                drafts.write(snapshot)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableState.update { it.copy(error = NoteEditorError.DRAFT) }
            }
        }
    }

    fun flushDraft() {
        val snapshot = state.value.draft ?: return
        if (state.value.status != NoteEditorStatus.READY || state.value.saving) return
        flushJob?.cancel()
        flushJob = viewModelScope.launch {
            persistJob?.cancelAndJoin()
            try {
                drafts.write(snapshot)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableState.update { it.copy(error = NoteEditorError.DRAFT) }
            }
        }
    }

    fun save() {
        val snapshot = state.value.draft ?: return
        if (state.value.status != NoteEditorStatus.READY || state.value.saving) return
        if (snapshot.current.topic.isBlank()) {
            mutableState.update { it.copy(error = NoteEditorError.TOPIC_REQUIRED) }
            return
        }
        mutableState.update { it.copy(saving = true, error = null) }
        operationJob = viewModelScope.launch {
            try {
                persistJob?.cancelAndJoin()
                flushJob?.cancelAndJoin()
                drafts.write(snapshot)
                val change = snapshot.original?.let { CharacterChange.UpdateNote(it, snapshot.current) }
                    ?: CharacterChange.AddNote(snapshot.current)
                characters.applyChange(route.characterId, change)
                drafts.delete(sessionId, route.characterId, noteId)
                mutableState.update { it.copy(draft = null) }
                effectChannel.send(NoteEditorEffect.BACK)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                mutableState.update { it.copy(error = when (failure) {
                    is BackupRecoveryRequiredException -> NoteEditorError.STALE_CHARACTER
                    is IllegalStateException -> NoteEditorError.CONFLICT
                    else -> NoteEditorError.SAVE
                }) }
            } finally {
                mutableState.update { it.copy(saving = false) }
            }
        }
    }

    fun backRequested() {
        if (state.value.saving) return
        if (state.value.dirty) mutableState.update { it.copy(confirmDiscard = true) }
        else viewModelScope.launch {
            try {
                persistJob?.cancelAndJoin()
                flushJob?.cancelAndJoin()
                drafts.delete(sessionId, route.characterId, noteId)
                mutableState.update { it.copy(draft = null) }
                effectChannel.send(NoteEditorEffect.BACK)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableState.update { it.copy(error = NoteEditorError.DRAFT) }
            }
        }
    }

    fun dismissDiscard() { mutableState.update { it.copy(confirmDiscard = false) } }
    fun discard() {
        viewModelScope.launch {
            try {
                persistJob?.cancelAndJoin()
                flushJob?.cancelAndJoin()
                drafts.delete(sessionId, route.characterId, noteId)
                mutableState.update { it.copy(draft = null, confirmDiscard = false) }
                effectChannel.send(NoteEditorEffect.BACK)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableState.update { it.copy(error = NoteEditorError.DRAFT) }
            }
        }
    }

    fun deleteRequested() {
        if (route.noteId != null && state.value.status == NoteEditorStatus.READY) {
            mutableState.update { it.copy(confirmDelete = true) }
        }
    }
    fun dismissDelete() { mutableState.update { it.copy(confirmDelete = false) } }
    fun delete() {
        if (route.noteId == null || state.value.status != NoteEditorStatus.READY || state.value.saving) return
        mutableState.update { it.copy(saving = true, confirmDelete = false) }
        operationJob = viewModelScope.launch {
            try {
                persistJob?.cancelAndJoin()
                flushJob?.cancelAndJoin()
                characters.applyChange(route.characterId, CharacterChange.DeleteNote(noteId))
                drafts.delete(sessionId, route.characterId, noteId)
                mutableState.update { it.copy(draft = null) }
                effectChannel.send(NoteEditorEffect.BACK)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableState.update { it.copy(error = NoteEditorError.SAVE) }
            } finally {
                mutableState.update { it.copy(saving = false) }
            }
        }
    }
}

private fun Character.fingerprint(): String {
    val identity = listOf(id.toString(), name, charClass, subClass, race, imagePath.orEmpty()).joinToString("\u0000")
    return MessageDigest.getInstance("SHA-256").digest(identity.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it.toInt() and 255) }
}
