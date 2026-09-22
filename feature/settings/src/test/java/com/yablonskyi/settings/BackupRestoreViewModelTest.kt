package com.yablonskyi.settings

import com.yablonskyi.domain.repository.BackupError
import com.yablonskyi.domain.repository.BackupProgress
import com.yablonskyi.domain.repository.BackupResult
import com.yablonskyi.domain.repository.BackupSummary
import com.yablonskyi.domain.repository.FullBackupRepository
import com.yablonskyi.domain.repository.PreparedRestore
import com.yablonskyi.model.backup.BackupRecordCounts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.Instant
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class BackupRestoreViewModelTest {
    private val dispatcher: TestDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test fun givenIdleState_whenCreateRequested_thenEmitsDatedPickerAndCancellationIsNoOp() = runTest(dispatcher) {
        val viewModel = BackupRestoreViewModel(FakeFullBackupRepository())
        val effect = async { viewModel.events.first() }

        viewModel.requestCreateBackup()
        runCurrent()

        val create = effect.await() as BackupRestoreEvent.CreateDocument
        assertTrue(create.fileName.matches(Regex("dnd-sheet_\\d{4}-\\d{2}-\\d{2}\\.zip")))
        assertEquals(BackupPicker.CREATE, viewModel.uiState.value.picker)
        assertTrue(viewModel.uiState.value.isBusy)

        viewModel.requestRestoreBackup()
        viewModel.pickerCancelled(BackupPicker.CREATE)
        runCurrent()

        assertFalse(viewModel.uiState.value.isBusy)
        assertNull(viewModel.uiState.value.restoreSummary)
    }

    @Test fun givenDestinationSelected_whenBackupSucceeds_thenReportsSuccess() = runTest(dispatcher) {
        val repository = FakeFullBackupRepository()
        val viewModel = BackupRestoreViewModel(repository)
        viewModel.requestCreateBackup()
        runCurrent()
        val message = async {
            viewModel.events.first { it is BackupRestoreEvent.Message } as BackupRestoreEvent.Message
        }

        viewModel.createBackup({ ByteArrayOutputStream() }, {})
        advanceUntilIdle()

        assertEquals(1, repository.createCalls)
        assertEquals(BackupMessage.BACKUP_CREATED, message.await().message)
        assertFalse(viewModel.uiState.value.isBusy)
    }

    @Test fun givenValidatedBackup_whenConfirmed_thenUsesSingleTokenAndReportsSuccess() = runTest(dispatcher) {
        val repository = FakeFullBackupRepository()
        val viewModel = BackupRestoreViewModel(repository)
        viewModel.requestRestoreBackup()
        runCurrent()

        viewModel.prepareRestore { ByteArrayInputStream(byteArrayOf(1)) }
        advanceUntilIdle()

        assertEquals(repository.summary, viewModel.uiState.value.restoreSummary)
        val completion = async { viewModel.events.first { it is BackupRestoreEvent.RestoreCompleted } }

        viewModel.confirmRestore()
        viewModel.confirmRestore()
        advanceUntilIdle()

        assertEquals(listOf("token"), repository.restoreTokens)
        assertEquals(BackupRestoreEvent.RestoreCompleted, completion.await())
        assertNull(viewModel.uiState.value.restoreSummary)
    }

    @Test fun givenValidatedBackup_whenConfirmationDismissed_thenDiscardsPreparedToken() = runTest(dispatcher) {
        val repository = FakeFullBackupRepository()
        val viewModel = BackupRestoreViewModel(repository)
        viewModel.requestRestoreBackup()
        viewModel.prepareRestore { ByteArrayInputStream(byteArrayOf(1)) }
        advanceUntilIdle()

        viewModel.dismissConfirmation()
        advanceUntilIdle()

        assertEquals(listOf("token"), repository.discardedTokens)
        assertNull(viewModel.uiState.value.restoreSummary)
        assertTrue(repository.restoreTokens.isEmpty())
    }

    @Test fun givenInvalidArchive_whenPreparationFails_thenShowsTypedErrorWithoutConfirmation() = runTest(dispatcher) {
        val repository = FakeFullBackupRepository().apply {
            prepareResult = BackupResult.Failure(BackupError.INVALID_ARCHIVE)
        }
        val viewModel = BackupRestoreViewModel(repository)
        viewModel.requestRestoreBackup()
        runCurrent()
        val message = async {
            viewModel.events.first { it is BackupRestoreEvent.Message } as BackupRestoreEvent.Message
        }

        viewModel.prepareRestore { ByteArrayInputStream(byteArrayOf(1)) }
        advanceUntilIdle()

        assertEquals(BackupMessage.INVALID_ARCHIVE, message.await().message)
        assertNull(viewModel.uiState.value.restoreSummary)
    }

    @Test fun givenDate_whenFilenameBuilt_thenUsesLocaleIndependentIsoDate() {
        assertEquals("dnd-sheet_2026-09-22.zip", defaultBackupFileName(LocalDate.of(2026, 9, 22)))
    }
}

private class FakeFullBackupRepository : FullBackupRepository {
    override val progress: StateFlow<BackupProgress> = MutableStateFlow(BackupProgress.IDLE)
    val summary = BackupSummary(
        createdAt = Instant.parse("2026-09-21T12:00:00Z"),
        sourceAppVersion = "1.0",
        counts = BackupRecordCounts(2, 1, 3, 4, 2, 1, 1),
        portraitCount = 1,
    )
    var prepareResult: BackupResult<PreparedRestore> = BackupResult.Success(
        PreparedRestore("token", summary),
    )
    var createCalls = 0
    val restoreTokens = mutableListOf<String>()
    val discardedTokens = mutableListOf<String>()

    override suspend fun createBackup(
        openDestination: () -> java.io.OutputStream,
        removePartialDestination: () -> Unit,
    ): BackupResult<Unit> {
        createCalls++
        openDestination().close()
        return BackupResult.Success(Unit)
    }

    override suspend fun prepareRestore(openSource: () -> java.io.InputStream): BackupResult<PreparedRestore> {
        openSource().close()
        return prepareResult
    }

    override suspend fun restore(token: String): BackupResult<Unit> {
        restoreTokens += token
        return BackupResult.Success(Unit)
    }

    override suspend fun discardPreparedRestore(token: String) {
        discardedTokens += token
    }

    override suspend fun recover(): BackupResult<Unit> = BackupResult.Success(Unit)
}
