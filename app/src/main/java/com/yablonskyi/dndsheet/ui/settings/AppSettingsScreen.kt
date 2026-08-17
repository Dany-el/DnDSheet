package com.yablonskyi.dndsheet.ui.settings

import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.api.services.drive.DriveScopes
import com.yablonskyi.dndsheet.BuildConfig
import com.yablonskyi.dndsheet.R
import com.yablonskyi.dndsheet.data.update.AppUpdate
import com.yablonskyi.dndsheet.ui.update.UpdateViewModel
import com.yablonskyi.dndsheet.ui.utils.AppLanguage
import com.yablonskyi.dndsheet.ui.utils.AppTheme
import kotlinx.coroutines.launch

data class SheetOption<T>(
    val value: T,
    val label: String,
    val icon: ImageVector
)

enum class ActiveSettingsSheet {
    THEME, LANGUAGE, LIST_VIEW
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSettingsScreen(
    viewModel: AppSettingsViewModel = hiltViewModel(LocalActivity.current as ComponentActivity),
    updateViewModel: UpdateViewModel = hiltViewModel(LocalActivity.current as ComponentActivity)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentLanguageState by viewModel.language.collectAsStateWithLifecycle()
    val isBackupAvailable by viewModel.isBackupAvailable.collectAsStateWithLifecycle()

    val updateState by updateViewModel.state.collectAsStateWithLifecycle()

    val context = LocalContext.current

    var activeSheet by remember { mutableStateOf<ActiveSettingsSheet?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.checkIfBackupExists(context)
    }

    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(com.google.android.gms.common.api.Scope(DriveScopes.DRIVE_APPDATA))
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            viewModel.setLoggedInUser(account.email)
        } catch (e: ApiException) {
            Log.e("AuthError", "Google Sign In Failed. Status Code: ${e.statusCode}")
            Toast.makeText(
                context,
                "Google Sign In Failed. Status Code: ${e.statusCode}",
                Toast.LENGTH_SHORT
            ).show()
            e.printStackTrace()
        }
    }

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val isWideScreen =
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.settings),
                        fontWeight = FontWeight.SemiBold,
                        textAlign = if (isWideScreen) TextAlign.Center else TextAlign.Left,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier.widthIn(max = 840.dp),
            ) {
                // THEME
                SettingsActionRow(
                    title = stringResource(R.string.appearance),
                    currentValue = stringResource(state.theme.label),
                    onClick = { activeSheet = ActiveSettingsSheet.THEME }
                )

                HorizontalDivider()

                val language =
                    AppLanguage.entries.first { it.code == state.languageCode }

                SettingsActionRow(
                    title = stringResource(R.string.language),
                    currentValue = stringResource(language.label),
                    onClick = { activeSheet = ActiveSettingsSheet.LANGUAGE }
                )

                HorizontalDivider()

                // UPDATES
                UpdatesRow(
                    updateState = updateState,
                    onDismiss = updateViewModel::dismiss,
                    onDownload = { update -> updateViewModel.download(update) },
                    onCheckUpdate = updateViewModel::checkForUpdate,
                    onInstallFile = updateViewModel::installApk
                )

                HorizontalDivider()

                // BACKUP & SYNC SECTION
                Text(
                    text = stringResource(R.string.backup_and_sync),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp)
                )

                // ACCOUNT ROW
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.google_account),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = state.userEmail ?: stringResource(R.string.not_signed_in),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            if (state.isLoggedIn) {
                                viewModel.signOut(context)
                            } else {
                                signInLauncher.launch(googleSignInClient.signInIntent)
                            }
                        }
                    ) {
                        Text(
                            if (state.isLoggedIn) stringResource(R.string.sign_out) else stringResource(
                                R.string.sign_in
                            )
                        )
                    }
                }

                AnimatedVisibility(visible = state.isLoggedIn) {
                    Column {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.last_sync),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = state.lastSyncTime
                                        ?: stringResource(R.string.never_synced),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.restoreFromDrive(context) },
                                enabled = !state.isSyncing && isBackupAvailable,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.restore))
                            }

                            Button(
                                onClick = { viewModel.syncWithDrive(context) },
                                enabled = !state.isSyncing,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (state.isSyncing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(R.string.syncing))
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Upload,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(R.string.backup))
                                }
                            }
                        }
                        OutlinedButton(
                            onClick = { showDeleteDialog = true },
                            enabled = !state.isSyncing && isBackupAvailable,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.delete_backup),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))
                Text(
                    text = BuildConfig.VERSION_NAME,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (showDeleteDialog) {
        DeleteBackupDialog(
            onDismiss = {
                showDeleteDialog = false
            },
            onConfirm = {
                viewModel.deleteBackupFromDrive(context)
            }
        )
    }

    // BOTTOM SHEETS
    when (activeSheet) {
        ActiveSettingsSheet.THEME -> {
            SelectionBottomSheet(
                options = AppTheme.entries.map {
                    SheetOption(it, stringResource(it.label), Icons.Default.Palette)
                },
                selectedValue = state.theme,
                onSelect = { viewModel.updateTheme(it) },
                onDismiss = { activeSheet = null }
            )
        }

        ActiveSettingsSheet.LANGUAGE -> {
            SelectionBottomSheet(
                options = AppLanguage.entries.map {
                    SheetOption(it.code, stringResource(it.label), Icons.Default.Language)
                },
                selectedValue = currentLanguageState,
                onSelect = { viewModel.updateLanguage(it) },
                onDismiss = { activeSheet = null }
            )
        }

        ActiveSettingsSheet.LIST_VIEW -> {
            SelectionBottomSheet(
                options = ListView.entries.map {
                    SheetOption(it, stringResource(it.label), Icons.AutoMirrored.Filled.ViewList)
                },
                selectedValue = state.listView,
                onSelect = { viewModel.updateListView(it) },
                onDismiss = { activeSheet = null }
            )
        }

        null -> {}
    }
}

