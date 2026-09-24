package com.yablonskyi.character.presentation.sheet.editor

import androidx.lifecycle.SavedStateHandle
import com.yablonskyi.model.character.Character
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test

class HealthFormViewModelTest {
    private fun create() = HealthFormViewModel(SavedStateHandle()).also {
        it.onIntent(HealthFormIntent.BeginSession("a", Character(id = 1, currentHp = 20, maxHp = 30, tempHp = 5)))
    }
    @Test fun givenTemporaryHp_whenDamaged_thenConsumesTemporaryHpFirst() {
        val vm = create()
        vm.onIntent(HealthFormIntent.Changed(HealthField.AMOUNT, 8))
        vm.onIntent(HealthFormIntent.Damage)
        assertEquals(17, vm.uiState.value.current.value)
        assertEquals(0, vm.uiState.value.temporary.value)
    }
    @Test fun givenHugeAdjustment_whenHealingOrDamaging_thenDoesNotOverflow() {
        val vm = create()
        vm.onIntent(HealthFormIntent.Changed(HealthField.AMOUNT, Int.MAX_VALUE))
        vm.onIntent(HealthFormIntent.Heal)
        assertEquals(30, vm.uiState.value.current.value)
        assertEquals(5, vm.uiState.value.temporary.value)
        vm.onIntent(HealthFormIntent.Damage)
        assertEquals(0, vm.uiState.value.current.value)
    }
    @Test fun givenCurrentHp_whenMaximumReduced_thenClampsCurrentAndValidatesBounds() {
        val vm = create()
        vm.onIntent(HealthFormIntent.Changed(HealthField.MAXIMUM, 10))
        assertEquals(10, vm.uiState.value.current.value)
        vm.onIntent(HealthFormIntent.Changed(HealthField.MAXIMUM, 10000))
        assertTrue(vm.uiState.value.isValid)
        vm.onIntent(HealthFormIntent.Changed(HealthField.MAXIMUM, 10001))
        assertFalse(vm.uiState.value.isValid)
    }
    @Test fun givenDirtyHealth_whenStaleSnapshotArrives_thenPreservesDraftUntilNewSession() {
        val vm = create()
        vm.onIntent(HealthFormIntent.MarkDead)
        vm.onIntent(HealthFormIntent.Synchronize(Character(id = 1, currentHp = 20, maxHp = 30)))
        assertEquals(0, vm.uiState.value.current.value)
        vm.onIntent(HealthFormIntent.BeginSession("b", Character(id = 1, currentHp = 20, maxHp = 30)))
        assertEquals(20, vm.uiState.value.current.value)
    }

    @Test fun givenAcceptedEdit_whenSheetClosesBeforeCollection_thenWriteIsStillAvailable() = kotlinx.coroutines.test.runTest {
        val vm = create()
        vm.onIntent(HealthFormIntent.MarkDead)
        vm.onIntent(HealthFormIntent.EndSession)
        val effect = vm.effects.first() as HealthFormEffect.ChangeRequested
        assertEquals(0, effect.change.current)
        assertEquals(0, effect.change.temp)
        assertFalse(vm.uiState.value.initialized)
    }
}
