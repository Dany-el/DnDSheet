package com.yablonskyi.compendium.spells.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.yablonskyi.compendium.MainDispatcherRule
import com.yablonskyi.compendium.fake.FakeSpellRepository
import com.yablonskyi.model.character.Ability
import com.yablonskyi.model.character.AttackType
import com.yablonskyi.model.character.Component
import com.yablonskyi.model.character.Spell
import com.yablonskyi.model.character.SpellRangeType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SpellFormViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createRoute() = SavedStateHandle(mapOf("spellId" to 0L))

    private fun updateRoute(spellId: Long) = SavedStateHandle(mapOf("spellId" to spellId))

    @Test
    fun `create mode has default state and invalid form`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val fake = FakeSpellRepository()
            val vm = SpellFormViewModel(fake, createRoute())

            assertFalse(vm.uiState.value.isLoading)
            assertEquals(0L, vm.uiState.value.id)
            assertEquals("", vm.uiState.value.name.text)
            assertFalse(vm.uiState.value.isFormValid)
        }

    @Test
    fun `submit in create mode inserts spell and navigates back`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val fake = FakeSpellRepository()
            val vm = SpellFormViewModel(fake, createRoute())
            advanceUntilIdle()

            vm.onIntent(SpellFormIntent.NameChanged("Firebolt"))
            vm.onIntent(SpellFormIntent.DescriptionChanged("desc"))
            vm.onIntent(SpellFormIntent.Submit)
            advanceUntilIdle()

            assertEquals(1, fake.inserted.size)
            val spell = fake.inserted.single()
            assertEquals("Firebolt", spell.name)
            assertEquals(0L, spell.spellId)
            assertEquals(SpellFormEffect.NavigateBack, vm.effect.first())
        }

    @Test
    fun `update mode loads spell from repository`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val existing = Spell(spellId = 7L, name = "Old", isRitual = true, description = "old desc")
            val fake = FakeSpellRepository().apply { setSpellById(existing) }
            val vm = SpellFormViewModel(fake, updateRoute(7L))
            advanceUntilIdle()

            assertFalse(vm.uiState.value.isLoading)
            assertEquals(7L, vm.uiState.value.id)
            assertEquals("Old", vm.uiState.value.name.text)
            assertEquals("old desc", vm.uiState.value.description.text)
            assertTrue(vm.uiState.value.isRitual)
            assertTrue(vm.uiState.value.isFormValid)
        }

    @Test
    fun `submit in update mode calls updateSpell preserving id`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val existing = Spell(spellId = 7L, name = "Old", description = "old desc")
            val fake = FakeSpellRepository().apply { setSpellById(existing) }
            val vm = SpellFormViewModel(fake, updateRoute(7L))
            advanceUntilIdle()

            vm.onIntent(SpellFormIntent.NameChanged("New"))
            vm.onIntent(SpellFormIntent.Submit)
            advanceUntilIdle()

            assertEquals(1, fake.updated.size)
            val spell = fake.updated.single()
            assertEquals(7L, spell.spellId)
            assertEquals("New", spell.name)
        }

    @Test
    fun `component toggling toggles components set`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val vm = SpellFormViewModel(FakeSpellRepository(), createRoute())
            advanceUntilIdle()

            assertFalse(Component.VERBAL in vm.uiState.value.components)
            vm.onIntent(SpellFormIntent.ComponentToggled(Component.VERBAL))
            assertTrue(Component.VERBAL in vm.uiState.value.components)
            vm.onIntent(SpellFormIntent.ComponentToggled(Component.VERBAL))
            assertFalse(Component.VERBAL in vm.uiState.value.components)
        }

    @Test
    fun `save attack type requires save stat for form validity`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val vm = SpellFormViewModel(FakeSpellRepository(), createRoute())
            advanceUntilIdle()
            vm.onIntent(SpellFormIntent.NameChanged("Spell"))
            vm.onIntent(SpellFormIntent.DescriptionChanged("desc"))

            vm.onIntent(SpellFormIntent.AttackTypeChanged(AttackType.SAVE))
            assertFalse(vm.uiState.value.isFormValid)
            assertEquals(AttackType.SAVE, vm.uiState.value.attackType)

            vm.onIntent(SpellFormIntent.SaveStatChanged(Ability.WIS))
            assertTrue(vm.uiState.value.isFormValid)
        }

    @Test
    fun `range value validation marks form invalid when out of range`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val vm = SpellFormViewModel(FakeSpellRepository(), createRoute())
            advanceUntilIdle()
            vm.onIntent(SpellFormIntent.NameChanged("Spell"))
            vm.onIntent(SpellFormIntent.DescriptionChanged("desc"))

            vm.onIntent(SpellFormIntent.RangeTypeChanged(SpellRangeType.DISTANCE))
            advanceUntilIdle()
            assertTrue(vm.uiState.value.isFormValid)

            vm.onIntent(SpellFormIntent.RangeValueChanged(5000))
            assertFalse(vm.uiState.value.isFormValid)

            vm.onIntent(SpellFormIntent.RangeValueChanged(30))
            assertTrue(vm.uiState.value.isFormValid)
        }

    @Test
    fun `damage dice typing rejects invalid partial input`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val vm = SpellFormViewModel(FakeSpellRepository(), createRoute())
            advanceUntilIdle()

            vm.onIntent(SpellFormIntent.DamageDiceChanged("1d"))
            assertEquals("1d", vm.uiState.value.damageDice.text)

            vm.onIntent(SpellFormIntent.DamageDiceChanged("1dx"))
            assertEquals("1d", vm.uiState.value.damageDice.text)
        }

    @Test
    fun `persisted text is restored from saved state handle`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val handle = SavedStateHandle(
                mapOf("spellId" to 0L, "spell_name" to "Persisted Name")
            )
            val vm = SpellFormViewModel(FakeSpellRepository(), handle)

            assertEquals("Persisted Name", vm.uiState.value.name.text)
        }
}
