package com.yablonskyi.wizard

import com.yablonskyi.model.character.Ability
import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.yablonskyi.wizard.viewmodel.AbilityMethod
import java.io.File
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WizardAbilitiesDeviceTest : WizardAbilitiesStepTestCases() {
    @Test fun captureNarrowLargeTextPages() {
        show(fontScale = 1.5f, rolls = listOf(18, 17, 16, 15, 14, 13), assignments = mapOf(Ability.STR to 15))
        for (page in AbilityMethod.entries) {
            compose.runOnIdle { method.value = page }
            compose.waitForIdle()
            val directory = File(InstrumentationRegistry.getInstrumentation().targetContext.filesDir, "wizard-screenshots")
            directory.mkdirs()
            File(directory, "${page.name.lowercase()}-320dp-font150.png").outputStream().use {
                compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        }
    }
}
