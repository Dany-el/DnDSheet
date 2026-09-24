package com.yablonskyi.character.presentation.sheet.editor

import androidx.lifecycle.SavedStateHandle
import com.yablonskyi.model.character.*
import com.yablonskyi.model.dice.DiceRoles
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test

class AttackFormViewModelTest {
    private fun create(attack: Attack = Attack(name = "Sword", damageDice = "1d8"), saved: SavedStateHandle = SavedStateHandle()) =
        AttackFormViewModel(saved).also { it.onIntent(AttackFormIntent.BeginSession("session", attack)) }

    @Test fun givenDiceAttack_whenChangingCountAndType_thenSerializesAllSupportedDice() {
        val vm = create()
        for (die in DiceRoles.entries) {
            vm.onIntent(AttackFormIntent.DieChanged(die))
            for (count in listOf(1, 100)) {
                vm.onIntent(AttackFormIntent.NumberChanged(AttackNumberField.DICE_COUNT, count))
                assertTrue(vm.uiState.value.isValid)
                assertEquals("$count${die.name.lowercase()}", vm.uiState.value.toAttack().damageDice)
            }
        }
        for (count in listOf(0, 101)) {
            vm.onIntent(AttackFormIntent.NumberChanged(AttackNumberField.DICE_COUNT, count))
            assertFalse(vm.uiState.value.isValid)
        }
    }
    @Test fun givenLegacyAttack_whenEditingOtherFields_thenPreservesRangeAndParsesCyrillicDice() {
        val vm = create(Attack(attackId = 7, name = "Sword", damageDice = "К8", range = "20/60"))
        assertEquals(1, vm.uiState.value.diceCount.value)
        assertEquals(DiceRoles.D8, vm.uiState.value.die)
        assertEquals("20/60", vm.uiState.value.toAttack().range)
        vm.onIntent(AttackFormIntent.NumberChanged(AttackNumberField.RANGE, 30))
        assertEquals("30", vm.uiState.value.toAttack().range)
    }
    @Test fun givenMalformedDice_whenSwitchingToFixed_thenOnlyActiveFieldsAreValidated() {
        val vm = create(Attack(attackId = 7, name = "Sword", damageDice = "invalid"))
        assertFalse(vm.uiState.value.isValid)
        vm.onIntent(AttackFormIntent.ModeChanged(DamageMode.FIXED))
        vm.onIntent(AttackFormIntent.NumberChanged(AttackNumberField.FIXED_DAMAGE, 5))
        assertTrue(vm.uiState.value.isValid)
        vm.onIntent(AttackFormIntent.ModeChanged(DamageMode.DICE))
        assertEquals(5, vm.uiState.value.fixedDamage.value)
        assertFalse(vm.uiState.value.isValid)
    }
    @Test fun givenDraft_whenRestoredAndReopened_thenRestoresOnlySameSession() {
        val saved = SavedStateHandle()
        val vm = create(saved = saved)
        vm.onIntent(AttackFormIntent.TextChanged(AttackTextField.NAME, "Draft"))
        vm.onIntent(AttackFormIntent.UsageChanged(AttackUsage.REACTION))
        val restored = create(saved = saved)
        assertEquals("Draft", restored.uiState.value.name.text)
        assertTrue(AttackUsage.REACTION in restored.uiState.value.usages)
        restored.onIntent(AttackFormIntent.BeginSession("new", Attack(name = "New")))
        assertEquals("New", restored.uiState.value.name.text)
    }
    @Test fun givenValidAttack_whenSubmittingTwice_thenEmitsOnceAndKeepsIdentity() = runTest {
        val vm = create(Attack(attackId = 8, characterId = 3, name = " Sword ", damageDice = "1d8"))
        vm.onIntent(AttackFormIntent.Submit)
        vm.onIntent(AttackFormIntent.Submit)
        val effect = vm.effects.first() as AttackFormEffect.SaveRequested
        assertEquals(8L, effect.attack.attackId)
        assertEquals(3L, effect.attack.characterId)
        assertEquals("Sword", effect.attack.name)
        assertTrue(vm.uiState.value.submitted)
    }
}
