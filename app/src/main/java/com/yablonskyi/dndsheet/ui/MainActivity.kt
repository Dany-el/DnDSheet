package com.yablonskyi.dndsheet.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yablonskyi.dndsheet.ui.settings.AppSettingsViewModel
import com.yablonskyi.dndsheet.ui.theme.DnDSheetTheme
import com.yablonskyi.dndsheet.ui.utils.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: AppSettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        splashScreen.setKeepOnScreenCondition {
            viewModel.uiState.value.isLoading
        }

        setContent {
            val appState by viewModel.uiState.collectAsStateWithLifecycle()

            DnDSheetTheme(
                darkTheme = when (appState.theme) {
                    AppTheme.LIGHT -> false
                    AppTheme.DARK -> true
                    AppTheme.SYSTEM -> isSystemInDarkTheme()
                }
            ) {
                if (!appState.isLoading) {
                    MainScreen(appState)
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