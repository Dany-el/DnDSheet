package com.yablonskyi.settings.language

import android.app.Activity
import com.yablonskyi.domain.provider.AppLanguageManager
import com.yablonskyi.domain.provider.NetworkStatusProvider
import com.yablonskyi.domain.repository.LanguageDownloadRepository
import com.yablonskyi.model.language.LanguageDeliveryState
import com.yablonskyi.model.language.LanguageDownloadOperation
import com.yablonskyi.model.language.NetworkStatus
import com.yablonskyi.settings.utils.AppLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LanguagesViewModelTest {
    private val dispatcher: TestDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun givenInstalledLanguageAndNoInternet_whenSelected_thenRequestsLanguage() = runTest(dispatcher) {
        val repository = FakeLanguageDownloadRepository(installedLanguages = setOf("en"))
        val viewModel = LanguagesViewModel(
            repository = repository,
            languageManager = FakeAppLanguageManager(),
            networkStatusProvider = FakeNetworkStatusProvider(NetworkStatus.OFFLINE),
        )
        advanceUntilIdle()

        viewModel.selectLanguage(AppLanguage.ENGLISH)

        assertEquals("en" to "en", repository.lastRequest)
    }

    @Test
    fun givenMissingLanguageAndNoInternet_whenSelected_thenDoesNotStartDownload() = runTest(dispatcher) {
        val repository = FakeLanguageDownloadRepository(installedLanguages = setOf("en"))
        val viewModel = LanguagesViewModel(
            repository = repository,
            languageManager = FakeAppLanguageManager(),
            networkStatusProvider = FakeNetworkStatusProvider(NetworkStatus.OFFLINE),
        )
        advanceUntilIdle()

        viewModel.selectLanguage(AppLanguage.RUSSIAN)

        assertEquals(null, repository.lastRequest)
    }

    @Test
    fun givenSystemLanguageSelected_whenRequested_thenAppliesSystemAfterDelivery() = runTest(dispatcher) {
        val repository = FakeLanguageDownloadRepository(installedLanguages = setOf("en", "ru", "uk"))
        val viewModel = LanguagesViewModel(
            repository = repository,
            languageManager = FakeAppLanguageManager(),
            networkStatusProvider = FakeNetworkStatusProvider(NetworkStatus.ONLINE),
        )
        advanceUntilIdle()

        viewModel.selectLanguage(AppLanguage.SYSTEM)

        assertEquals("system", repository.lastRequest?.second)
    }

    @Test
    fun givenDifferentInstalledLanguage_whenApplied_thenKeepsNewLanguageSelected() = runTest(dispatcher) {
        val repository = FakeLanguageDownloadRepository(installedLanguages = setOf("en", "ru"))
        val viewModel = LanguagesViewModel(
            repository = repository,
            languageManager = FakeAppLanguageManager(),
            networkStatusProvider = FakeNetworkStatusProvider(NetworkStatus.ONLINE),
        )
        advanceUntilIdle()

        repository.emit(
            LanguageDownloadOperation.Installed(
                languageCode = "ru",
                shouldApply = true,
            ),
        )
        runCurrent()
        repository.emit(LanguageDownloadOperation.Idle)
        advanceUntilIdle()

        assertEquals("ru", viewModel.uiState.value.selectedLanguageCode)
    }

    @Test
    fun givenCompletionMissed_whenScreenResumes_thenSelectionMatchesAppliedLocale() = runTest(dispatcher) {
        val repository = FakeLanguageDownloadRepository(installedLanguages = setOf("en", "ru", "uk"))
        val languageManager = FakeAppLanguageManager()
        val viewModel = LanguagesViewModel(
            repository = repository,
            languageManager = languageManager,
            networkStatusProvider = FakeNetworkStatusProvider(NetworkStatus.ONLINE),
        )
        advanceUntilIdle()

        for (code in listOf("ru", "uk", "system", "en")) {
            // The activity applies and clears completion before this collector runs.
            repository.emit(LanguageDownloadOperation.Installed(code, shouldApply = true))
            languageManager.applyLanguage(code)
            repository.emit(LanguageDownloadOperation.Idle)
            runCurrent()

            // The same ViewModel survives the activity's locale recreation.
            viewModel.syncSelectedLanguage()

            assertEquals(code, viewModel.uiState.value.selectedLanguageCode)
        }
    }

    @Test
    fun givenDownloadMakesNoProgress_whenThresholdPasses_thenReportsSlowConnection() = runTest(dispatcher) {
        val repository = FakeLanguageDownloadRepository(installedLanguages = setOf("en"))
        val viewModel = LanguagesViewModel(
            repository = repository,
            languageManager = FakeAppLanguageManager(),
            networkStatusProvider = FakeNetworkStatusProvider(NetworkStatus.ONLINE),
        )
        advanceUntilIdle()
        val event = async { viewModel.events.first() }

        repository.emit(
            LanguageDownloadOperation.Downloading(
                languageCode = "uk",
                bytesDownloaded = 10,
                totalBytes = 100,
            ),
        )
        runCurrent()
        advanceTimeBy(15_001)

        assertEquals(LanguagesEvent.SlowConnection, event.await())
    }
}

private class FakeLanguageDownloadRepository(
    installedLanguages: Set<String>,
) : LanguageDownloadRepository {
    private val mutableState = MutableStateFlow(
        LanguageDeliveryState(installedLanguageCodes = installedLanguages),
    )
    override val state: StateFlow<LanguageDeliveryState> = mutableState.asStateFlow()
    var lastRequest: Pair<String, String>? = null
        private set

    override fun refresh() = Unit

    override fun requestLanguage(languageCode: String, applyLanguageCode: String) {
        lastRequest = languageCode to applyLanguageCode
    }

    override fun cancelDownload() {
        val code = state.value.operation.languageCode() ?: return
        mutableState.value = state.value.copy(
            operation = LanguageDownloadOperation.Canceled(code),
        )
    }

    override fun launchConfirmation(activity: Activity): Boolean = false

    override fun clearTerminalState() = Unit

    fun emit(operation: LanguageDownloadOperation) {
        mutableState.value = mutableState.value.copy(operation = operation)
    }
}

private class FakeNetworkStatusProvider(
    status: NetworkStatus,
) : NetworkStatusProvider {
    override val status: Flow<NetworkStatus> = MutableStateFlow(status)
}

private class FakeAppLanguageManager : AppLanguageManager {
    private var code = "en"
    override fun currentLanguageCode(): String = code
    override fun applyLanguage(languageCode: String) {
        code = languageCode
    }
}

private fun LanguageDownloadOperation.languageCode(): String? = when (this) {
    is LanguageDownloadOperation.Pending -> languageCode
    is LanguageDownloadOperation.RequiresConfirmation -> languageCode
    is LanguageDownloadOperation.Downloading -> languageCode
    is LanguageDownloadOperation.Installing -> languageCode
    is LanguageDownloadOperation.Canceling -> languageCode
    is LanguageDownloadOperation.Installed -> languageCode
    is LanguageDownloadOperation.Canceled -> languageCode
    is LanguageDownloadOperation.Failed -> languageCode
    LanguageDownloadOperation.Idle -> null
}
