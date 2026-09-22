package com.yablonskyi.dndsheet

import android.content.Intent
import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.splitcompat.SplitCompat
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.yablonskyi.domain.backup.BackupRecoveryManager
import com.yablonskyi.domain.backup.BackupRecoveryState
import com.yablonskyi.settings.AppSettingsViewModel
import com.yablonskyi.settings.update.UpdateDialog
import com.yablonskyi.settings.update.UpdateViewModel
import com.yablonskyi.settings.utils.AppTheme
import com.yablonskyi.ui.theme.DnDSheetTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.getValue

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject lateinit var backupRecovery: BackupRecoveryManager
    private val viewModel: AppSettingsViewModel by viewModels()
    private val updateViewModel: UpdateViewModel by viewModels()

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
        SplitCompat.installActivity(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        splashScreen.setKeepOnScreenCondition {
            viewModel.uiState.value.isLoading || backupRecovery.state.value.let {
                it == BackupRecoveryState.Pending || it == BackupRecoveryState.Recovering
            }
        }

        setContent {
            val appState by viewModel.uiState.collectAsStateWithLifecycle()
            val recoveryState by backupRecovery.state.collectAsStateWithLifecycle()

            LaunchedEffect(recoveryState) {
                if (recoveryState == BackupRecoveryState.Ready) updateViewModel.checkForUpdate()
            }

            DnDSheetTheme(
                darkTheme = when (appState.theme) {
                    AppTheme.LIGHT -> false
                    AppTheme.DARK -> true
                    AppTheme.SYSTEM -> isSystemInDarkTheme()
                }
            ) {
                when (recoveryState) {
                    BackupRecoveryState.Ready -> if (!appState.isLoading) {
                        MainScreen(appState)
                    }
                    is BackupRecoveryState.Failed -> AlertDialog(
                        onDismissRequest = {},
                        title = { Text(stringResource(R.string.backup_recovery_failed_title)) },
                        text = { Text(stringResource(R.string.backup_recovery_failed_message)) },
                        confirmButton = {
                            TextButton(onClick = {
                                lifecycleScope.launch { backupRecovery.recover() }
                            }) {
                                Text(stringResource(R.string.retry))
                            }
                        },
                    )
                    BackupRecoveryState.Pending,
                    BackupRecoveryState.Recovering -> Unit
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        viewModel.syncLanguageWithSystem()
    }
}