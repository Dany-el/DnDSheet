package com.yablonskyi.character.platform.print

import com.yablonskyi.character.platform.print.CharacterPrintEffect
import com.yablonskyi.character.platform.print.CharacterPrintError
import com.yablonskyi.character.platform.print.CharacterPrintState
import com.yablonskyi.character.presentation.list.CharacterListViewModel

import com.yablonskyi.domain.CharacterSheetHtmlRenderer
import com.yablonskyi.domain.RenderedCharacterSheet
import com.yablonskyi.domain.repository.CharacterRepository
import com.yablonskyi.model.character.Character
import com.yablonskyi.model.character.CharacterSheet
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import androidx.lifecycle.SavedStateHandle
import com.yablonskyi.character.presentation.list.*
import com.yablonskyi.domain.repository.CharacterFileRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CharacterPrintTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeCharacterRepository()
    private val renderer = FakeRenderer()
    private lateinit var viewModel: CharacterListViewModel

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
        viewModel = CharacterListViewModel(repository, renderer, FakeFiles(), SavedStateHandle())
    }

    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun givenRegionalLocale_whenPrintRequested_thenCapturesLanguageAndSheet() = runTest {
        viewModel.onIntent(CharacterListIntent.PrintClicked(7, "uk_UA"))
        advanceUntilIdle()
        assertTrue(viewModel.state.value.printState != CharacterPrintState.Idle)
        advanceUntilIdle()
        val effect = viewModel.effects.filterIsInstance<CharacterListEffect.Print>().map { it.effect }.first() as CharacterPrintEffect.LaunchPrint
        assertEquals(listOf(7L), repository.loadedIds)
        assertEquals(listOf("uk"), renderer.languages)
        assertEquals("<html>sheet</html>", effect.html)
        assertTrue(viewModel.claimPrintRequest(effect.requestId))
        assertFalse(viewModel.claimPrintRequest(effect.requestId))
        viewModel.onIntent(CharacterListIntent.PrintFinished(effect.requestId, Result.success(Unit)))
        advanceUntilIdle()
        assertEquals(CharacterPrintState.Idle, viewModel.state.value.printState)
        assertEquals(CharacterPrintEffect.PrintRequestAccepted, viewModel.effects.filterIsInstance<CharacterListEffect.Print>().map { it.effect }.first())
    }

    @Test fun givenBusyRequest_whenTappedAgain_thenIgnoresDuplicatesAndStaleResults() = runTest {
        renderer.gate = CompletableDeferred()
        viewModel.onIntent(CharacterListIntent.PrintClicked(7, "ru-RU"))
        viewModel.onIntent(CharacterListIntent.PrintClicked(8, "uk"))
        advanceUntilIdle()
        assertEquals(listOf(7L), repository.loadedIds)
        renderer.gate!!.complete(Unit)
        advanceUntilIdle()
        val effect = viewModel.effects.filterIsInstance<CharacterListEffect.Print>().map { it.effect }.first() as CharacterPrintEffect.LaunchPrint
        viewModel.onIntent(CharacterListIntent.PrintClicked(8, "en"))
        viewModel.onIntent(CharacterListIntent.PrintFinished(effect.requestId + 1, Result.success(Unit)))
        assertTrue(viewModel.state.value.printState is CharacterPrintState.Launching)
        viewModel.onIntent(CharacterListIntent.PrintFinished(effect.requestId, Result.failure(IllegalStateException())))
        assertEquals(CharacterPrintEffect.Failed(CharacterPrintError.PRINT_LAUNCH_FAILED), viewModel.effects.filterIsInstance<CharacterListEffect.Print>().map { it.effect }.first())
        viewModel.onIntent(CharacterListIntent.PrintClicked(8, "de-DE"))
        advanceUntilIdle()
        assertEquals(listOf("ru", "en"), renderer.languages)
    }

    @Test fun givenLoadFailure_whenRequested_thenReturnsToIdleWithLoadError() = runTest {
        repository.failure = IllegalStateException("Missing character")
        viewModel.onIntent(CharacterListIntent.PrintClicked(7, "en"))
        advanceUntilIdle()
        advanceUntilIdle()
        assertEquals(CharacterPrintState.Idle, viewModel.state.value.printState)
        assertEquals(CharacterPrintEffect.Failed(CharacterPrintError.LOAD_FAILED), viewModel.effects.filterIsInstance<CharacterListEffect.Print>().map { it.effect }.first())
        assertTrue(renderer.languages.isEmpty())
    }

    @Test fun givenRenderFailure_whenRequested_thenReturnsToIdleWithRenderError() = runTest {
        renderer.failure = IllegalStateException("Python failed")
        viewModel.onIntent(CharacterListIntent.PrintClicked(7, "en"))
        advanceUntilIdle()
        advanceUntilIdle()
        assertEquals(CharacterPrintState.Idle, viewModel.state.value.printState)
        assertEquals(CharacterPrintEffect.Failed(CharacterPrintError.RENDER_FAILED), viewModel.effects.filterIsInstance<CharacterListEffect.Print>().map { it.effect }.first())
    }

    @Test fun givenCancellation_whenRendering_thenAllowsRetryWithoutFailureEffect() = runTest {
        renderer.failure = CancellationException()
        viewModel.onIntent(CharacterListIntent.PrintClicked(7, "en"))
        advanceUntilIdle()
        advanceUntilIdle()
        assertEquals(CharacterPrintState.Idle, viewModel.state.value.printState)
        renderer.failure = null
        viewModel.onIntent(CharacterListIntent.PrintClicked(7, "system"))
        advanceUntilIdle()
        val effect = viewModel.effects.filterIsInstance<CharacterListEffect.Print>().map { it.effect }.first() as CharacterPrintEffect.LaunchPrint
        viewModel.onIntent(CharacterListIntent.PrintCancelled(effect.requestId))
        advanceUntilIdle()
        assertEquals(CharacterPrintState.Idle, viewModel.state.value.printState)
    }

    @Test fun givenUpdatedCharacter_whenSelected_thenSelectionFollowsId() = runTest {
        val original = Character(id = 1, name = "Hero")
        repository.characters.value = listOf(original)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }
        advanceUntilIdle()
        viewModel.onIntent(CharacterListIntent.SelectionToggled(original.id))
        repository.characters.value = listOf(original.copy(name = "Renamed"))
        advanceUntilIdle()
        viewModel.onIntent(CharacterListIntent.SelectionToggled(repository.characters.value.single().id))
        assertTrue(viewModel.state.value.selectedIds.isEmpty())
    }

    @Test fun givenHiddenSelection_whenVisibleCountMatches_thenNotAllVisibleAreSelected() = runTest {
        repository.characters.value = listOf(Character(id = 1, name = "Alpha"), Character(id = 2, name = "Beta"))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }
        advanceUntilIdle()
        viewModel.onIntent(CharacterListIntent.SelectionToggled(repository.characters.value.first().id))
        viewModel.onIntent(CharacterListIntent.SearchChanged("Beta"))
        advanceUntilIdle()
        assertFalse(viewModel.state.value.isAllSelected)
    }

    private class FakeFiles : CharacterFileRepository {
        override suspend fun read(uri: String) = emptyList<CharacterSheet>()
        override suspend fun import(sheets: List<CharacterSheet>) = Unit
        override suspend fun export(uri: String, characterIds: List<Long>) = Unit
    }

    private class FakeRenderer : CharacterSheetHtmlRenderer {
        val languages = mutableListOf<String>()
        var gate: CompletableDeferred<Unit>? = null
        var failure: Exception? = null
        override suspend fun render(sheet: CharacterSheet, languageCode: String): Result<RenderedCharacterSheet> {
            languages += languageCode
            gate?.await()
            return failure?.let { Result.failure(it) }
                ?: Result.success(RenderedCharacterSheet("<html>sheet</html>", sheet.character.name))
        }
    }

    private class FakeCharacterRepository : CharacterRepository {
        override suspend fun reorderCharacters(orderedIds: List<Long>) = error("Unused")
        val loadedIds = mutableListOf<Long>()
        var failure: Exception? = null
        override suspend fun getCharacterSheetById(characterId: Long): CharacterSheet {
            loadedIds += characterId
            failure?.let { throw it }
            return CharacterSheet(Character(id = characterId, name = "Hero"), emptyList(), emptyList())
        }
        val characters = MutableStateFlow(emptyList<Character>())
        override fun getAllCharacters() = characters
        override fun getCharacterById(id: Long) = flowOf<Character?>(null)
        override suspend fun getCharacterSheetsByIds(characterIds: List<Long>) = characterIds.map { getCharacterSheetById(it) }
        override suspend fun getAllCharacterSheets() = emptyList<CharacterSheet>()
        override suspend fun insertCharacter(character: Character) = character.id
        override suspend fun insertCharacters(sheets: List<CharacterSheet>) = Unit
        override suspend fun restoreCharacters(sheets: List<CharacterSheet>) = Unit
        override suspend fun applyChange(id: Long, change: com.yablonskyi.domain.character.CharacterChange): Character {
            val current = getCharacterSheetById(id).character
            val updated = com.yablonskyi.domain.character.applyCharacterChange(current, change)
            updateCharacter(updated)
            return updated
        }
        override suspend fun updateCharacter(character: Character) = Unit
        override suspend fun deleteCharacter(character: Character) = Unit
        override suspend fun deleteCharacters(characters: List<Character>) = Unit
    }
}
