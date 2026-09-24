package com.yablonskyi.model.character

import kotlinx.serialization.Serializable

@Serializable
data class RichText(
    val version: Int = VERSION,
    val plainText: String = "",
    val spans: List<TextSpan> = emptyList(),
) {
    fun validate() {
        require(version == VERSION) { "Unsupported rich-text version: $version" }
        for (span in spans) {
            require(span.start >= 0 && span.start < span.endExclusive && span.endExclusive <= plainText.length) {
                "Invalid text span: $span"
            }
            require(isBoundary(span.start) && isBoundary(span.endExclusive)) {
                "Text span splits a surrogate pair: $span"
            }
        }
    }

    /** Canonical form for editor output; import and storage boundaries must call [validate]. */
    fun normalized(): RichText {
        require(version == VERSION) { "Unsupported rich-text version: $version" }
        for (span in spans) {
            require(span.start in 0..plainText.length && span.endExclusive in 0..plainText.length &&
                span.start <= span.endExclusive && isBoundary(span.start) && isBoundary(span.endExclusive)) {
                "Invalid editor text span: $span"
            }
        }
        val merged = buildList<TextSpan> {
            for (format in TextFormat.entries) {
                val ordered = spans.filter { it.format == format && it.start < it.endExclusive }.sortedWith(
                    compareBy<TextSpan> { it.start }.thenBy { it.endExclusive },
                )
                for (span in ordered) {
                    val last = lastOrNull()
                    if (last != null && last.format == format && span.start <= last.endExclusive) {
                        set(lastIndex, last.copy(endExclusive = maxOf(last.endExclusive, span.endExclusive)))
                    } else {
                        add(span)
                    }
                }
            }
        }.sortedWith(compareBy<TextSpan> { it.start }.thenBy { it.endExclusive }.thenBy { it.format.ordinal })
        return copy(spans = merged)
    }

    fun isBoundary(offset: Int): Boolean = offset in 0..plainText.length &&
        !(offset > 0 && offset < plainText.length &&
            plainText[offset - 1].isHighSurrogate() && plainText[offset].isLowSurrogate())

    companion object {
        const val VERSION = 1
    }
}

@Serializable
data class TextSpan(
    val start: Int,
    val endExclusive: Int,
    val format: TextFormat,
)

@Serializable
enum class TextFormat { BOLD, ITALIC, UNDERLINE, STRIKETHROUGH }
