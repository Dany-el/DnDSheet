package com.yablonskyi.character.presentation.list

import androidx.lifecycle.SavedStateHandle
import com.yablonskyi.character.testutil.*
import com.yablonskyi.domain.CharacterSheetHtmlRenderer
import com.yablonskyi.domain.RenderedCharacterSheet
import com.yablonskyi.model.character.Character
import com.yablonskyi.model.character.CharacterSheet
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CharacterListViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    private val repository = FakeCharacterRepository()
    private val files = FakeCharacterFileRepository()
    private fun viewModel() = CharacterListViewModel(repository,
        CharacterSheetHtmlRenderer { _, _ -> Result.success(RenderedCharacterSheet("html", "Hero")) }, files, SavedStateHandle())

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
