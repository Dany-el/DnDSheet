package com.yablonskyi.character.presentation.list

import androidx.lifecycle.SavedStateHandle
import com.yablonskyi.character.presentation.common.CharacterTransitionCache
import com.yablonskyi.character.testutil.FakeCharacterFileRepository
import com.yablonskyi.character.testutil.FakeCharacterRepository
import com.yablonskyi.character.testutil.MainDispatcherRule
import com.yablonskyi.domain.RenderedCharacterSheet
import com.yablonskyi.model.character.Character
import com.yablonskyi.model.character.CharacterSheet
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CharacterListViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    private val repository = FakeCharacterRepository()
    private val files = FakeCharacterFileRepository()
    private fun viewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()) = CharacterListViewModel(repository,
        { _, _ -> Result.success(RenderedCharacterSheet("html", "Hero")) }, files, CharacterTransitionCache(), savedStateHandle)

    @Test fun givenFilteredCharacters_whenMoved_thenPreservesHiddenPositionsAndSelection() = runTest {
        repository.characters.value = listOf(
            Character(id = 7, name = "Hero A"),
            Character(id = 8, name = "Other"),
            Character(id = 9, name = "Hero B"),
        )
        val vm = viewModel()
        advanceUntilIdle()
        vm.onIntent(CharacterListIntent.SelectionToggled(7))
        vm.onIntent(CharacterListIntent.SearchChanged("Hero"))
        vm.onIntent(CharacterListIntent.MoveCharacter(7, 9))

        assertEquals(listOf(9L, 7L), vm.state.value.characters.map { it.id })
        assertEquals(setOf(7L), vm.state.value.selectedIds)
        vm.onIntent(CharacterListIntent.SearchChanged(""))
        assertEquals(listOf(9L, 8L, 7L), vm.state.value.characters.map { it.id })

        repository.characters.value = repository.characters.value.map { it.copy(level = 2) }
        advanceUntilIdle()
        assertEquals(listOf(9L, 8L, 7L), vm.state.value.characters.map { it.id })
    }

    @Test fun givenSavedOrder_whenViewModelRecreated_thenRestoresOrderAndIgnoresInvalidMoves() = runTest {
        repository.characters.value += Character(id = 8, name = "Other")
        val vm = viewModel()
        advanceUntilIdle()
        vm.moveCharacter(7, 8)
        vm.onIntent(CharacterListIntent.ReorderFinished)
        advanceUntilIdle()
        val restored = viewModel()
        advanceUntilIdle()

        assertEquals(listOf(8L, 7L), restored.state.value.characters.map { it.id })
        restored.moveCharacter(99, 7)
        restored.moveCharacter(8, 8)
        assertEquals(listOf(8L, 7L), restored.state.value.characters.map { it.id })
    }

    @Test fun givenCharacters_whenEnteringSelectionMode_thenCanSelectExportAndExit() = runTest {
        val vm = viewModel()
        advanceUntilIdle()

        vm.onIntent(CharacterListIntent.EnterSelectionMode)
        assertTrue(vm.state.value.isSelectionMode)
        assertTrue(vm.state.value.selectedIds.isEmpty())

        vm.onIntent(CharacterListIntent.SelectionToggled(7))
        vm.onIntent(CharacterListIntent.ExportClicked)
        assertEquals(CharacterListEffect.LaunchExport, vm.effects.first())
        vm.onIntent(CharacterListIntent.ExportDocumentSelected("content://export"))
        advanceUntilIdle()
        assertEquals(listOf(7L), files.exportedIds)

        vm.onIntent(CharacterListIntent.ClearSelection)
        assertFalse(vm.state.value.isSelectionMode)
        assertTrue(vm.state.value.selectedIds.isEmpty())
    }

    @Test fun givenCreateClicked_whenHandled_thenOpensCharacterCreation() = runTest {
        val vm = viewModel()
        advanceUntilIdle()
        vm.onIntent(CharacterListIntent.CreateClicked)
        assertEquals(CharacterListEffect.CreateCharacter, vm.effects.first())
    }

    @Test fun givenSelectionChangedDuringPicker_whenExported_thenUsesOriginalIds() = runTest {
        repository.characters.value += Character(id = 8, name = "Other")
        val vm = viewModel(); advanceUntilIdle()
        vm.onIntent(CharacterListIntent.SelectionToggled(7))
        vm.onIntent(CharacterListIntent.ExportClicked)
        assertEquals(CharacterListEffect.LaunchExport, vm.effects.first())
        vm.onIntent(CharacterListIntent.ClearSelection)
        vm.onIntent(CharacterListIntent.SelectionToggled(8))
        vm.onIntent(CharacterListIntent.ExportDocumentSelected("content://export")); advanceUntilIdle()
        assertEquals(listOf(7L), files.exportedIds)
    }

    @Test fun givenImportConfirmed_whenPersistencePending_thenShowsBusyUntilSuccess() = runTest {
        files.input = listOf(CharacterSheet(Character(id = 9), emptyList(), emptyList()))
        files.gate = CompletableDeferred()
        val vm = viewModel(); advanceUntilIdle()
        vm.onIntent(CharacterListIntent.ImportClicked)
        assertEquals(CharacterListEffect.LaunchImport, vm.effects.first())
        vm.onIntent(CharacterListIntent.ImportDocumentSelected("content://import")); advanceUntilIdle()
        assertNotNull(vm.state.value.pendingImport)
        vm.onIntent(CharacterListIntent.ImportConfirmed); advanceUntilIdle()
        assertEquals(CharacterListOperation.IMPORT, vm.state.value.operation)
        assertTrue(files.imported.isEmpty())
        files.gate!!.complete(Unit); advanceUntilIdle()
        assertNull(vm.state.value.operation)
        assertNull(vm.state.value.pendingImport)
        assertEquals(CharacterListEffect.ImportSucceeded, vm.effects.first())
    }

    @Test fun givenImportFailure_whenConfirmed_thenRetainsConfirmationForRetry() = runTest {
        files.input = listOf(CharacterSheet(Character(id = 9), emptyList(), emptyList()))
        files.failure = IllegalStateException()
        val vm = viewModel(); advanceUntilIdle()
        vm.onIntent(CharacterListIntent.ImportClicked); vm.effects.first()
        vm.onIntent(CharacterListIntent.ImportDocumentSelected("content://import")); advanceUntilIdle()
        vm.onIntent(CharacterListIntent.ImportConfirmed); advanceUntilIdle()
        assertEquals(CharacterListError.IMPORT, vm.state.value.error)
        assertNotNull(vm.state.value.pendingImport)
        assertTrue(files.imported.isEmpty())
        files.failure = null
        vm.onIntent(CharacterListIntent.Retry); advanceUntilIdle()
        assertEquals(CharacterListEffect.ImportSucceeded, vm.effects.first())
    }

    @Test fun givenHiddenSelection_whenSelectAllToggled_thenPreservesHiddenIds() = runTest {
        repository.characters.value = listOf(Character(id = 7, name = "Alpha"), Character(id = 8, name = "Beta"))
        val vm = viewModel(); advanceUntilIdle()
        vm.onIntent(CharacterListIntent.SelectionToggled(7))
        vm.onIntent(CharacterListIntent.SearchChanged("Beta"))
        vm.onIntent(CharacterListIntent.SelectAllClicked)
        assertEquals(setOf(7L, 8L), vm.state.value.selectedIds)
        vm.onIntent(CharacterListIntent.SelectAllClicked)
        assertEquals(setOf(7L), vm.state.value.selectedIds)
        repository.characters.value = emptyList(); advanceUntilIdle()
        assertTrue(vm.state.value.selectedIds.isEmpty())
    }
}