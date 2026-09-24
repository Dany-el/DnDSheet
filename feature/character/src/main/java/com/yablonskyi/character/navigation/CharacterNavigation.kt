package com.yablonskyi.character.navigation

import kotlinx.serialization.Serializable

@Serializable
data class CharacterSheetRoute(val id: Long)

@Serializable
data class CharacterSettingsRoute(val id: Long)

@Serializable
data class DiceHistoryRoute(val characterId: Long)

@Serializable
data class NoteEditorRoute(val characterId: Long, val noteId: String? = null)
