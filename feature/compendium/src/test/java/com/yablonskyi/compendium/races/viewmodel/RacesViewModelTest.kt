package com.yablonskyi.compendium.races.viewmodel

import com.yablonskyi.compendium.MainDispatcherRule
import com.yablonskyi.compendium.fake.FakeRaceRepository
import com.yablonskyi.model.rulebook.Race
import com.yablonskyi.ui.R
import com.yablonskyi.ui.utils.FileOperationEffect
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RacesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun homebrew(name: String) =
        Race(id = "hb-$name", name = name, isHomebrew = true)

    private fun original(name: String) =
        Race(id = "og-$name", name = name, isHomebrew = false)

    private fun TestScope.collectUiState(vm: RacesViewModel) {
        backgroundScope.launch { vm.uiState.collect {} }
    }

    @Test
    fun `loads and partitions races into homebrew and original`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val fake = FakeRaceRepository().apply {
                setRaces(listOf(original("Human"), homebrew("Ratfolk"), original("Elf")))
            }
            val vm = RacesViewModel(fake)
            collectUiState(vm)
            advanceUntilIdle()

            assertFalse(vm.uiState.value.isLoading)
            assertEquals(listOf("Human", "Elf"), vm.uiState.value.origRaces.map { it.name })
            assertEquals(listOf("Ratfolk"), vm.uiState.value.homebrewRaces.map { it.name })
        }

    @Test
    fun `search filters both sections case-insensitively`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val fake = FakeRaceRepository().apply {
                setRaces(listOf(original("Human"), homebrew("Ratfolk"), original("Orc")))
            }
            val vm = RacesViewModel(fake)
            collectUiState(vm)
            advanceUntilIdle()

            vm.onIntent(RacesIntent.SearchQueryChanged("RAT"))
            advanceUntilIdle()

            assertEquals(listOf("Ratfolk"), vm.uiState.value.homebrewRaces.map { it.name })
            assertEquals(emptyList<String>(), vm.uiState.value.origRaces.map { it.name })
        }

    @Test
    fun `toggling selection marks selection mode and select all`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val fake = FakeRaceRepository().apply {
                setRaces(listOf(homebrew("A"), homebrew("B"), original("Human")))
            }
            val vm = RacesViewModel(fake)
            collectUiState(vm)
            advanceUntilIdle()

            vm.onIntent(RacesIntent.ToggleSelection("hb-A"))
            advanceUntilIdle()
            assertTrue(vm.uiState.value.isSelectionMode)
            assertEquals(setOf("hb-A"), vm.uiState.value.selectedRaceIds)
            assertFalse(vm.uiState.value.isAllSelected)

            vm.onIntent(RacesIntent.ToggleSelectAll)
            advanceUntilIdle()
            assertEquals(setOf("hb-A", "hb-B"), vm.uiState.value.selectedRaceIds)
            assertTrue(vm.uiState.value.isAllSelected)

            vm.onIntent(RacesIntent.ClearSelection)
            advanceUntilIdle()
            assertFalse(vm.uiState.value.isSelectionMode)
            assertEquals(emptySet<String>(), vm.uiState.value.selectedRaceIds)
        }

    @Test
    fun `delete selected removes homebrew races from repository`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val fake = FakeRaceRepository().apply {
                setRaces(listOf(homebrew("Ratfolk"), homebrew("Keep")))
            }
            val vm = RacesViewModel(fake)
            collectUiState(vm)
            advanceUntilIdle()

            vm.onIntent(RacesIntent.ToggleSelection("hb-Ratfolk"))
            advanceUntilIdle()
            vm.onIntent(RacesIntent.DeleteSelected)
            advanceUntilIdle()

            assertEquals(listOf("Ratfolk"), fake.deleted.map { it.name })
            assertEquals(listOf("Keep"), vm.uiState.value.homebrewRaces.map { it.name })
            assertFalse(vm.uiState.value.isSelectionMode)
        }

    @Test
    fun `import decodes json and inserts as homebrew`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val fake = FakeRaceRepository()
            val vm = RacesViewModel(fake)
            collectUiState(vm)
            advanceUntilIdle()

            val json = Json.encodeToString(listOf(Race(id = "x", name = "Imported")))
            vm.onIntent(RacesIntent.ImportRequested(json))
            advanceUntilIdle()

            val inserted = fake.insertedAll.flatten()
            assertEquals(listOf("Imported"), inserted.map { it.name })
            assertTrue(inserted.all { it.isHomebrew })
        }

    @Test
    fun `share emits file effect with serialized json`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val fake = FakeRaceRepository().apply {
                setRaces(listOf(homebrew("Ratfolk"), original("Human")))
            }
            val vm = RacesViewModel(fake)
            collectUiState(vm)
            advanceUntilIdle()

            vm.onIntent(RacesIntent.ShareRequested("hb-Ratfolk"))
            advanceUntilIdle()

            val export = vm.fileEffect.first() as FileOperationEffect.ShareReady
            assertEquals("Ratfolk.json", export.fileName)
            val decoded = Json.decodeFromString<List<Race>>(export.json)
            assertEquals(listOf("Ratfolk"), decoded.map { it.name })
        }

    @Test
    fun `invalid json import shows failure snackbar`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val fake = FakeRaceRepository()
            val vm = RacesViewModel(fake)
            collectUiState(vm)
            advanceUntilIdle()

            vm.onIntent(RacesIntent.ImportRequested("not-json"))
            advanceUntilIdle()

            assertEquals(
                RacesEffect.ShowSnackbar(R.string.failure_import),
                vm.effect.first()
            )
        }

    @Test
    fun `sharing preserves selection and failures report sharing error`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val vm = RacesViewModel(FakeRaceRepository().apply { setRaces(listOf(homebrew("A"))) })
            collectUiState(vm)
            advanceUntilIdle()
            vm.onIntent(RacesIntent.ToggleSelection("hb-A"))
            advanceUntilIdle()
            vm.onIntent(RacesIntent.ShareRequested("hb-A"))
            advanceUntilIdle()
            val shared = vm.fileEffect.first() as FileOperationEffect.ShareReady
            assertEquals(listOf(homebrew("A")), Json.decodeFromString<List<Race>>(shared.json))
            assertEquals(setOf("hb-A"), vm.uiState.value.selectedRaceIds)
            vm.onIntent(RacesIntent.ShareFailed)
            advanceUntilIdle()
            assertEquals(RacesEffect.ShowSnackbar(R.string.failure_share), vm.effect.first())
            assertEquals(setOf("hb-A"), vm.uiState.value.selectedRaceIds)
        }

    @Test
    fun `selection export saves one or multiple items`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val vm = RacesViewModel(FakeRaceRepository().apply { setRaces(listOf(homebrew("A"), homebrew("B"))) })
            collectUiState(vm)
            advanceUntilIdle()
            vm.onIntent(RacesIntent.ToggleSelection("hb-A"))
            advanceUntilIdle()
            vm.onIntent(RacesIntent.ExportAllSelected)
            advanceUntilIdle()
            val single = vm.fileEffect.first() as FileOperationEffect.ExportReady
            assertEquals(listOf(homebrew("A")), Json.decodeFromString<List<Race>>(single.json))
            vm.onIntent(RacesIntent.ToggleSelection("hb-B"))
            advanceUntilIdle()
            vm.onIntent(RacesIntent.ExportAllSelected)
            advanceUntilIdle()
            val multiple = vm.fileEffect.first() as FileOperationEffect.ExportReady
            assertEquals(listOf(homebrew("A"), homebrew("B")), Json.decodeFromString<List<Race>>(multiple.json))
        }

    @Test
    fun `missing item and empty selection emit no file effects`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val vm = RacesViewModel(FakeRaceRepository())
            collectUiState(vm)
            val effects = mutableListOf<FileOperationEffect>()
            backgroundScope.launch { vm.fileEffect.collect { effects += it } }
            advanceUntilIdle()
            vm.onIntent(RacesIntent.ShareRequested("missing"))
            vm.onIntent(RacesIntent.ExportAllSelected)
            advanceUntilIdle()
            assertTrue(effects.isEmpty())
        }

    @Test
    fun `export entry allows empty selection until explicitly cancelled`() = runTest(mainDispatcherRule.testDispatcher.scheduler) {
        val vm = RacesViewModel(FakeRaceRepository().apply { setRaces(listOf(homebrew("A"))) })
        collectUiState(vm)
        advanceUntilIdle()
        vm.onIntent(RacesIntent.EnterSelectionMode)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isSelectionMode)
        assertTrue(vm.uiState.value.selectedRaceIds.isEmpty())
        vm.onIntent(RacesIntent.ToggleSelection("hb-A"))
        vm.onIntent(RacesIntent.ToggleSelection("hb-A"))
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isSelectionMode)
        vm.onIntent(RacesIntent.ClearSelection)
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isSelectionMode)
    }

    @Test
    fun `select all preserves hidden selections and export resolves current items`() = runTest(mainDispatcherRule.testDispatcher.scheduler) {
        val fake = FakeRaceRepository().apply { setRaces(listOf(homebrew("A"), homebrew("B"))) }
        val vm = RacesViewModel(fake)
        collectUiState(vm)
        advanceUntilIdle()
        vm.onIntent(RacesIntent.ToggleSelection("hb-A"))
        vm.onIntent(RacesIntent.SearchQueryChanged("B"))
        advanceUntilIdle()
        vm.onIntent(RacesIntent.ToggleSelectAll)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isAllSelected)
        assertEquals(setOf("hb-A", "hb-B"), vm.uiState.value.selectedRaceIds)
        vm.onIntent(RacesIntent.ToggleSelectAll)
        advanceUntilIdle()
        assertEquals(setOf("hb-A"), vm.uiState.value.selectedRaceIds)
        val updated = homebrew("A").copy(name = "Updated")
        fake.setRaces(listOf(updated, homebrew("B")))
        advanceUntilIdle()
        vm.onIntent(RacesIntent.ExportAllSelected)
        advanceUntilIdle()
        val export = vm.fileEffect.first() as FileOperationEffect.ExportReady
        assertEquals(listOf(updated), Json.decodeFromString<List<Race>>(export.json))
        vm.onIntent(RacesIntent.ExportCompleted(false))
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isSelectionMode)
        assertEquals(setOf("hb-A"), vm.uiState.value.selectedRaceIds)
        vm.onIntent(RacesIntent.ExportCompleted(true))
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isSelectionMode)
        assertTrue(vm.uiState.value.selectedRaceIds.isEmpty())
    }
}
