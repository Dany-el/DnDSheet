package com.yablonskyi.dndsheet.ui.settings

import androidx.activity.ComponentActivity
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yablonskyi.dndsheet.R
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
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()

    // Track which bottom sheet to show
    var activeSheet by remember { mutableStateOf<ActiveSettingsSheet?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.settings),
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            // THEME
            SettingsActionRow(
                title = stringResource(R.string.appearance),
                currentValue = stringResource(state.theme.label),
                onClick = { activeSheet = ActiveSettingsSheet.THEME }
            )

            HorizontalDivider()

            val language =
                AppLanguage.entries.first { language -> language.code == state.languageCode }

            SettingsActionRow(
                title = stringResource(R.string.language),
                currentValue = stringResource(language.label),
                onClick = { activeSheet = ActiveSettingsSheet.LANGUAGE }
            )

            HorizontalDivider()

            // LIST VIEW
            SettingsActionRow(
                title = stringResource(R.string.list_view_title),
                currentValue = stringResource(state.listView.label),
                onClick = { activeSheet = ActiveSettingsSheet.LIST_VIEW }
            )

            HorizontalDivider()
        }
    }

    // --- BOTTOM SHEETS ---
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
                selectedValue = language,
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
            options.forEachIndexed { index, option ->
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
                    /*Icon(
                        imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )*/
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
                HorizontalDivider()
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}