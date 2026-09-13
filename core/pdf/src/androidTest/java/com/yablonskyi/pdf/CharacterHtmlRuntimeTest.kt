package com.yablonskyi.pdf

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.yablonskyi.model.character.Character
import com.yablonskyi.model.character.CharacterSheet
import com.yablonskyi.model.character.Spell
import com.yablonskyi.pdf.html.CharacterSheetPdfGenerator
import com.yablonskyi.pdf.html.DefaultCharacterSheetHtmlRenderer
import com.yablonskyi.pdf.html.PythonHtmlTemplateRenderer
import com.yablonskyi.ui.provider.CharacterImageLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CharacterHtmlRuntimeTest {
    @Test fun givenPackagedPython_whenRendered_thenLoadsTemplatesAndAllThreeLanguages() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
        val imageLoader = object : CharacterImageLoader {
            override fun loadImageBytes(imagePath: String?): ByteArray? = null
        }
        val renderer = DefaultCharacterSheetHtmlRenderer(CharacterSheetPdfGenerator(
            imageLoader, PythonHtmlTemplateRenderer(context), Dispatchers.IO,
        ))
        val sheet = CharacterSheet(
            Character(name = "Аарон <&>", inventory = "Rope & torch"),
            listOf(Spell(name = "Magic", description = "<script>bad()</script>")),
            emptyList(),
        )
        for (language in listOf("en", "ru", "uk")) {
            val html = renderer.render(sheet, language).getOrThrow().html
            assertTrue(html.contains("lang=\"$language\""))
            assertTrue(html.contains("Аарон &lt;&amp;&gt;"))
            assertTrue(html.contains("Rope &amp; torch"))
            assertFalse(html.contains("<script>bad()</script>"))
        }
    }
}
