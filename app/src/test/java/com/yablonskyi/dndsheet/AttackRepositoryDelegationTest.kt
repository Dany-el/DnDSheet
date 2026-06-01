package com.yablonskyi.dndsheet

import com.yablonskyi.dndsheet.data.model.character.Ability
import com.yablonskyi.dndsheet.data.model.character.Attack
import com.yablonskyi.dndsheet.fake_repository.FakeAttackRepository
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
class AttackRepositoryDelegationTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepo: FakeAttackRepository

    private val attack =
        Attack(attackId = 1, characterId = 10, name = "Greataxe", ability = Ability.STR)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeAttackRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `insertAttack зберігає атаку та повертає id`() = runTest {
        val id = fakeRepo.insertAttack(attack)
        assertEquals(attack.attackId, id)
        assertEquals(1, fakeRepo.insertedAttacks.size)
    }

    @Test
    fun `updateAttack оновлює атаку`() = runTest {
        fakeRepo.setAttacks(listOf(attack))
        val updated = attack.copy(name = "Greataxe +1")
        fakeRepo.updateAttack(updated)
        assertEquals(1, fakeRepo.updatedAttacks.size)
        assertEquals("Greataxe +1", fakeRepo.updatedAttacks.first().name)
    }

    @Test
    fun `deleteAttack видаляє атаку`() = runTest {
        fakeRepo.setAttacks(listOf(attack))
        fakeRepo.deleteAttack(attack)
        assertEquals(1, fakeRepo.deletedAttacks.size)
    }

    @Test
    fun `getAttacksForCharacter повертає лише атаки зазначеного персонажа`() = runTest {
        val attackForOther = Attack(attackId = 2, characterId = 99, name = "Dagger")
        fakeRepo.setAttacks(listOf(attack, attackForOther))
        val result = fakeRepo.getAttacksForCharacter(charId = 10L).first()
        assertEquals(1, result.size)
        assertEquals("Greataxe", result.first().name)
    }
}