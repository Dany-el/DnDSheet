package com.yablonskyi.character.platform.print

import com.yablonskyi.character.presentation.list.*
import com.yablonskyi.character.platform.print.CharacterPrintState

import com.yablonskyi.character.presentation.list.CharacterSheetsScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.yablonskyi.model.character.Character
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "uk")
class CharacterPrintUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun givenUkrainianLocale_whenPrintMenuTapped_thenDispatchesIdAndLanguage() {
        val requests = mutableListOf<Pair<Long, String>>()
        compose.setContent {
            MaterialTheme {
                SharedTransitionLayout {
                    AnimatedVisibility(true) {
                        CharacterSheetsScreen(
                            uiState = CharacterListState(
                                allCharacters = listOf(Character(id = 7, name = "Hero")),
                                isLoading = false,
                            ),
                            onIntent = { if (it is CharacterListIntent.PrintClicked) requests += it.id to it.language },
                            animatedVisibilityScope = this,
                        )
                    }
                }
            }
        }
        compose.onNodeWithContentDescription("Options").performClick()
        compose.onNodeWithText("Друк / зберегти як PDF").performClick()
        assertEquals(listOf(7L to "uk"), requests)
    }

    @Test fun givenPrintInProgress_whenRendered_thenShowsLoadingDialog() {
        compose.setContent {
            MaterialTheme {
                SharedTransitionLayout {
                    AnimatedVisibility(true) {
                        CharacterSheetsScreen(
                            uiState = CharacterListState(
                                printState = CharacterPrintState.Preparing(7),
                                isLoading = false,
                            ),
                            onIntent = {},
                            animatedVisibilityScope = this,
                        )
                    }
                }
            }
        }
        compose.onNode(isDialog()).assertExists()
    }
}
