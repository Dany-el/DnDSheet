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

@Serializable
object CharacterCreationWizardRoute

@Serializable
sealed interface CompendiumRoute{
    @Serializable
    data object Home: CompendiumRoute
    @Serializable
    data object RacesScreen: CompendiumRoute
    @Serializable
    data class RaceDetailsScreen(val raceId: String = ""): CompendiumRoute
    @Serializable
    data class RaceUpdateScreen(val raceId: String = ""): CompendiumRoute
    @Serializable
    data class RaceCreateScreen(val raceId: String = ""): CompendiumRoute
    @Serializable
    data object ClassesScreen: CompendiumRoute
    @Serializable
    data class ClassDetailsScreen(val classId: String = ""): CompendiumRoute
    @Serializable
    data class ClassUpdateScreen(val classId: String = ""): CompendiumRoute
    @Serializable
    data class ClassCreateScreen(val classId: String = ""): CompendiumRoute
    @Serializable
    data object SpellsLibrary: CompendiumRoute
}