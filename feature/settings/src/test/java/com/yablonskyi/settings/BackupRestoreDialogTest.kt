package com.yablonskyi.settings

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import com.yablonskyi.domain.repository.BackupProgress
import com.yablonskyi.domain.repository.BackupSummary
import com.yablonskyi.model.backup.BackupRecordCounts
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "en")
class BackupRestoreDialogTest {
    @get:Rule val compose = createComposeRule()

    @Test fun givenBackupScreen_whenShown_thenExplainsScopeAndExcludesPreferences() {
        val actions = mutableListOf<String>()
        compose.setContent {
            MaterialTheme {
                BackupRestoreContent(
                    isBusy = false,
                    isProcessing = false,
                    progress = BackupProgress.IDLE,
                    restoreSummary = null,
                    snackbarHostState = SnackbarHostState(),
                    onNavigateBack = { actions += "back" },
                    onCreate = { actions += "create" },
                    onSelectBackup = { actions += "restore" },
                    onCancelRestore = {},
                    onConfirmRestore = {},
                )
            }
        }

        compose.onNodeWithText(
            "Backup stores all characters (dice history, attacks), spells, races, classes.",
        ).assertIsDisplayed()
        compose.onNodeWithText(
            "Locale, theme, list/grid mode, and all other preferences are not included.",
        ).assertIsDisplayed()
        compose.onNodeWithText("Create backup").assertHeightIsAtLeast(48.dp).performClick()
        compose.onNodeWithText("Restore backup").performScrollTo().assertHeightIsAtLeast(48.dp).performClick()

        assertEquals(listOf("create", "restore"), actions)
    }

    @Test fun givenValidatedSummary_whenConfirmationShown_thenShowsCountsAndReplacementWarning() {
        var restored = false
        compose.setContent {
            MaterialTheme {
                BackupRestoreContent(
                    isBusy = false,
                    isProcessing = false,
                    progress = BackupProgress.IDLE,
                    restoreSummary = BackupSummary(
                        createdAt = Instant.parse("2026-09-21T12:00:00Z"),
                        sourceAppVersion = "1.0",
                        counts = BackupRecordCounts(2, 1, 3, 4, 2, 1, 1),
                        portraitCount = 1,
                    ),
                    snackbarHostState = SnackbarHostState(),
                    onNavigateBack = {},
                    onCreate = {},
                    onSelectBackup = {},
                    onCancelRestore = {},
                    onConfirmRestore = { restored = true },
                )
            }
        }

        compose.onNodeWithText("Characters: 2", substring = true).assertIsDisplayed()
        compose.onNodeWithText("Dice rolls: 3", substring = true).assertIsDisplayed()
        compose.onNodeWithText("Portraits: 1", substring = true).assertIsDisplayed()
        compose.onNodeWithText("Create backup").assertDoesNotExist()
        compose.onNodeWithText(
            "Restoring replaces current characters, attacks, dice history, spells, races, classes, associations, and portraits.",
        ).assertIsDisplayed()
        compose.onNodeWithText(
            "Locale, theme, list/grid mode, and all other preferences stay unchanged.",
        ).assertIsDisplayed()
        compose.onNodeWithText("Restore backup").performScrollTo().assertHeightIsAtLeast(48.dp).performClick()

        assertEquals(true, restored)
    }

    @Test fun givenValidatedSummary_whenCancelled_thenEmitsCancel() {
        var cancelled = false
        compose.setContent {
            MaterialTheme {
                BackupRestoreContent(
                    isBusy = false,
                    isProcessing = false,
                    progress = BackupProgress.IDLE,
                    restoreSummary = BackupSummary(
                        createdAt = Instant.parse("2026-09-21T12:00:00Z"),
                        sourceAppVersion = "1.0",
                        counts = BackupRecordCounts(2, 1, 3, 4, 2, 1, 1),
                        portraitCount = 1,
                    ),
                    snackbarHostState = SnackbarHostState(),
                    onNavigateBack = {},
                    onCreate = {},
                    onSelectBackup = {},
                    onCancelRestore = { cancelled = true },
                    onConfirmRestore = {},
                )
            }
        }

        compose.onNodeWithText("Cancel").performScrollTo().performClick()
        assertEquals(true, cancelled)
    }

    @Test fun givenBackupOperationInProgress_whenScreenShown_thenActionsAreDisabled() {
        compose.setContent {
            MaterialTheme {
                BackupRestoreContent(
                    isBusy = true,
                    isProcessing = true,
                    progress = BackupProgress.RESTORING,
                    restoreSummary = null,
                    snackbarHostState = SnackbarHostState(),
                    onNavigateBack = {},
                    onCreate = {},
                    onSelectBackup = {},
                    onCancelRestore = {},
                    onConfirmRestore = {},
                )
            }
        }

        compose.onNodeWithText("Create backup").assertIsNotEnabled()
        compose.onNodeWithText("Restore backup").assertIsNotEnabled()
        compose.onNodeWithText("Restoring content…").assertIsDisplayed()
    }
}
