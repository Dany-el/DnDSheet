package com.yablonskyi.character.presentation.sheet

import androidx.lifecycle.SavedStateHandle
import com.yablonskyi.character.testutil.*
import com.yablonskyi.character.presentation.common.*
import com.yablonskyi.character.presentation.sheet.model.*
import com.yablonskyi.domain.character.*
import com.yablonskyi.model.character.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CharacterSheetViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    private val repository = FakeCharacterRepository()
    private val attacks = FakeAttackRepository()
    private fun vm(handle: SavedStateHandle = SavedStateHandle(mapOf("id" to 7L))) = CharacterSheetViewModel(repository, FakeSpellRepository(), attacks, CharacterTransitionCache(), handle)

    @Test fun givenMissingCharacter_whenAttacksEmit_thenShowsNotFoundWithoutCrash() = runTest {
        repository.characters.value = emptyList()
        attacks.attacks.value = listOf(Attack(characterId = 7))
        val vm = vm(); advanceUntilIdle()
        assertEquals(CharacterLoadStatus.NOT_FOUND, vm.state.value.status)
        assertTrue(vm.state.value.attacks.isEmpty())
    }

    @Test fun givenRapidChanges_whenSaved_thenPreservesBothFields() = runTest {
        val vm = vm(); advanceUntilIdle()
        val note = Note("15412a7e-37e6-4e8a-92cb-af49e0759032", "Notes", RichText(plainText = "New notes"))
        vm.onIntent(CharacterSheetIntent.Change(CharacterChange.AddNote(note)))
        vm.onIntent(CharacterSheetIntent.Change(CharacterChange.Health(10, 20, 3))); advanceUntilIdle()
        assertEquals(listOf(note), vm.state.value.character?.notes)
        assertEquals(10, vm.state.value.character?.currentHp)
        assertEquals(0, vm.state.value.pendingWrites)
    }

    @Test fun givenOpposingTab_whenSelected_thenSwapsAndRestoresTabs() = runTest {
        val handle = SavedStateHandle(mapOf("id" to 7L))
        val vm = vm(handle)
        vm.onIntent(CharacterSheetIntent.LeftTabSelected(CharacterTab.SPELLS))
        assertEquals(CharacterTab.ABILITIES, vm.state.value.rightSelectedTab)
        val restored = vm(handle)
        assertEquals(CharacterTab.SPELLS, restored.state.value.leftSelectedTab)
        assertEquals(CharacterTab.ABILITIES, restored.state.value.rightSelectedTab)
    }

    @Test fun givenSheet_whenSettingsRequested_thenNavigatesWithRouteId() = runTest {
        val vm = vm()
        vm.onIntent(CharacterSheetIntent.OpenSettings)
        assertEquals(CharacterSheetEffect.OpenSettings(7), vm.effects.first())
    }

    @Test fun givenSheet_whenDiceHistoryRequested_thenNavigatesWithRouteId() = runTest {
        val vm = vm()
        vm.onIntent(CharacterSheetIntent.OpenDiceHistory)
        assertEquals(CharacterSheetEffect.OpenDiceHistory(7), vm.effects.first())
    }

    @Test fun givenFailedWriteAndLaterEdit_whenRetried_thenPreservesIntentOrder() = runTest {
        val vm = vm(); advanceUntilIdle()
        repository.failure = IllegalStateException()
        vm.onIntent(CharacterSheetIntent.Change(CharacterChange.Text(CharacterTextField.BACKSTORY, "First"))); advanceUntilIdle()
        repository.failure = null
        vm.onIntent(CharacterSheetIntent.Change(CharacterChange.Text(CharacterTextField.BACKSTORY, "Latest"))); advanceUntilIdle()
        vm.onIntent(CharacterSheetIntent.Retry); advanceUntilIdle()
        assertEquals("Latest", repository.characters.value.single().backstory)
    }
}
