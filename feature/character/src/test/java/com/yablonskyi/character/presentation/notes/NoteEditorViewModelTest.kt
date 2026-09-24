package com.yablonskyi.character.presentation.notes

import androidx.lifecycle.SavedStateHandle
import com.yablonskyi.character.testutil.FakeCharacterRepository
import com.yablonskyi.character.testutil.MainDispatcherRule
import com.yablonskyi.domain.backup.BackupAccessGate
import com.yablonskyi.model.character.Note
import com.yablonskyi.model.character.RichText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NoteEditorViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    private val repository = FakeCharacterRepository()
    private val drafts = MemoryDraftStorage()
    private val gate = BackupAccessGate()
    private val noteId = "169d2bf0-329b-454e-b801-a1ea5f7f4f30"

    private fun handle(noteId: String? = null) = SavedStateHandle(
        if (noteId == null) mapOf("characterId" to 7L) else mapOf("characterId" to 7L, "noteId" to noteId),
    )

    @Test fun givenNewNote_whenSaved_thenAddsOnceAndRemovesDraft() = runTest {
        gate.recover { }
        val vm = NoteEditorViewModel(repository, drafts, gate, handle())
        advanceUntilIdle()
        assertEquals(NoteEditorStatus.READY, vm.state.value.status)
        vm.topicChanged("Quest")
        advanceUntilIdle()
        vm.save()
        advanceUntilIdle()
        assertEquals("Quest", repository.characters.value.single().notes.single().topic)
        assertTrue(drafts.values.isEmpty())
        assertEquals(NoteEditorEffect.BACK, vm.effects.first())
    }

    @Test fun givenFailedSave_whenRetried_thenUsesSameIdAndKeepsDraft() = runTest {
        gate.recover { }
        val vm = NoteEditorViewModel(repository, drafts, gate, handle())
        advanceUntilIdle()
        vm.topicChanged("Quest")
        repository.failure = IllegalArgumentException("temporary")
        vm.save()
        advanceUntilIdle()
        assertEquals(NoteEditorError.SAVE, vm.state.value.error)
        assertEquals(1, drafts.values.size)
        val id = vm.state.value.draft!!.noteId
        repository.failure = null
        vm.save()
        advanceUntilIdle()
        assertEquals(id, repository.characters.value.single().notes.single().id)
        assertTrue(drafts.values.isEmpty())
    }

    @Test fun givenRecreation_whenDraftExists_thenRestoresUnsavedTopicAndId() = runTest {
        gate.recover { }
        val handle = handle()
        val first = NoteEditorViewModel(repository, drafts, gate, handle)
        advanceUntilIdle()
        first.topicChanged("Session notes")
        first.flushDraft()
        advanceUntilIdle()
        val restored = NoteEditorViewModel(repository, drafts, gate, handle)
        advanceUntilIdle()
        assertEquals("Session notes", restored.state.value.draft?.current?.topic)
        assertEquals(first.state.value.draft?.noteId, restored.state.value.draft?.noteId)
    }

    @Test fun givenCommitBeforeDraftCleanup_whenReopened_thenDoesNotAddDuplicate() = runTest {
        gate.recover { }
        val handle = handle()
        val first = NoteEditorViewModel(repository, drafts, gate, handle)
        advanceUntilIdle()
        first.topicChanged("Committed")
        advanceUntilIdle()
        val draft = drafts.values.values.single()
        repository.characters.value = listOf(repository.characters.value.single().copy(notes = listOf(draft.current)))
        val reopened = NoteEditorViewModel(repository, drafts, gate, handle)
        advanceUntilIdle()
        assertEquals(NoteEditorEffect.BACK, reopened.effects.first())
        assertEquals(listOf(draft.current), repository.characters.value.single().notes)
        assertTrue(drafts.values.isEmpty())
    }

    @Test fun givenDirtyEditor_whenBackRequested_thenRequiresDiscard() = runTest {
        gate.recover { }
        val vm = NoteEditorViewModel(repository, drafts, gate, handle())
        advanceUntilIdle()
        vm.topicChanged("Draft")
        advanceUntilIdle()
        vm.backRequested()
        assertTrue(vm.state.value.confirmDiscard)
        assertNotNull(drafts.values.values.singleOrNull())
        vm.discard()
        advanceUntilIdle()
        assertTrue(drafts.values.isEmpty())
        assertEquals(NoteEditorEffect.BACK, vm.effects.first())
    }

    @Test fun givenExistingNoteChangedElsewhere_whenSaved_thenReportsConflictAndKeepsDraft() = runTest {
        gate.recover { }
        repository.characters.value = listOf(repository.characters.value.single().copy(notes = listOf(Note(noteId, "Old"))))
        val vm = NoteEditorViewModel(repository, drafts, gate, handle(noteId))
        advanceUntilIdle()
        vm.topicChanged("Mine")
        repository.characters.value = listOf(repository.characters.value.single().copy(notes = listOf(Note(noteId, "Theirs"))))
        advanceUntilIdle()
        assertEquals(NoteEditorError.CONFLICT, vm.state.value.error)
        vm.save()
        advanceUntilIdle()
        assertEquals("Theirs", repository.characters.value.single().notes.single().topic)
        assertEquals(NoteEditorError.CONFLICT, vm.state.value.error)
        assertFalse(drafts.values.isEmpty())
    }

    @Test fun givenExistingNote_whenDeleted_thenRemovesNoteAndDraft() = runTest {
        gate.recover { }
        repository.characters.value = listOf(repository.characters.value.single().copy(notes = listOf(Note(noteId, "Old", RichText()))))
        val vm = NoteEditorViewModel(repository, drafts, gate, handle(noteId))
        advanceUntilIdle()
        vm.deleteRequested()
        assertTrue(vm.state.value.confirmDelete)
        vm.delete()
        advanceUntilIdle()
        assertTrue(repository.characters.value.single().notes.isEmpty())
        assertTrue(drafts.values.isEmpty())
    }

    @Test fun givenNoteRemovedWhileOpen_whenSaving_thenDoesNotRecreateIt() = runTest {
        gate.recover { }
        repository.characters.value = listOf(repository.characters.value.single().copy(notes = listOf(Note(noteId, "Old"))))
        val vm = NoteEditorViewModel(repository, drafts, gate, handle(noteId))
        advanceUntilIdle()
        vm.topicChanged("Mine")
        repository.characters.value = listOf(repository.characters.value.single().copy(notes = emptyList()))
        advanceUntilIdle()
        assertEquals(NoteEditorStatus.MISSING_NOTE, vm.state.value.status)
        vm.save()
        advanceUntilIdle()
        assertTrue(repository.characters.value.single().notes.isEmpty())
    }

    @Test fun givenRestoreStartedWhileOpen_whenSaving_thenDraftIsBlocked() = runTest {
        gate.recover { }
        val vm = NoteEditorViewModel(repository, drafts, gate, handle())
        advanceUntilIdle()
        vm.topicChanged("Old session")
        gate.recover { advanceUntilIdle() }
        advanceUntilIdle()
        assertEquals(NoteEditorStatus.ERROR, vm.state.value.status)
        assertEquals(NoteEditorError.STALE_CHARACTER, vm.state.value.error)
        vm.save()
        advanceUntilIdle()
        assertTrue(repository.characters.value.single().notes.isEmpty())
    }

    private class MemoryDraftStorage : NoteDraftStorage {
        val values = mutableMapOf<String, NoteDraft>()
        override suspend fun read(sessionId: String, characterId: Long, noteId: String) = values[sessionId]
        override suspend fun write(draft: NoteDraft) { draft.validate(); values[draft.sessionId] = draft }
        override suspend fun delete(sessionId: String, characterId: Long, noteId: String) { values.remove(sessionId) }
        override suspend fun cleanupOrphans() = Unit
    }
}
