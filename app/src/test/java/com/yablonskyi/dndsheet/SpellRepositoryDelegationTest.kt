package com.yablonskyi.dndsheet

import com.yablonskyi.dndsheet.data.model.character.CharacterSpellCrossRef
import com.yablonskyi.dndsheet.data.model.character.MagicSchool
import com.yablonskyi.dndsheet.data.model.character.Spell
import com.yablonskyi.dndsheet.data.model.character.SpellLevel
import com.yablonskyi.dndsheet.fake_repository.FakeSpellRepository
import junit.framework.TestCase.assertEquals
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
class SpellRepositoryDelegationTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepo: FakeSpellRepository

    private val fireball = Spell(
        spellId = 1,
        name = "Fireball",
        level = SpellLevel.LEVEL_3,
        school = MagicSchool.EVOCATION
    )
    private val magicMissile = Spell(
        spellId = 2,
        name = "Magic Missile",
        level = SpellLevel.LEVEL_1,
        school = MagicSchool.EVOCATION
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeSpellRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getAllSpellsInLibrary повертає всі заклинання`() = runTest {
        fakeRepo.setLibrarySpells(listOf(fireball, magicMissile))
        val spells = fakeRepo.getAllSpellsInLibrary().first()
        assertEquals(2, spells.size)
    }

    @Test
    fun `getCharacterSpells повертає заклинання персонажа`() = runTest {
        fakeRepo.setCharacterSpells(listOf(fireball))
        val spells = fakeRepo.getCharacterSpells(charId = 1L).first()
        assertEquals(1, spells.size)
        assertEquals("Fireball", spells.first().name)
    }

    @Test
    fun `assignSpellToCharacter зберігає crossRef`() = runTest {
        val crossRef = CharacterSpellCrossRef(characterId = 1L, spellId = 1L)
        fakeRepo.assignSpellToCharacter(crossRef)
        assertEquals(1, fakeRepo.assignedCrossRefs.size)
        assertEquals(crossRef, fakeRepo.assignedCrossRefs.first())
    }

    @Test
    fun `removeSpellFromCharacter зберігає пару ids`() = runTest {
        fakeRepo.removeSpellFromCharacter(charId = 1L, spellId = 1L)
        assertEquals(1, fakeRepo.removedPairs.size)
        assertEquals(1L to 1L, fakeRepo.removedPairs.first())
    }

    @Test
    fun `deleteSpell видаляє заклинання з бібліотеки`() = runTest {
        fakeRepo.setLibrarySpells(listOf(fireball, magicMissile))
        fakeRepo.deleteSpell(fireball)
        val spells = fakeRepo.getAllSpellsInLibrary().first()
        assertEquals(1, spells.size)
        assertEquals("Magic Missile", spells.first().name)
    }
}