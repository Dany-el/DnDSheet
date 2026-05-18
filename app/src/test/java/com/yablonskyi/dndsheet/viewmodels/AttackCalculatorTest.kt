package com.yablonskyi.dndsheet.viewmodels

import com.yablonskyi.dndsheet.data.model.character.Character
import com.yablonskyi.dndsheet.data.model.character.Ability
import com.yablonskyi.dndsheet.data.model.character.AbilityBlock
import com.yablonskyi.dndsheet.data.model.character.Attack
import com.yablonskyi.dndsheet.data.model.character.AttackType
import com.yablonskyi.dndsheet.data.model.character.DamageType
import com.yablonskyi.dndsheet.ui.attack.AttackCalculator
import junit.framework.TestCase.assertEquals
import org.junit.Test


class AttackCalculatorTest {

    private fun character(str: Int = 16, level: Int = 1) = Character(
        name = "Fighter",
        level = level,
        abilityBlock = AbilityBlock(strength = str)
    )

    private fun attack(
        ability: Ability = Ability.STR,
        proficient: Boolean = true,
        bonusToHit: Int = 0,
        bonusToDamage: Int = 0,
        damageDice: String = "1d8"
    ) = Attack(
        name = "Longsword",
        attackType = AttackType.MELEE_ATTACK,
        ability = ability,
        isProficient = proficient,
        bonusToHit = bonusToHit,
        bonusToDamage = bonusToDamage,
        damageDice = damageDice,
        damageType = DamageType.SLASHING
    )

    @Test
    fun `бонус до влучання з майстерністю дорівнює модифікатору плюс бонус майстерності`() {
        val calc = AttackCalculator(character(str = 16, level = 1), attack(proficient = true))
        // mod 3 + prof 2 = 5
        assertEquals(5, calc.getToHitModifier())
    }

    @Test
    fun `бонус до влучання без майстерності дорівнює лише модифікатору`() {
        val calc = AttackCalculator(character(str = 16, level = 1), attack(proficient = false))
        assertEquals(3, calc.getToHitModifier())
    }

    @Test
    fun `бонус предмету враховується у влученні`() {
        val calc = AttackCalculator(character(str = 16, level = 1), attack(proficient = true, bonusToHit = 2))
        // 3 + 2 + 2 = 7
        assertEquals(7, calc.getToHitModifier())
    }

    @Test
    fun `рядок шкоди містить кубик та позитивний бонус`() {
        val calc = AttackCalculator(character(str = 16), attack(damageDice = "1d8"))
        assertEquals("1d8 + 3", calc.getDamageString())
    }

    @Test
    fun `рядок шкоди містить від'ємний модифікатор`() {
        val calc = AttackCalculator(character(str = 6), attack(damageDice = "1d6"))
        assertEquals("1d6 - 2", calc.getDamageString())
    }

    @Test
    fun `нульовий модифікатор відображається як плюс 0`() {
        val calc = AttackCalculator(character(str = 10), attack(damageDice = "1d4", proficient = false))
        assertEquals("1d4 + 0", calc.getDamageString())
    }

    @Test
    fun `здібність NONE повертає порожній рядок шкоди`() {
        val noneAttack = attack(ability = Ability.NONE)
        val calc = AttackCalculator(character(), noneAttack)
        assertEquals("", calc.getDamageString())
    }

    @Test
    fun `бонус до влучання на вищих рівнях враховує збільшений бонус майстерності`() {
        val calc = AttackCalculator(character(str = 16, level = 9), attack(proficient = true))
        // mod 3 + prof 4 = 7
        assertEquals(7, calc.getToHitModifier())
    }
}