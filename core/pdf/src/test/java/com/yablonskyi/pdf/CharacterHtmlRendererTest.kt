package com.yablonskyi.pdf

import com.yablonskyi.model.character.Character
import com.yablonskyi.model.character.CharacterSheet
import com.yablonskyi.pdf.html.CharacterSheetPdfGenerator
import com.yablonskyi.pdf.html.DefaultCharacterSheetHtmlRenderer
import com.yablonskyi.pdf.html.HtmlTemplateRenderer
import com.yablonskyi.pdf.html.safePrintJobName
import com.yablonskyi.ui.provider.CharacterImageLoader
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class CharacterHtmlRendererTest {
    private val imageLoader = object : CharacterImageLoader {
        override fun loadImageBytes(imagePath: String?): ByteArray? = null
    }
    private val sheet = CharacterSheet(Character(name = "Hero:/"), emptyList(), emptyList())

    @Test fun givenSheet_whenRendered_thenForwardsDataLanguageAndHtmlUnchanged() = runTest {
        val requests = mutableListOf<String>()
        val generator = CharacterSheetPdfGenerator(imageLoader, HtmlTemplateRenderer { data, language ->
            assertEquals("Hero:/", (data["character"] as Map<*, *>)["name"])
            requests += language
            "<html>Українська &amp; Русский</html>"
        }, StandardTestDispatcher(testScheduler))
        val renderer = DefaultCharacterSheetHtmlRenderer(generator)
        for (language in listOf("uk", "ru", "en")) {
            val result = renderer.render(sheet, language).getOrThrow()
            assertEquals("<html>Українська &amp; Русский</html>", result.html)
            assertEquals("Hero__", result.jobName)
        }
        assertEquals(listOf("uk", "ru", "en"), requests)
    }

    @Test fun givenRendererFailure_whenRendered_thenReturnsFailureButRethrowsCancellation() = runTest {
        var failure: Exception = IllegalStateException("Renderer unavailable")
        val renderer = DefaultCharacterSheetHtmlRenderer(CharacterSheetPdfGenerator(
            imageLoader, HtmlTemplateRenderer { _, _ -> throw failure }, StandardTestDispatcher(testScheduler),
        ))
        val actualFailure = renderer.render(sheet, "en").exceptionOrNull()
        assertEquals(failure::class, actualFailure!!::class)
        assertEquals(failure.message, actualFailure.message)
        failure = CancellationException("Cancelled")
        try {
            renderer.render(sheet, "en")
            fail("Cancellation must escape the Result boundary")
        } catch (cancelled: CancellationException) {
            assertEquals(failure.message, cancelled.message)
        }
    }

    @Test fun givenEmptyHtml_whenRendered_thenRejectsDocument() = runTest {
        val renderer = DefaultCharacterSheetHtmlRenderer(CharacterSheetPdfGenerator(
            imageLoader, HtmlTemplateRenderer { _, _ -> " " }, StandardTestDispatcher(testScheduler),
        ))
        assertTrue(renderer.render(sheet, "en").isFailure)
    }

    @Test fun givenUnsafeOrBlankName_whenSanitized_thenHasSafeBoundedFallback() {
        assertEquals("character_sheet", safePrintJobName("  ...  "))
        assertEquals("Аарон д'Тутам", safePrintJobName("Аарон д'Тутам"))
        assertEquals("A_B_C_", safePrintJobName("A\nB/C?"))
        assertEquals(120, safePrintJobName("a".repeat(200)).length)
    }
}
