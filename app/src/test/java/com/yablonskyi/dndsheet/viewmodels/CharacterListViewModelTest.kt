package com.yablonskyi.dndsheet.viewmodels

import com.yablonskyi.dndsheet.data.model.character.Character
import com.yablonskyi.dndsheet.fake_repository.FakeCharacterRepository
import com.yablonskyi.dndsheet.ui.character.CharacterListViewModel
import junit.framework.TestCase
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CharacterListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeCharacterRepository
    private lateinit var viewModel: CharacterListViewModel

    private val char1 = Character(
        id = 1,
        name = "Aragorn",
        charClass = "Ranger",
        level = 5
    )
    private val char2 = Character(id = 2, name = "Gandalf", charClass = "Wizard", level = 20)
    private val char3 = Character(id = 3, name = "Frodo", charClass = "Rogue", level = 3)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeCharacterRepository()
        viewModel = CharacterListViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `початковий стан показує завантаження`(): Unit = runTest {
        assertTrue(viewModel.characterListState.value.isLoading)
    }

    @Test
    fun `список персонажів відображається після завантаження`() = runTest {
        val job = launch { viewModel.characterListState.collect {} }
        repository.setCharacters(listOf(char1, char2, char3))
        advanceUntilIdle()
        val state = viewModel.characterListState.value
        assertFalse(state.isLoading)
        assertEquals(3, state.characters.size)
        job.cancel()
    }

    @Test
    fun `createCharacter викликає insertCharacter у репозиторії`(): Unit = runTest {
        viewModel.createCharacter(char1)
        advanceUntilIdle()
        TestCase.assertEquals(1, repository.insertedCharacters.size)
        TestCase.assertEquals("Aragorn", repository.insertedCharacters.first().name)
    }

    @Test
    fun `createCharacter оновлює lastCreatedId`(): Unit = runTest {
        viewModel.createCharacter(char1)
        advanceUntilIdle()
        val id = viewModel.lastCreatedId.value
        assertTrue(id != null && id > 0)
    }

    @Test
    fun `deleteCharacter видаляє персонажа з репозиторію`(): Unit = runTest {
        repository.setCharacters(listOf(char1, char2))
        viewModel.deleteCharacter(char1)
        advanceUntilIdle()
        TestCase.assertEquals(1, repository.deletedCharacters.size)
        assertEquals(char1, repository.deletedCharacters.first())
    }

    @Test
    fun `toggleSelection вмикає режим виділення`(): Unit = runTest {
        assertFalse(viewModel.isSelectionMode.value)
        viewModel.toggleSelection(char1)
        assertTrue(viewModel.isSelectionMode.value)
    }

    @Test
    fun `toggleSelection додає персонажа до виділених`(): Unit = runTest {
        viewModel.toggleSelection(char1)
        assertTrue(viewModel.selectedCharacters.value.contains(char1))
    }

    @Test
    fun `toggleSelection двічі знімає виділення з персонажа`(): Unit = runTest {
        viewModel.toggleSelection(char1)
        viewModel.toggleSelection(char1)
        assertFalse(viewModel.selectedCharacters.value.contains(char1))
    }

    @Test
    fun `closeSelection скидає режим виділення та список виділених`(): Unit = runTest {
        viewModel.toggleSelection(char1)
        viewModel.toggleSelection(char2)
        viewModel.closeSelection()
        assertFalse(viewModel.isSelectionMode.value)
        assertTrue(viewModel.selectedCharacters.value.isEmpty())
    }

    @Test
    fun `deleteSelectedCharacters видаляє виділених персонажів та закриває режим`() = runTest {
        val job = launch { viewModel.characterListState.collect {} }
        repository.setCharacters(listOf(char1, char2, char3))
        advanceUntilIdle()
        viewModel.toggleSelection(char1)
        viewModel.toggleSelection(char3)
        viewModel.deleteSelectedCharacters()
        advanceUntilIdle()
        assertEquals(2, repository.deletedCharacters.size)
        assertFalse(viewModel.isSelectionMode.value)
        assertTrue(viewModel.selectedCharacters.value.isEmpty())
        job.cancel()
    }
}