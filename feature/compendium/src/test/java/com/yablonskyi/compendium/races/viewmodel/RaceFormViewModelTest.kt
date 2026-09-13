package com.yablonskyi.compendium.races.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.yablonskyi.compendium.MainDispatcherRule
import com.yablonskyi.compendium.fake.FakeRaceRepository
import com.yablonskyi.model.rulebook.Race
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class RaceFormViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createRoute() = SavedStateHandle(mapOf("raceId" to null as String?))

    private fun updateRoute(raceId: String) = SavedStateHandle(mapOf("raceId" to raceId))

    @Test
    fun `create mode form is invalid until required fields are filled`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val vm = RaceFormViewModel(FakeRaceRepository(), createRoute())

            assertFalse(vm.uiState.value.isFormValid)

            vm.onIntent(RaceFormIntent.NameChanged("Elf"))
            vm.onIntent(RaceFormIntent.SizeChanged("Medium"))
            vm.onIntent(RaceFormIntent.TraitsChanged("Darkvision"))
            vm.onIntent(RaceFormIntent.DescriptionChanged("desc"))
            assertTrue(vm.uiState.value.isFormValid)
        }

    @Test
    fun `invalid speed marks form invalid and back to valid`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val vm = RaceFormViewModel(FakeRaceRepository(), createRoute())
            vm.onIntent(RaceFormIntent.NameChanged("Elf"))
            vm.onIntent(RaceFormIntent.SizeChanged("Medium"))
            vm.onIntent(RaceFormIntent.TraitsChanged("Darkvision"))
            vm.onIntent(RaceFormIntent.DescriptionChanged("desc"))

            vm.onIntent(RaceFormIntent.SpeedChanged(150))
            assertFalse(vm.uiState.value.isFormValid)

            vm.onIntent(RaceFormIntent.SpeedChanged(30))
            assertTrue(vm.uiState.value.isFormValid)
        }

    @Test
    fun `update mode loads race and form is valid without edits`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val existing = Race(
                id = "r1",
                name = "Elf",
                size = "Medium",
                traits = listOf("Darkvision", "Keen Senses"),
                description = "An elven race."
            )
            val fake = FakeRaceRepository().apply { setRaces(listOf(existing)) }
            val vm = RaceFormViewModel(fake, updateRoute("r1"))
            advanceUntilIdle()

            assertEquals("Elf", vm.uiState.value.name.text)
            assertEquals("Medium", vm.uiState.value.size.text)
            assertEquals(listOf("Darkvision", "Keen Senses"), vm.uiState.value.parsedTraits)
            assertEquals("An elven race.", vm.uiState.value.description.text)
            assertTrue(vm.uiState.value.isFormValid)
        }
}
