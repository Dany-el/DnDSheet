package com.yablonskyi.compendium.classes.viewmodel

import com.yablonskyi.compendium.MainDispatcherRule
import com.yablonskyi.compendium.fake.FakeClassRepository
import com.yablonskyi.model.rulebook.CharacterClass
import com.yablonskyi.ui.R
import com.yablonskyi.ui.utils.FileOperationEffect
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CharacterClassesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun homebrew(name: String) =
        CharacterClass(id = "hb-$name", name = name, isHomebrew = true)

    private fun original(name: String) =
        CharacterClass(id = "og-$name", name = name, isHomebrew = false)

    private fun TestScope.collectUiState(vm: CharacterClassesViewModel) {
        backgroundScope.launch { vm.uiState.collect {} }
    }

    @Test
    fun `loads and partitions classes into homebrew and original`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val fake = FakeClassRepository().apply {
                setClasses(listOf(original("Fighter"), homebrew("RatLord"), original("Bard")))
            }
            val vm = CharacterClassesViewModel(fake)
            collectUiState(vm)
            advanceUntilIdle()

            assertFalse(vm.uiState.value.isLoading)
            assertEquals(listOf("Fighter", "Bard"), vm.uiState.value.origClasses.map { it.name })
            assertEquals(listOf("RatLord"), vm.uiState.value.homebrewClasses.map { it.name })
        }

    @Test
    fun `search filters both sections case-insensitively`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val fake = FakeClassRepository().apply {
                setClasses(listOf(original("Fighter"), homebrew("RatLord"), original("Mage")))
            }
            val vm = CharacterClassesViewModel(fake)
            collectUiState(vm)
            advanceUntilIdle()

            vm.onIntent(ClassesIntent.SearchQueryChanged("rat"))
            advanceUntilIdle()

            assertEquals(emptyList<String>(), vm.uiState.value.origClasses.map { it.name })
            assertEquals(listOf("RatLord"), vm.uiState.value.homebrewClasses.map { it.name })
            assertEquals("rat", vm.uiState.value.searchQuery)
            vm.onIntent(ClassesIntent.SearchQueryChanged(""))
            advanceUntilIdle()
            assertEquals(2, vm.uiState.value.origClasses.size)
        }

    @Test
    fun `toggling selection marks selection mode and select all`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val fake = FakeClassRepository().apply {
                setClasses(listOf(homebrew("A"), homebrew("B"), original("Fighter")))
            }
            val vm = CharacterClassesViewModel(fake)
            collectUiState(vm)
            advanceUntilIdle()

            vm.onIntent(ClassesIntent.ToggleSelection("hb-A"))
            advanceUntilIdle()
            assertTrue(vm.uiState.value.isSelectionMode)
            assertEquals(setOf("hb-A"), vm.uiState.value.selectedClassesIds)
            assertFalse(vm.uiState.value.isAllSelected)

            vm.onIntent(ClassesIntent.ToggleSelectAll)
            advanceUntilIdle()
            assertEquals(setOf("hb-A", "hb-B"), vm.uiState.value.selectedClassesIds)
            assertTrue(vm.uiState.value.isAllSelected)

            vm.onIntent(ClassesIntent.ClearSelection)
            advanceUntilIdle()
            assertFalse(vm.uiState.value.isSelectionMode)
            assertEquals(emptySet<String>(), vm.uiState.value.selectedClassesIds)
        }

    @Test
    fun `delete removes class from repository and state`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val fake = FakeClassRepository().apply {
                setClasses(listOf(homebrew("RatLord"), homebrew("Keep")))
            }
            val vm = CharacterClassesViewModel(fake)
            collectUiState(vm)
            advanceUntilIdle()

            vm.onIntent(ClassesIntent.Delete(vm.uiState.value.homebrewClasses.first()))
            advanceUntilIdle()

            assertEquals(listOf("RatLord"), fake.deleted.map { it.name })
            assertEquals(listOf("Keep"), vm.uiState.value.homebrewClasses.map { it.name })
        }

    @Test
    fun `import decodes json, marks homebrew and inserts`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val fake = FakeClassRepository()
            val vm = CharacterClassesViewModel(fake)
            collectUiState(vm)
            advanceUntilIdle()

            val json = Json.encodeToString(
                listOf(CharacterClass(id = "x", name = "Imported"))
            )
            vm.onIntent(ClassesIntent.ImportRequested(json))
            advanceUntilIdle()

            assertTrue(fake.insertedAll.isNotEmpty())
            val inserted = fake.insertedAll.flatten()
            assertEquals(listOf("Imported"), inserted.map { it.name })
            assertTrue(inserted.all { it.isHomebrew })
            assertEquals(listOf("Imported"), vm.uiState.value.homebrewClasses.map { it.name })
        }

    @Test
    fun `import invalid json shows failure snackbar`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val fake = FakeClassRepository()
            val vm = CharacterClassesViewModel(fake)
            collectUiState(vm)
            advanceUntilIdle()

            vm.onIntent(ClassesIntent.ImportRequested("not-json"))
            advanceUntilIdle()

            assertEquals(
                ClassesEffect.ShowSnackbar(R.string.failure_import),
                vm.effect.first()
            )
        }

    @Test
    fun `share emits file effect with serialized json`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val fake = FakeClassRepository().apply {
                setClasses(listOf(homebrew("RatLord"), original("Fighter")))
            }
            val vm = CharacterClassesViewModel(fake)
            collectUiState(vm)
            advanceUntilIdle()

            vm.onIntent(ClassesIntent.ShareRequested("hb-RatLord"))
            advanceUntilIdle()

            val export = vm.fileEffect.first() as FileOperationEffect.ShareReady
            assertEquals("RatLord.json", export.fileName)
            val decoded = Json.decodeFromString<List<CharacterClass>>(export.json)
            assertEquals(listOf("RatLord"), decoded.map { it.name })
        }

    @Test
    fun `navigation intents emit navigation effects`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val fake = FakeClassRepository()
            val vm = CharacterClassesViewModel(fake)
            collectUiState(vm)
            advanceUntilIdle()

            vm.onIntent(ClassesIntent.CreateClass)
            advanceUntilIdle()
            assertEquals(ClassesEffect.NavigateToCreate, vm.effect.first())

            vm.onIntent(ClassesIntent.NavigateBack)
            advanceUntilIdle()
            assertEquals(ClassesEffect.NavigateBack, vm.effect.first())

            vm.onIntent(ClassesIntent.Edit("id"))
            advanceUntilIdle()
            assertEquals(ClassesEffect.NavigateToEdit("id"), vm.effect.first())

            vm.onIntent(ClassesIntent.Details("id"))
            advanceUntilIdle()
            assertEquals(ClassesEffect.NavigateToDetails("id"), vm.effect.first())
        }

    @Test
    fun `sharing preserves selection and failures report sharing error`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val vm = CharacterClassesViewModel(FakeClassRepository().apply { setClasses(listOf(homebrew("A"))) })
            collectUiState(vm)
            advanceUntilIdle()
            vm.onIntent(ClassesIntent.ToggleSelection("hb-A"))
            advanceUntilIdle()
            vm.onIntent(ClassesIntent.ShareRequested("hb-A"))
            advanceUntilIdle()
            val shared = vm.fileEffect.first() as FileOperationEffect.ShareReady
            assertEquals(listOf(homebrew("A")), Json.decodeFromString<List<CharacterClass>>(shared.json))
            assertEquals(setOf("hb-A"), vm.uiState.value.selectedClassesIds)
            vm.onIntent(ClassesIntent.ShareFailed)
            advanceUntilIdle()
            assertEquals(ClassesEffect.ShowSnackbar(R.string.failure_share), vm.effect.first())
            assertEquals(setOf("hb-A"), vm.uiState.value.selectedClassesIds)
        }

    @Test
    fun `selection export saves one or multiple items`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val vm = CharacterClassesViewModel(FakeClassRepository().apply { setClasses(listOf(homebrew("A"), homebrew("B"))) })
            collectUiState(vm)
            advanceUntilIdle()
            vm.onIntent(ClassesIntent.ToggleSelection("hb-A"))
            advanceUntilIdle()
            vm.onIntent(ClassesIntent.ExportAllSelected)
            advanceUntilIdle()
            val single = vm.fileEffect.first() as FileOperationEffect.ExportReady
            assertEquals(listOf(homebrew("A")), Json.decodeFromString<List<CharacterClass>>(single.json))
            vm.onIntent(ClassesIntent.ToggleSelection("hb-B"))
            advanceUntilIdle()
            vm.onIntent(ClassesIntent.ExportAllSelected)
            advanceUntilIdle()
            val multiple = vm.fileEffect.first() as FileOperationEffect.ExportReady
            assertEquals(listOf(homebrew("A"), homebrew("B")), Json.decodeFromString<List<CharacterClass>>(multiple.json))
        }

    @Test
    fun `missing item and empty selection emit no file effects`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val vm = CharacterClassesViewModel(FakeClassRepository())
            collectUiState(vm)
            val effects = mutableListOf<FileOperationEffect>()
            backgroundScope.launch { vm.fileEffect.collect { effects += it } }
            advanceUntilIdle()
            vm.onIntent(ClassesIntent.ShareRequested("missing"))
            vm.onIntent(ClassesIntent.ExportAllSelected)
            advanceUntilIdle()
            assertTrue(effects.isEmpty())
        }

    @Test
    fun `export entry allows empty selection until explicitly cancelled`() = runTest(mainDispatcherRule.testDispatcher.scheduler) {
        val vm = CharacterClassesViewModel(FakeClassRepository().apply { setClasses(listOf(homebrew("A"))) })
        collectUiState(vm)
        advanceUntilIdle()
        vm.onIntent(ClassesIntent.EnterSelectionMode)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isSelectionMode)
        assertTrue(vm.uiState.value.selectedClassesIds.isEmpty())
        vm.onIntent(ClassesIntent.ToggleSelection("hb-A"))
        vm.onIntent(ClassesIntent.ToggleSelection("hb-A"))
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isSelectionMode)
        vm.onIntent(ClassesIntent.ClearSelection)
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isSelectionMode)
    }

    @Test
    fun `select all preserves hidden selections and export resolves current items`() = runTest(mainDispatcherRule.testDispatcher.scheduler) {
        val fake = FakeClassRepository().apply { setClasses(listOf(homebrew("A"), homebrew("B"))) }
        val vm = CharacterClassesViewModel(fake)
        collectUiState(vm)
        advanceUntilIdle()
        vm.onIntent(ClassesIntent.ToggleSelection("hb-A"))
        vm.onIntent(ClassesIntent.SearchQueryChanged("B"))
        advanceUntilIdle()
        vm.onIntent(ClassesIntent.ToggleSelectAll)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isAllSelected)
        assertEquals(setOf("hb-A", "hb-B"), vm.uiState.value.selectedClassesIds)
        vm.onIntent(ClassesIntent.ToggleSelectAll)
        advanceUntilIdle()
        assertEquals(setOf("hb-A"), vm.uiState.value.selectedClassesIds)
        val updated = homebrew("A").copy(name = "Updated")
        fake.setClasses(listOf(updated, homebrew("B")))
        advanceUntilIdle()
        vm.onIntent(ClassesIntent.ExportAllSelected)
        advanceUntilIdle()
        val export = vm.fileEffect.first() as FileOperationEffect.ExportReady
        assertEquals(listOf(updated), Json.decodeFromString<List<CharacterClass>>(export.json))
        vm.onIntent(ClassesIntent.ExportCompleted(false))
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isSelectionMode)
        assertEquals(setOf("hb-A"), vm.uiState.value.selectedClassesIds)
        vm.onIntent(ClassesIntent.ExportCompleted(true))
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isSelectionMode)
        assertTrue(vm.uiState.value.selectedClassesIds.isEmpty())
    }
}
