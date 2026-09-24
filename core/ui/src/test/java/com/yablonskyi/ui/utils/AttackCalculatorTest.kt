package com.yablonskyi.ui.utils

import com.yablonskyi.model.character.*
import org.junit.Assert.*
import org.junit.Test

class AttackCalculatorTest {
    @Test fun givenNoAbility_whenFormattingDice_thenDamageIsVisible() {
        assertEquals("2d6 + 3", AttackCalculator(Character(), Attack(damageDice = "2d6", bonusToDamage = 3)).getDamageString())
    }

    @Test fun givenFixedDamage_whenCalculating_thenClampsWithoutOverflow() {
        val attack = Attack(damageMode = DamageMode.FIXED, fixedDamage = Int.MAX_VALUE, bonusToDamage = 100)
        assertEquals(Int.MAX_VALUE.toString(), AttackCalculator(Character(), attack).getDamageString())
        assertEquals("0", AttackCalculator(Character(), attack.copy(fixedDamage = 1, bonusToDamage = -10)).getDamageString())
    }

    @Test fun givenModifierPolicies_whenApplying_thenOnlyConfiguredContributionIsUsed() {
        assertEquals(3, DamageAbilityModifier.FULL.contribution(3))
        assertEquals(0, DamageAbilityModifier.NONE.contribution(-3))
        assertEquals(0, DamageAbilityModifier.NEGATIVE_ONLY.contribution(3))
        assertEquals(-3, DamageAbilityModifier.NEGATIVE_ONLY.contribution(-3))
    }
}
