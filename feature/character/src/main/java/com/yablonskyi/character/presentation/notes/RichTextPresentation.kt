package com.yablonskyi.character.presentation.notes

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.yablonskyi.model.character.RichText
import com.yablonskyi.model.character.TextFormat

internal fun RichText.toAnnotatedString(): AnnotatedString = AnnotatedString.Builder(plainText).apply {
    for (span in spans) {
        addStyle(
            when (span.format) {
                TextFormat.BOLD -> SpanStyle(fontWeight = FontWeight.Bold)
                TextFormat.ITALIC -> SpanStyle(fontStyle = FontStyle.Italic)
                TextFormat.UNDERLINE -> SpanStyle(textDecoration = TextDecoration.Underline)
                TextFormat.STRIKETHROUGH -> SpanStyle(textDecoration = TextDecoration.LineThrough)
            },
            span.start,
            span.endExclusive,
        )
    }
}.toAnnotatedString()