@Composable
fun DeleteBackupDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(id = R.string.dialog_delete_backup_title))
        },
        text = {
            Text(text = stringResource(id = R.string.dialog_delete_backup_text))
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                }
            ) {
                Text(
                    text = stringResource(id = R.string.delete),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.cancel))
            }
        }
    )
}

@Composable
fun SettingsActionRow(
    title: String,
    currentValue: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = currentValue,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Change $title",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SelectionBottomSheet(
    options: List<SheetOption<T>>,
    selectedValue: T,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            options.forEach { option ->
                val isSelected = option.value == selectedValue

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    onSelect(option.value)
                                    onDismiss()
                                }
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = null
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = option.label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun UpdatesRow(
    updateState: UpdateViewModel.UpdateState,
    onCheckUpdate: () -> Unit,
    onDownload: (AppUpdate) -> Unit,
    onInstallFile: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val rotation = remember { Animatable(0f) }

    fun triggerCheck() {
        onCheckUpdate()
        coroutineScope.launch {
            rotation.snapTo(0f)
            rotation.animateTo(
                targetValue = 360f,
                animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = when (updateState) {
                        is UpdateViewModel.UpdateState.Available -> Icons.Default.SystemUpdate
                        is UpdateViewModel.UpdateState.Error -> Icons.Default.ErrorOutline
                        is UpdateViewModel.UpdateState.ReadyToInstall -> Icons.Default.CheckCircle
                        else -> Icons.Default.Update
                    },
                    contentDescription = null,
                    tint = when (updateState) {
                        is UpdateViewModel.UpdateState.Available -> MaterialTheme.colorScheme.primary
                        is UpdateViewModel.UpdateState.Error -> MaterialTheme.colorScheme.error
                        is UpdateViewModel.UpdateState.ReadyToInstall -> Color(0xff529c64)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )

                Column {
                    Text(
                        text = when (updateState) {
                            is UpdateViewModel.UpdateState.Idle -> stringResource(R.string.check_for_updates)
                            is UpdateViewModel.UpdateState.Available -> stringResource(
                                R.string.update_available,
                                updateState.update.versionName
                            )

                            is UpdateViewModel.UpdateState.Downloading -> stringResource(R.string.downloading_update)
                            is UpdateViewModel.UpdateState.ReadyToInstall -> stringResource(R.string.update_ready)
                            is UpdateViewModel.UpdateState.Error -> stringResource(R.string.update_failed)
                            is UpdateViewModel.UpdateState.UptoDate -> stringResource(R.string.app_is_up_to_date)
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )

                    if (updateState is UpdateViewModel.UpdateState.Available) {
                        Text(
                            text = updateState.update.releaseNotes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (updateState is UpdateViewModel.UpdateState.Downloading) {
                        Text(
                            text = "${(updateState.progress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Trailing action
            when (updateState) {
                is UpdateViewModel.UpdateState.Idle -> {
                    IconButton(
                        onClick = { triggerCheck() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.check_for_updates),
                            modifier = Modifier.rotate(rotation.value)
                        )
                    }
                }

                is UpdateViewModel.UpdateState.UptoDate -> {
                    IconButton(
                        onClick = { triggerCheck() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.check_for_updates),
                            modifier = Modifier.rotate(rotation.value)
                        )
                    }
                }

                is UpdateViewModel.UpdateState.Available -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = stringResource(R.string.later),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { onDownload(updateState.update) }) {
                            Icon(
                                Icons.Default.Download,
                                contentDescription = stringResource(R.string.update_now),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                is UpdateViewModel.UpdateState.ReadyToInstall -> {
                    IconButton(
                        onClick = onInstallFile
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = stringResource(R.string.install_apk)
                        )
                    }
                }

                is UpdateViewModel.UpdateState.Error -> {
                    IconButton(onClick = onCheckUpdate) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.retry),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }

                else -> {}
            }
        }

        // Progress bar
        if (updateState is UpdateViewModel.UpdateState.Downloading) {
            LinearProgressIndicator(
                progress = { updateState.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 12.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )
        }
    }
}