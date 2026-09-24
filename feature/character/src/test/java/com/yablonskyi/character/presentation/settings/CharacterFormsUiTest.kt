package com.yablonskyi.character.presentation.settings

import android.graphics.Bitmap
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.SavedStateHandle
import com.yablonskyi.character.presentation.sheet.editor.*
import com.yablonskyi.model.character.Attack
import com.yablonskyi.model.character.Character
import com.yablonskyi.model.character.DamageMode
import com.yablonskyi.ui.theme.DnDSheetTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "en-rUS-w400dp-h900dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CharacterFormsUiTest {
    @get:Rule val compose = createComposeRule()

    private fun capture(name: String) {
        val file = File("build/reports/form-screenshots/$name.png")
        file.parentFile.mkdirs()
        file.outputStream().use { compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
    private fun settings() {
        val vm = CharacterSettingsFormViewModel(SavedStateHandle())
        vm.onIntent(CharacterSettingsFormIntent.Synchronize(Character(id = 1, name = "Aria", race = "Elf", charClass = "Ranger", level = 19)))
        compose.setContent { DnDSheetTheme {
            val form by vm.uiState.collectAsState()
            CharacterSettingsScreen(form = form, onIntent = {}, onFormIntent = vm::onIntent)
        } }
        compose.onNodeWithContentDescription("Increase level").performClick().assertIsNotEnabled()
        compose.runOnIdle { assertEquals(20, vm.uiState.value.character!!.level) }
    }
    @Test fun givenSettings_whenLevelIncreased_thenShows20AndDisablesIncrement() {
        settings()
        capture("settings-portrait")
    }
    @Test @Config(qualifiers = "en-rUS-w1000dp-h700dp-land")
    fun givenWideSettings_whenRendered_thenLevelControlsRemainAvailable() {
        settings()
        capture("settings-landscape")
    }
    @Test fun givenAttack_whenSwitchingMode_thenShowsFixedDamageField() {
        val vm = AttackFormViewModel(SavedStateHandle())
        vm.onIntent(AttackFormIntent.BeginSession("a", Attack(name = "Dagger", damageDice = "1d4")))
        compose.setContent { DnDSheetTheme {
            val state by vm.uiState.collectAsState()
            UpdateAttackSheet(state, vm::onIntent)
        } }
        compose.onNodeWithText("Dice count").performScrollTo().assertIsDisplayed()
        capture("attack-dice")
        compose.runOnIdle { vm.onIntent(AttackFormIntent.ModeChanged(DamageMode.FIXED)) }
        compose.onNodeWithText("Base damage").performScrollTo().assertIsDisplayed()
        capture("attack-fixed")
    }
    @Test fun givenHealth_whenDamageApplied_thenUpdatesVisibleFields() {
        val vm = HealthFormViewModel(SavedStateHandle())
        vm.onIntent(HealthFormIntent.BeginSession("a", Character(id = 1, currentHp = 20, maxHp = 30, tempHp = 5)))
        compose.setContent { DnDSheetTheme {
            val state by vm.uiState.collectAsState()
            HealthEditSheetContent(state, vm::onIntent)
        } }
        compose.runOnIdle {
            vm.onIntent(HealthFormIntent.Changed(HealthField.AMOUNT, 8))
            vm.onIntent(HealthFormIntent.Damage)
        }
        compose.onNodeWithText("17").assertIsDisplayed()
        capture("health")
    }
}
