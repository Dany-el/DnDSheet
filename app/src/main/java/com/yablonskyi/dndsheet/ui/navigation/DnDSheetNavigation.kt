package com.yablonskyi.dndsheet.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

@Serializable
data class CharacterSheetRoute(val id: Long)

@Serializable
object ListOfCharactersRoute

@Serializable
data class UpdateSpellRoute(val spellId: Long)

@Serializable
data class SpellLibraryRoute(
    val characterId: Long = -1L
)

@Serializable
object GlobalSpellLibraryRoute

@Serializable
data class CharacterSettingsRoute(val characterId: Long)

@Serializable
object AppSettingsRoute

data class BottomNavItem<T : Any>(
    @StringRes val name: Int,
    val route: T,
    val icon: ImageVector
)