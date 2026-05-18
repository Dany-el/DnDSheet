package com.yablonskyi.dndsheet

import com.yablonskyi.dndsheet.data.model.character.Character
import com.yablonskyi.dndsheet.fake_repository.FakeCharacterRepository
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CharacterRepositoryDelegationTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepo: FakeCharacterRepository

    private val testCharacter = Character(id = 1, name = "Legolas", level = 7)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeCharacterRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `insertCharacter зберігає персонажа та повертає id`() = runTest {
        val id = fakeRepo.insertCharacter(testCharacter)
        assertTrue(id > 0)
        assertEquals(1, fakeRepo.insertedCharacters.size)
    }

    @Test
    fun `getAllCharacters повертає актуальний список`() = runTest {
        fakeRepo.setCharacters(listOf(testCharacter))
        val characters = fakeRepo.getAllCharacters().first()
        assertEquals(1, characters.size)
        assertEquals("Legolas", characters.first().name)
    }

    @Test
    fun `getCharacterById повертає конкретного персонажа`() = runTest {
        fakeRepo.setCharacters(listOf(testCharacter))
        val result = fakeRepo.getCharacterById(1L).first()
        assertEquals("Legolas", result?.name)
    }

    @Test
    fun `getCharacterById повертає null для невідомого id`() = runTest {
        fakeRepo.setCharacters(listOf(testCharacter))
        val result = fakeRepo.getCharacterById(999L).first()
        assertEquals(null, result)
    }

    @Test
    fun `updateCharacter оновлює запис`() = runTest {
        fakeRepo.setCharacters(listOf(testCharacter))
        val updated = testCharacter.copy(name = "Legolas Greenleaf")
        fakeRepo.updateCharacter(updated)
        assertEquals(1, fakeRepo.updatedCharacters.size)
        assertEquals("Legolas Greenleaf", fakeRepo.updatedCharacters.first().name)
    }

    @Test
    fun `deleteCharacter видаляє персонажа`() = runTest {
        fakeRepo.setCharacters(listOf(testCharacter))
        fakeRepo.deleteCharacter(testCharacter)
        assertEquals(1, fakeRepo.deletedCharacters.size)
        assertTrue(fakeRepo.getAllCharacters().first().isEmpty())
    }

    @Test
    fun `deleteCharacters видаляє кількох персонажів одночасно`() = runTest {
        val char2 = Character(id = 2, name = "Gimli")
        fakeRepo.setCharacters(listOf(testCharacter, char2))
        fakeRepo.deleteCharacters(listOf(testCharacter, char2))
        assertEquals(2, fakeRepo.deletedCharacters.size)
        assertTrue(fakeRepo.getAllCharacters().first().isEmpty())
    }
}