package com.yablonskyi.compendium

import androidx.navigation.NavHostController

fun navigateToCompendiumRaces(navController: NavHostController) =
    navController.navigate(CompendiumRacesRoute)

fun navigateToCompendiumRaceDetails(navController: NavHostController, raceId: String) =
    navController.navigate(CompendiumRaceDetailsRoute(raceId))

fun navigateToCompendiumRaceCreate(navController: NavHostController) =
    navController.navigate(CompendiumRaceCreateRoute)

fun navigateToCompendiumRaceUpdate(navController: NavHostController, raceId: String? = null) =
    navController.navigate(CompendiumRaceUpdateRoute(raceId))

fun navigateToCompendiumClasses(navController: NavHostController) =
    navController.navigate(CompendiumClassesRoute)

fun navigateToCompendiumClassDetails(navController: NavHostController, classId: String) =
    navController.navigate(CompendiumClassDetailsRoute(classId))

fun navigateToCompendiumClassCreate(navController: NavHostController) =
    navController.navigate(CompendiumClassCreateRoute)

fun navigateToCompendiumClassUpdate(navController: NavHostController, classId: String? = null) =
    navController.navigate(CompendiumClassUpdateRoute(classId))

fun navigateToCompendiumSpellsLibrary(navController: NavHostController) =
    navController.navigate(CompendiumSpellsLibraryRoute)

fun navigateToCompendiumSpellUpdate(navController: NavHostController, spellId: Long = 0L) =
    navController.navigate(CompendiumSpellUpdateRoute(spellId))