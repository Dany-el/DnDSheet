package com.yablonskyi.wizard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.text.TextLayoutResult
import com.yablonskyi.model.character.Ability
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.yablonskyi.wizard.viewmodel.AbilityMethod
import com.yablonskyi.wizard.viewmodel.WizardFormState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

abstract class WizardAbilitiesStepTestCases {
    @get:Rule val compose = createComposeRule()
    protected val method = mutableStateOf(AbilityMethod.STANDARD_ARRAY)
    protected val changes = mutableListOf<AbilityMethod>()

    private val restoration = StateRestorationTester(compose)

    protected fun show(fontScale: Float = 1f, rolls: List<Int> = emptyList(), assignments: Map<Ability, Int> = emptyMap()) {
        restoration.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, fontScale)) {
                Box(Modifier.width(320.dp)) {
                    MaterialTheme {
                        WizardAbilitiesStep(
                            method = method.value,
                            raceAbilityBonuses = mapOf(Ability.STR to 2),
                            standardAssignments = assignments, pendingPoolValue = null,
                            pointBuyScores = WizardFormState().pointBuyScores, pointsSpent = 0,
                            rolledResults = rolls, rollIndexAssignments = emptyMap(), pendingRollIndex = null,
                            onMethodChange = { changes += it; method.value = it },
                            onSelectPoolValue = {}, onAssignToAbility = {}, onUnassignAbility = {},
                            onIncrementPB = {}, onDecrementPB = {}, onRollAll = {}, onSelectRollIndex = {},
                        )
                    }
                }
            }
        }
    }

    @Test fun givenExternalMethodChange_whenRecomposed_thenDisplaysRequestedPageWithoutCallback() {
        show()
        compose.runOnIdle { method.value = AbilityMethod.ROLL }
        compose.onNodeWithText("Roll Abilities", ignoreCase = true).assertIsDisplayed()
        compose.runOnIdle { assertEquals(emptyList<AbilityMethod>(), changes) }
    }

    @Test fun givenPointBuy_whenRendered_thenControlsHaveAbilityLabelsAndDisabledMinimum() {
        method.value = AbilityMethod.POINT_BUY
        show()
        compose.onNodeWithContentDescription("Increase Strength").assertIsEnabled()
        compose.onNodeWithContentDescription("Decrease Strength").assertIsNotEnabled()
    }

    @Test fun givenNoPoolSelection_whenRendered_thenAssignmentIsDisabled() {
        show()
        compose.onNode(hasText("STR") and hasClickAction()).assertIsNotEnabled()
    }

    @Test fun givenTabs_whenSelectingAndSwiping_thenNotifiesOncePerSettledMethod() {
        show()
        compose.onNodeWithText("Point Buy").performClick()
        compose.onNodeWithText("Point Buy").assertIsSelected()
        compose.onNodeWithContentDescription("Increase Strength").assertIsDisplayed()
        compose.onNodeWithTag("ability_pager").performTouchInput { swipeLeft() }
        compose.onNodeWithText("Roll").assertIsSelected()
        compose.onNodeWithText("Roll Abilities").assertIsDisplayed()
        compose.runOnIdle { assertEquals(listOf(AbilityMethod.POINT_BUY, AbilityMethod.ROLL), changes) }
    }

    @Test fun givenSelectedMethod_whenRestored_thenKeepsPageWithoutDuplicateCallback() {
        show()
        compose.onNodeWithText("Point Buy").performClick()
        compose.waitForIdle()
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithText("Point Buy").assertIsSelected()
        compose.onNodeWithContentDescription("Increase Strength").assertIsDisplayed()
        compose.runOnIdle { assertEquals(listOf(AbilityMethod.POINT_BUY), changes) }
    }

    @Test fun givenNarrowScreenAndLargeText_whenPoolsRendered_thenAllValuesRemainReachable() {
        show(fontScale = 2f, rolls = listOf(18, 17, 16, 15, 14, 13))
        listOf("15", "14", "13", "12", "10", "8").forEach {
            compose.onNodeWithText(it).assertIsDisplayed()
        }
        compose.runOnIdle { method.value = AbilityMethod.ROLL }
        listOf("18", "17", "16", "15", "14", "13").forEach {
            compose.onNodeWithText(it).assertIsDisplayed()
        }
    }

    @Test fun givenAssignedAbilityAndLargeText_whenRendered_thenNameAndScoreStayOnOneLine() {
        show(fontScale = 2f, assignments = mapOf(Ability.STR to 15))
        listOf("STR", "17").forEach { text ->
            val results = mutableListOf<TextLayoutResult>()
            compose.onNodeWithText(text, useUnmergedTree = true)
                .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
            assertEquals("$text should fit without wrapping", 1, results.single().lineCount)
            val layout = results.single()
            // Grid can reuse a wider paragraph for a wrap-content Text. Check visible line bounds.
            org.junit.Assert.assertTrue("$text clips horizontally", layout.getLineRight(0) <= layout.size.width)
            org.junit.Assert.assertTrue("$text clips vertically", layout.getLineBottom(0) <= layout.size.height)
        }
    }
}
