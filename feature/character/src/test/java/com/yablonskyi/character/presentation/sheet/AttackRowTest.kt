package com.yablonskyi.character.presentation.sheet

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.yablonskyi.character.presentation.sheet.model.AttackUiModel
import com.yablonskyi.character.presentation.sheet.slides.AttackRow
import com.yablonskyi.model.character.Attack
import com.yablonskyi.model.character.Character
import com.yablonskyi.model.character.DamageMode
import com.yablonskyi.ui.theme.DnDSheetTheme
import com.yablonskyi.ui.utils.AttackCalculator
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "en-rUS-w400dp-h800dp")
class AttackRowTest {
    @get:Rule val compose = createComposeRule()
    @Test fun givenRow_whenLongPressedOnRollControl_thenRevealsDescriptionWithoutRolling() {
        var rolls = 0
        var edits = 0
        val attack = Attack(attackId = 1, name = "Sword", damageDice = "1d8", notes = "A long description\nSecond line")
        compose.setContent { DnDSheetTheme {
            AttackRow(AttackUiModel(1, "Sword", "+2", "1d8", AttackCalculator(Character(), attack)),
                { rolls++ }, { rolls++ }, { edits++ })
        } }
        compose.onNodeWithText("1d8").performTouchInput { longClick() }
        compose.onNodeWithText(attack.notes).assertIsDisplayed()
        compose.runOnIdle { assertEquals(0, rolls); assertEquals(0, edits) }
        compose.onNodeWithText("1d8").performClick()
        compose.runOnIdle { assertEquals(1, rolls) }
        compose.onNodeWithText("Sword").performTouchInput { longClick() }
        compose.onNodeWithText(attack.notes).assertDoesNotExist()
    }
    @Test fun givenFixedDamage_whenLongPressed_thenShowsEmptyDescriptionWithoutRoll() {
        var rolls = 0
        val attack = Attack(attackId = 1, name = "Strike", damageMode = DamageMode.FIXED, fixedDamage = 5)
        compose.setContent { DnDSheetTheme {
            AttackRow(AttackUiModel(1, "Strike", "+0", "5", AttackCalculator(Character(), attack)), {}, { rolls++ }, {})
        } }
        compose.onNodeWithText("5").performTouchInput { longClick() }
        compose.onNodeWithText("No description").assertIsDisplayed()
        compose.runOnIdle { assertEquals(0, rolls) }
    }
}
