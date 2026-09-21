package com.yablonskyi.settings.language

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import com.yablonskyi.model.language.LanguageDownloadFailure
import com.yablonskyi.model.language.LanguageDownloadOperation
import com.yablonskyi.settings.R
import com.yablonskyi.settings.utils.AppLanguage
import com.yablonskyi.ui.theme.Dimens

@Composable
fun LanguagesRoute(
    onNavigateBack: () -> Unit,
    viewModel: LanguagesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val activity = LocalActivity.current as Activity

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.syncSelectedLanguage()
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            snackbarHostState.showSnackbar(
                message = when (event) {
                    LanguagesEvent.SlowConnection -> activity.getString(R.string.language_slow_connection)
                    LanguagesEvent.NoInternet -> activity.getString(R.string.language_no_internet)
                },
                duration = SnackbarDuration.Long,
            )
        }
    }
    LaunchedEffect(state.deliveryState.operation) {
        if (state.deliveryState.operation is LanguageDownloadOperation.RequiresConfirmation) {
            viewModel.launchConfirmation(activity)
        }
    }

    LanguagesScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        onSelectLanguage = viewModel::selectLanguage,
        onCancel = viewModel::cancel,
        onRetry = viewModel::retry,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguagesScreen(
    state: LanguagesUiState,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    onSelectLanguage: (AppLanguage) -> Unit,
    onCancel: () -> Unit,
    onRetry: () -> Unit,
) {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val isWideScreen =
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.languages_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        textAlign = if (isWideScreen) TextAlign.Center else TextAlign.Left,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.languages_navigate_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(Dimens.TopBar.Elevation),
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                        Dimens.TopBar.Elevation
                    ),
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = stringResource(R.string.language_error_icon),
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(data.visuals.message)
                    }
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .widthIn(max = 840.dp),
        ) {
            AppLanguage.entries.forEachIndexed { index, language ->
                LanguageRow(
                    language = language,
                    selected = state.selectedLanguageCode == language.code,
                    installed = language == AppLanguage.SYSTEM ||
                            language.code in state.deliveryState.installedLanguageCodes,
                    enabled = !state.isDownloadActive,
                    onClick = { onSelectLanguage(language) },
                )
                if (index != AppLanguage.entries.lastIndex) HorizontalDivider()
            }

            Spacer(Modifier.height(24.dp))
            DownloadStatus(
                operation = state.deliveryState.operation,
                onCancel = onCancel,
                onRetry = onRetry,
            )
        }
    }
}

@Composable
private fun LanguageRow(
    language: AppLanguage,
    selected: Boolean,
    installed: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, enabled = enabled, onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(stringResource(language.label), style = MaterialTheme.typography.bodyLarge)
            if (installed) {
                Text(
                    stringResource(R.string.language_installed),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (selected) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = stringResource(R.string.language_selected),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun DownloadStatus(
    operation: LanguageDownloadOperation,
    onCancel: () -> Unit,
    onRetry: () -> Unit,
) {
    val languageName = operation.languageCodeOrNull()?.let { code ->
        AppLanguage.entries.firstOrNull { it.code == code }?.let { stringResource(it.label) }
    }.orEmpty()

    when (operation) {
        is LanguageDownloadOperation.Pending,
        is LanguageDownloadOperation.RequiresConfirmation -> DownloadPanel(
            message = stringResource(R.string.language_preparing, languageName),
            onCancel = onCancel,
        )

        is LanguageDownloadOperation.Downloading -> {
            val determinate = operation.totalBytes > 0
            val progress = if (determinate) {
                (operation.bytesDownloaded.toFloat() / operation.totalBytes).coerceIn(0f, 1f)
            } else 0f
            DownloadPanel(
                message = if (determinate) {
                    stringResource(
                        R.string.language_downloading_percent,
                        languageName,
                        (progress * 100).toInt()
                    )
                } else {
                    stringResource(R.string.language_downloading, languageName)
                },
                progress = progress.takeIf { determinate },
                onCancel = onCancel,
            )
        }

        is LanguageDownloadOperation.Installing -> DownloadPanel(
            message = stringResource(R.string.language_installing, languageName),
            onCancel = onCancel,
        )

        is LanguageDownloadOperation.Canceling -> DownloadPanel(
            message = stringResource(R.string.language_canceling),
            showCancel = false,
            onCancel = onCancel,
        )

        is LanguageDownloadOperation.Failed -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = stringResource(operation.reason.messageResource()),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onRetry) { Text(stringResource(R.string.language_retry)) }
            }
        }

        is LanguageDownloadOperation.Canceled -> Text(
            stringResource(R.string.language_download_canceled),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        is LanguageDownloadOperation.Idle,
        is LanguageDownloadOperation.Installed -> Unit
    }
}

@Composable
private fun DownloadPanel(
    message: String,
    progress: Float? = null,
    showCancel: Boolean = true,
    onCancel: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("language_download_panel"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(message)
        if (progress == null) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        } else {
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
        }
        if (showCancel) {
            TextButton(onClick = onCancel, modifier = Modifier.align(Alignment.End)) {
                Text(stringResource(R.string.language_cancel))
            }
        }
    }
}

private fun LanguageDownloadOperation.languageCodeOrNull(): String? = when (this) {
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

private fun LanguageDownloadFailure.messageResource(): Int = when (this) {
    LanguageDownloadFailure.NETWORK -> R.string.language_network_error
    LanguageDownloadFailure.STORAGE -> R.string.language_storage_error
    LanguageDownloadFailure.PLAY_UNAVAILABLE -> R.string.language_play_unavailable
    LanguageDownloadFailure.APP_NOT_OWNED -> R.string.language_app_not_owned
    LanguageDownloadFailure.UNKNOWN -> R.string.language_unknown_error
}