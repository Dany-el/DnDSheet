package com.yablonskyi.pdf

import androidx.activity.ComponentActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.yablonskyi.model.character.Character
import com.yablonskyi.model.character.CharacterSheet
import com.yablonskyi.pdf.html.CharacterSheetPdfGenerator
import com.yablonskyi.pdf.html.DefaultCharacterSheetHtmlRenderer
import com.yablonskyi.pdf.html.HtmlToPdfConverter
import com.yablonskyi.pdf.html.PythonHtmlTemplateRenderer
import com.yablonskyi.ui.provider.CharacterImageLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

class PrintHostActivity : ComponentActivity()

@RunWith(AndroidJUnit4::class)
class CharacterPrintRuntimeTest {
    @Test fun givenRenderedSheet_whenPrinted_thenRealWebViewSubmitsToAndroidPrintManager() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
        val renderer = DefaultCharacterSheetHtmlRenderer(CharacterSheetPdfGenerator(
            object : CharacterImageLoader {
                override fun loadImageBytes(imagePath: String?): ByteArray? = null
            }, PythonHtmlTemplateRenderer(context), Dispatchers.IO,
        ))
        val document = renderer.render(
            CharacterSheet(Character(name = "Print integration"), emptyList(), emptyList()), "uk",
        ).getOrThrow()
        ActivityScenario.launch(PrintHostActivity::class.java).use { scenario ->
            lateinit var activity: PrintHostActivity
            scenario.onActivity { activity = it }
            val result = HtmlToPdfConverter().print(activity, document.html, document.jobName)
            assertTrue(result.exceptionOrNull()?.stackTraceToString(), result.isSuccess)
        }
    }
}
