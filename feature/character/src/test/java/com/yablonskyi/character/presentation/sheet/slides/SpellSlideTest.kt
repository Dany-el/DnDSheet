package com.yablonskyi.character.presentation.sheet.slides

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Density
import com.yablonskyi.model.character.Spell
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "en")
class SpellSlideTest {
    @get:Rule val compose = createComposeRule()

    @Test
    @Config(qualifiers = "uk")
    fun givenNarrowWidthAndLargeText_whenStatsRendered_thenValuesRemainVisible() {
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, 2f)) {
                MaterialTheme {
                    SpellCastingRow(
                        savingThrow = 13,
                        attackBonus = 5,
                        onSpellAttackRoll = {},
                        onNavigate = {},
                        modifier = Modifier.width(320.dp),
                    )
                }
            }
        }

        compose.onNodeWithText("13").assertIsDisplayed()
        compose.onNodeWithText("+5").assertIsDisplayed()
    }

    @Test
    fun givenSpellCard_whenCastTapped_thenCastsOnceWithoutOpeningDetails() {
        var casts = 0
        var details = 0
        compose.setContent {
            MaterialTheme {
                SpellCard(
                    spell = Spell(name = "Fire Bolt"),
                    shape = RoundedCornerShape(16.dp),
                    spellSaveDC = 13,
                    onCastSpell = { casts++ },
                    onSpellClick = { details++ },
                )
            }
        }

        compose.onNodeWithContentDescription("Cast Fire Bolt").performClick()
        assertEquals(1, casts)
        assertEquals(0, details)

        compose.onNodeWithText("Fire Bolt").performClick()
        assertEquals(1, casts)
        assertEquals(1, details)
    }
}
