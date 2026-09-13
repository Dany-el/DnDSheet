package com.yablonskyi.compendium.spells.viewmodel

import com.yablonskyi.compendium.MainDispatcherRule
import com.yablonskyi.compendium.fake.FakeSpellRepository
import com.yablonskyi.model.character.Spell
import com.yablonskyi.ui.R
import com.yablonskyi.ui.spell.SpellsEffect
import com.yablonskyi.ui.spell.SpellsIntent
import com.yablonskyi.ui.utils.FileOperationEffect
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CompendiumSpellsLibraryViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val spell = Spell(
        spellId = 42,
        name = "Shared spell",
        description = "Unicode: вогонь\nSecond line",
        material = "A crystal",
        isRitual = true,
        damageDice = "2d6",
        higherLevels = "An additional die",
    )

    @Test
    fun `sharing emits one complete importable spell without clearing selection`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val fake = FakeSpellRepository()
            val vm = CompendiumSpellsLibraryViewModel(fake)
            backgroundScope.launch { vm.uiState.collect {} }
            val effects = mutableListOf<SpellsEffect>()
            backgroundScope.launch { vm.effect.collect { effects += it } }
            advanceUntilIdle()
            vm.onIntent(SpellsIntent.ToggleSelection(spell))
            advanceUntilIdle()
            vm.onIntent(SpellsIntent.ShareRequested(spell))
            advanceUntilIdle()

            val shared = vm.fileEffect.first() as FileOperationEffect.ShareReady
            assertEquals("Shared_spell.json", shared.fileName)
            assertEquals(listOf(spell), Json.decodeFromString<List<Spell>>(shared.json))
            assertEquals(setOf(spell.spellId), vm.uiState.value.selectedSpellIds)
            assertTrue(vm.uiState.value.isSelectionMode)
            assertTrue(effects.isEmpty())

            vm.onIntent(SpellsIntent.ImportRequested(shared.json))
            advanceUntilIdle()
            assertEquals(listOf(spell), vm.uiState.value.pendingImport)
        }

    @Test
    fun `sharing failure reports sharing error and preserves selection`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val fake = FakeSpellRepository()
            val vm = CompendiumSpellsLibraryViewModel(fake)
            backgroundScope.launch { vm.uiState.collect {} }
            advanceUntilIdle()
            vm.onIntent(SpellsIntent.ToggleSelection(spell))
            vm.onIntent(SpellsIntent.ShareFailed)
            advanceUntilIdle()
            assertEquals(SpellsEffect.ShowSnackbar(R.string.failure_share), vm.effect.first())
            assertEquals(setOf(spell.spellId), vm.uiState.value.selectedSpellIds)
        }

    @Test
    fun `selection export still saves selected spells as one file`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val fake = FakeSpellRepository()
            val vm = CompendiumSpellsLibraryViewModel(fake)
            backgroundScope.launch { vm.uiState.collect {} }
            advanceUntilIdle()
            val other = spell.copy(spellId = 43, name = "Other")
            vm.onIntent(SpellsIntent.ToggleSelection(spell))
            vm.onIntent(SpellsIntent.ToggleSelection(other))
            fake.setLibrary(listOf(spell, other))
            advanceUntilIdle()
            for (intent in listOf(SpellsIntent.ExportAllSelected)) {
                vm.onIntent(intent)
                advanceUntilIdle()
                val exported = vm.fileEffect.first() as FileOperationEffect.ExportReady
                assertEquals(listOf(spell, other), Json.decodeFromString<List<Spell>>(exported.json))
            }
        }

    @Test
    fun `filtered select all preserves hidden spells and empty mode persists`() = runTest(mainDispatcherRule.testDispatcher.scheduler) {
        val other = spell.copy(spellId = 43, name = "Other", isRitual = false)
        val fake = FakeSpellRepository().apply { setLibrary(listOf(spell, other)) }
        val vm = CompendiumSpellsLibraryViewModel(fake)
        backgroundScope.launch { vm.uiState.collect {} }
        advanceUntilIdle()
        vm.onIntent(SpellsIntent.EnterSelectionMode)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isSelectionMode)
        assertTrue(vm.uiState.value.selectedSpellIds.isEmpty())
        vm.onIntent(SpellsIntent.ToggleSelection(other))
        vm.onIntent(SpellsIntent.ToggleRitual)
        vm.onIntent(SpellsIntent.SearchQueryChanged("Shared"))
        advanceUntilIdle()
        vm.onIntent(SpellsIntent.ToggleSelectAll)
        advanceUntilIdle()
        assertEquals(setOf(spell.spellId, other.spellId), vm.uiState.value.selectedSpellIds)
        assertTrue(vm.uiState.value.isAllSelected)
        vm.onIntent(SpellsIntent.ToggleSelectAll)
        advanceUntilIdle()
        assertEquals(setOf(other.spellId), vm.uiState.value.selectedSpellIds)
        val updated = other.copy(description = "Changed")
        fake.setLibrary(listOf(spell, updated))
        advanceUntilIdle()
        vm.onIntent(SpellsIntent.ExportAllSelected)
        advanceUntilIdle()
        assertEquals(listOf(updated), Json.decodeFromString<List<Spell>>((vm.fileEffect.first() as FileOperationEffect.ExportReady).json))
        vm.onIntent(SpellsIntent.ExportCompleted(false))
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isSelectionMode)
        vm.onIntent(SpellsIntent.ToggleSelection(other))
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isSelectionMode)
        assertTrue(vm.uiState.value.selectedSpellIds.isEmpty())
        vm.onIntent(SpellsIntent.ExportCompleted(true))
        advanceUntilIdle()
        assertEquals(false, vm.uiState.value.isSelectionMode)
    }
}
