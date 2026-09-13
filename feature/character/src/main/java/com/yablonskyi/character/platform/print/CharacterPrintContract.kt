package com.yablonskyi.character.platform.print

sealed interface CharacterPrintState {
    data object Idle : CharacterPrintState
    data class Preparing(val characterId: Long) : CharacterPrintState
    data class Launching(
        val requestId: Long,
        val characterId: Long,
        val claimed: Boolean = false,
    ) : CharacterPrintState
}

sealed interface CharacterPrintEffect {
    data class LaunchPrint(val requestId: Long, val html: String, val jobName: String) : CharacterPrintEffect
    data class Failed(val error: CharacterPrintError) : CharacterPrintEffect
    data object PrintRequestAccepted : CharacterPrintEffect
}

enum class CharacterPrintError { LOAD_FAILED, RENDER_FAILED, PRINT_LAUNCH_FAILED }

internal fun normalizePrintLanguage(code: String): String =
    code.trim().lowercase(java.util.Locale.ROOT).replace('_', '-').substringBefore('-')
        .takeIf { it == "uk" || it == "ru" } ?: "en"
