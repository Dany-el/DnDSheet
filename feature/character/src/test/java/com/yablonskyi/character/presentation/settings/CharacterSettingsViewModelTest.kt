package com.yablonskyi.character.presentation.settings

import androidx.lifecycle.SavedStateHandle
import com.yablonskyi.character.testutil.*
import com.yablonskyi.domain.character.*
import com.yablonskyi.character.presentation.common.CharacterUiError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CharacterSettingsViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    private val repository = FakeCharacterRepository()
    private val images = FakeCharacterImageRepository()
    private fun vm(handle: SavedStateHandle = SavedStateHandle(mapOf("id" to 7L))) = CharacterSettingsViewModel(repository, images, handle)

    @Test fun givenDraft_whenUnrelatedRepositoryEmissionArrives_thenRetainsDraft() = runTest {
        val handle = SavedStateHandle(mapOf("id" to 7L))
        val vm = vm(handle); advanceUntilIdle()
        vm.onIntent(CharacterSettingsIntent.Change(CharacterChange.Text(CharacterTextField.NAME, "Typing")))
        repository.characters.value = listOf(repository.characters.value.single().copy(currentHp = 2)); advanceUntilIdle()
        assertEquals("Typing", vm.state.value.character?.name)
        assertEquals(2, vm.state.value.character?.currentHp)
        val restored = vm(handle); advanceUntilIdle()
        assertEquals("Typing", restored.state.value.character?.name)
    }

    @Test fun givenImagePicker_whenCancelled_thenClearsPendingState() = runTest {
        val vm = vm(); advanceUntilIdle()
        vm.onIntent(CharacterSettingsIntent.ImagePickerClicked)
        assertEquals(CharacterSettingsEffect.LaunchImagePicker, vm.effects.first())
        vm.onIntent(CharacterSettingsIntent.ImageSelected(null)); advanceUntilIdle()
        assertFalse(vm.state.value.isPickingImage)
        assertFalse(vm.state.value.isSavingImage)
        assertTrue(images.replacements.isEmpty())
    }

    @Test fun givenImageFailure_whenRetried_thenUsesOriginalCharacterAndUri() = runTest {
        val vm = vm(); advanceUntilIdle()
        images.failure = IllegalStateException()
        vm.onIntent(CharacterSettingsIntent.ImagePickerClicked); vm.effects.first()
        vm.onIntent(CharacterSettingsIntent.ImageSelected("content://image")); advanceUntilIdle()
        assertTrue(CharacterUiError.IMAGE in vm.state.value.errors)
        images.failure = null
        vm.onIntent(CharacterSettingsIntent.Retry); advanceUntilIdle()
        assertEquals(listOf(7L to "content://image"), images.replacements)
        assertFalse(vm.state.value.isSavingImage)
    }
}
