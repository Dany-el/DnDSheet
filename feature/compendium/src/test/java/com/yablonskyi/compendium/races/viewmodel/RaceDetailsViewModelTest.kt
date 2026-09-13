package com.yablonskyi.compendium.races.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.yablonskyi.compendium.MainDispatcherRule
import com.yablonskyi.compendium.fake.FakeRaceRepository
import com.yablonskyi.model.rulebook.Race
import com.yablonskyi.ui.R
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
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RaceDetailsViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `loaded item shares an importable single item array`() = runTest(mainDispatcherRule.testDispatcher.scheduler) {
        val item = Race(id = "hb-A", name = "A", isHomebrew = true)
        val repo = FakeRaceRepository().apply { setRaces(listOf(item)) }
        val vm = RaceDetailsViewModel(repo, SavedStateHandle(mapOf("raceId" to item.id)))
        backgroundScope.launch { vm.uiState.collect {} }
        advanceUntilIdle()
        vm.onIntent(RaceDetailsIntent.ShareRequested)
        advanceUntilIdle()
        val effect = vm.fileEffect.first() as FileOperationEffect.ShareReady
        assertEquals("A.json", effect.fileName)
        assertEquals(listOf(item), Json.decodeFromString<List<Race>>(effect.json))
        vm.onIntent(RaceDetailsIntent.ShareFailed)
        advanceUntilIdle()
        assertEquals(RaceDetailsEffect.ShowSnackbar(R.string.failure_share), vm.effect.first())
    }

    @Test
    fun `unloaded and missing items do not share`() = runTest(mainDispatcherRule.testDispatcher.scheduler) {
        val vm = RaceDetailsViewModel(FakeRaceRepository(), SavedStateHandle(mapOf("raceId" to "missing")))
        val effects = mutableListOf<FileOperationEffect>()
        backgroundScope.launch { vm.fileEffect.collect { effects += it } }
        vm.onIntent(RaceDetailsIntent.ShareRequested)
        backgroundScope.launch { vm.uiState.collect {} }
        advanceUntilIdle()
        vm.onIntent(RaceDetailsIntent.ShareRequested)
        advanceUntilIdle()
        assertTrue(effects.isEmpty())
    }
}
