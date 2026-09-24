package com.yablonskyi.character.presentation.settings

import androidx.lifecycle.SavedStateHandle
import com.yablonskyi.character.presentation.common.*
import com.yablonskyi.domain.character.*
import com.yablonskyi.model.character.Character
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class CharacterSettingsFormViewModelTest {
    @Test fun givenLevel19_whenIncrementing_thenReaches20AndStops() {
        val vm = CharacterSettingsFormViewModel(SavedStateHandle())
        vm.onIntent(CharacterSettingsFormIntent.Synchronize(Character(id = 1, level = 19)))
        repeat(3) { vm.onIntent(CharacterSettingsFormIntent.IncreaseLevel) }
        assertEquals(20, vm.uiState.value.numbers.getValue(CharacterNumberField.LEVEL).value)
        repeat(30) { vm.onIntent(CharacterSettingsFormIntent.DecreaseLevel) }
        assertEquals(1, vm.uiState.value.numbers.getValue(CharacterNumberField.LEVEL).value)
    }
    @Test fun givenInvalidLevel_whenEditingName_thenIndependentValidChangeIsEmitted() = runTest {
        val vm = CharacterSettingsFormViewModel(SavedStateHandle())
        vm.onIntent(CharacterSettingsFormIntent.Synchronize(Character(id = 1)))
        vm.onIntent(CharacterSettingsFormIntent.Change(CharacterChange.Number(CharacterNumberField.LEVEL, 21)))
        vm.onIntent(CharacterSettingsFormIntent.Change(CharacterChange.Text(CharacterTextField.NAME, "Hero")))
        assertEquals(CharacterChange.Text(CharacterTextField.NAME, "Hero"), (vm.effects.first() as CharacterSettingsFormEffect.ChangeRequested).change)
    }
    @Test fun givenNewerDraft_whenOldWriteIsAbandoned_thenKeepsNewerInput() = runTest {
        val vm = CharacterSettingsFormViewModel(SavedStateHandle())
        val original = Character(id = 1, name = "Original")
        vm.onIntent(CharacterSettingsFormIntent.Synchronize(original))
        vm.onIntent(CharacterSettingsFormIntent.Change(CharacterChange.Text(CharacterTextField.NAME, "First")))
        val first = vm.effects.first() as CharacterSettingsFormEffect.ChangeRequested
        vm.onIntent(CharacterSettingsFormIntent.Change(CharacterChange.Text(CharacterTextField.NAME, "Second")))
        vm.onIntent(CharacterSettingsFormIntent.WriteFinished(FormWriteResult(first.write, first.change, false, original)))
        vm.onIntent(CharacterSettingsFormIntent.Synchronize(original))
        assertEquals("Second", vm.uiState.value.texts.getValue(CharacterTextField.NAME).text)
    }
    @Test fun givenFailedEdit_whenAbandoned_thenRestoresPersistedValue() = runTest {
        val vm = CharacterSettingsFormViewModel(SavedStateHandle())
        val original = Character(id = 1, name = "Original")
        vm.onIntent(CharacterSettingsFormIntent.Synchronize(original))
        vm.onIntent(CharacterSettingsFormIntent.Change(CharacterChange.Text(CharacterTextField.NAME, "Draft")))
        val edit = vm.effects.first() as CharacterSettingsFormEffect.ChangeRequested
        vm.onIntent(CharacterSettingsFormIntent.WriteFinished(FormWriteResult(edit.write, edit.change, false, original)))
        assertEquals("Original", vm.uiState.value.texts.getValue(CharacterTextField.NAME).text)
    }
}
