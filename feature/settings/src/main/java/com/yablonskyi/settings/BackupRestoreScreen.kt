package com.yablonskyi.settings

import android.content.ActivityNotFoundException
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import com.yablonskyi.domain.repository.BackupProgress
import com.yablonskyi.domain.repository.BackupSummary
import com.yablonskyi.ui.theme.Dimens
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import com.yablonskyi.ui.R as UiR

@Composable
fun BackupRestoreScreen(
    onNavigateBack: () -> Unit,
    viewModel: BackupRestoreViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val resources = LocalResources.current
    val snackbarHostState = remember { SnackbarHostState() }
    val createDocument = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip"),
    ) { uri ->
        if (uri == null) {
            viewModel.pickerCancelled(BackupPicker.CREATE)
        } else {
            viewModel.createBackup(
                openDestination = {
                    context.contentResolver.openOutputStream(uri, "w")
                        ?: throw java.io.IOException("Cannot open backup destination")
                },
                removePartialDestination = {
                    context.contentResolver.delete(uri, null, null)
                },
            )
        }
    }
    val openDocument = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) {
            viewModel.pickerCancelled(BackupPicker.OPEN)
        } else {
            viewModel.prepareRestore {
                context.contentResolver.openInputStream(uri)
                    ?: throw java.io.IOException("Cannot open backup source")
            }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is BackupRestoreEvent.CreateDocument -> try {
                    createDocument.launch(event.fileName)
                } catch (_: ActivityNotFoundException) {
                    viewModel.pickerFailed(BackupPicker.CREATE)
                }

                BackupRestoreEvent.OpenDocument -> try {
                    openDocument.launch(arrayOf("application/zip", "application/octet-stream"))
                } catch (_: ActivityNotFoundException) {
                    viewModel.pickerFailed(BackupPicker.OPEN)
                }

                BackupRestoreEvent.RestoreCompleted -> onNavigateBack()

                is BackupRestoreEvent.Message -> snackbarHostState.showSnackbar(
                    resources.getString(event.message.stringResource()),
                )
            }
        }
    }

    BackHandler(enabled = state.isProcessing || state.restoreSummary != null) {
        if (!state.isProcessing) viewModel.dismissConfirmation()
    }
    BackupRestoreContent(
        isBusy = state.isBusy,
        isProcessing = state.isProcessing,
        progress = state.progress,
        restoreSummary = state.restoreSummary,
        snackbarHostState = snackbarHostState,
        onNavigateBack = {
            if (state.restoreSummary != null) viewModel.dismissConfirmation()
            else onNavigateBack()
        },
        onCreate = viewModel::requestCreateBackup,
        onSelectBackup = viewModel::requestRestoreBackup,
        onCancelRestore = viewModel::dismissConfirmation,
        onConfirmRestore = viewModel::confirmRestore,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BackupRestoreContent(
    isBusy: Boolean,
    isProcessing: Boolean,
    progress: BackupProgress,
    restoreSummary: BackupSummary?,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    onCreate: () -> Unit,
    onSelectBackup: () -> Unit,
    onCancelRestore: () -> Unit,
    onConfirmRestore: () -> Unit,
) {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val isWideScreen =
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(
                                if (isProcessing) progress.titleResource() else R.string.backup_restore_title,
                            ),
                            fontWeight = FontWeight.SemiBold,
                            textAlign = if (isWideScreen) TextAlign.Center else TextAlign.Left,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack, enabled = !isProcessing) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(UiR.string.back),
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(Dimens.TopBar.Elevation),
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                            Dimens.TopBar.Elevation,
                        ),
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                )
                if (isProcessing) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier.widthIn(max = Dimens.Content.MaxWidth)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                if (restoreSummary == null) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            stringResource(R.string.backup_scope_message),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(stringResource(R.string.backup_portraits_message))
                        Text(stringResource(R.string.backup_preferences_excluded))
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = onCreate,
                            enabled = !isBusy,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp),
                        ) { Text(stringResource(R.string.create_backup_action)) }
                        OutlinedButton(
                            onClick = onSelectBackup,
                            enabled = !isBusy,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp),
                        ) { Text(stringResource(R.string.restore_backup_action)) }
                    }
                } else {
                    RestoreConfirmationContent(
                        summary = restoreSummary,
                        enabled = !isBusy,
                        onCancel = onCancelRestore,
                        onRestore = onConfirmRestore,
                    )
                }
            }
        }
    }
}

@Composable
internal fun RestoreConfirmationContent(
    summary: BackupSummary,
    enabled: Boolean,
    onCancel: () -> Unit,
    onRestore: () -> Unit,
) {
    val createdAt = remember(summary.createdAt) {
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
            .withZone(ZoneId.systemDefault())
            .format(summary.createdAt)
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = createdAt,
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            stringResource(
                R.string.backup_record_counts,
                summary.counts.characters,
                summary.counts.attacks,
                summary.counts.diceRolls,
                summary.counts.spells,
                summary.counts.characterSpells,
                summary.counts.races,
                summary.counts.classes,
                summary.portraitCount,
            ),
        )
        Text(stringResource(R.string.restore_replacement_warning))
        Text(stringResource(R.string.restore_preferences_unchanged))
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Button(
            onClick = onRestore,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
        ) { Text(stringResource(R.string.restore_backup_action)) }
        OutlinedButton(
            onClick = onCancel,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
        ) { Text(stringResource(UiR.string.cancel)) }
    }
}

private fun BackupProgress.titleResource(): Int = when (this) {
    BackupProgress.IDLE -> R.string.backup_restore_working
    BackupProgress.SNAPSHOTTING -> R.string.backup_progress_snapshotting
    BackupProgress.WRITING -> R.string.backup_progress_writing
    BackupProgress.VALIDATING -> R.string.backup_progress_validating
    BackupProgress.RESTORING -> R.string.backup_progress_restoring
    BackupProgress.RECOVERING -> R.string.backup_progress_recovering
}

private fun BackupMessage.stringResource(): Int = when (this) {
    BackupMessage.BACKUP_CREATED -> R.string.backup_created_success
    BackupMessage.PICKER_UNAVAILABLE -> R.string.backup_picker_unavailable
    BackupMessage.BUSY -> R.string.backup_error_busy
    BackupMessage.UNSUPPORTED_VERSION -> R.string.backup_error_unsupported_version
    BackupMessage.INVALID_ARCHIVE -> R.string.backup_error_invalid_archive
    BackupMessage.MISSING_IMAGE -> R.string.backup_error_missing_image
    BackupMessage.SIZE_LIMIT_EXCEEDED -> R.string.backup_error_size_limit
    BackupMessage.PERMISSION_DENIED -> R.string.backup_error_permission
    BackupMessage.INSUFFICIENT_STORAGE -> R.string.backup_error_storage
    BackupMessage.IO_FAILURE -> R.string.backup_error_io
    BackupMessage.EXPIRED_RESTORE -> R.string.backup_error_expired
    BackupMessage.RECOVERY_REQUIRED -> R.string.backup_error_recovery_required
}