package com.yablonskyi.pdf.html

import com.yablonskyi.domain.CharacterSheetHtmlRenderer
import com.yablonskyi.domain.RenderedCharacterSheet
import com.yablonskyi.model.character.CharacterSheet
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class DefaultCharacterSheetHtmlRenderer @Inject constructor(
    private val generator: CharacterSheetPdfGenerator,
) : CharacterSheetHtmlRenderer {
    override suspend fun render(sheet: CharacterSheet, languageCode: String): Result<RenderedCharacterSheet> =
        try {
            val html = generator.generateHtml(sheet, languageCode)
            check(html.isNotBlank()) { "The character sheet renderer returned an empty document" }
            Result.success(RenderedCharacterSheet(html, safePrintJobName(sheet.character.name)))
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            Result.failure(error)
        }
}

internal fun safePrintJobName(name: String): String =
    name.map { if (it.isISOControl() || it in "\\/:*?\"<>|") '_' else it }
        .joinToString("").trim().trim('.').take(120).ifBlank { "character_sheet" }
