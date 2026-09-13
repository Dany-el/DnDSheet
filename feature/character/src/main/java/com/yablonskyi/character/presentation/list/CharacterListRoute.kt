package com.yablonskyi.character.presentation.list

import android.widget.Toast
import com.yablonskyi.character.platform.files.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.yablonskyi.character.platform.print.handleCharacterPrintEffect
import com.yablonskyi.ui.R
import com.yablonskyi.ui.settings.ListView

@Composable
fun CharacterListRoute(
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    listView: ListView,
    onCreateCharacter: () -> Unit,
    onOpenCharacter: (Long) -> Unit,
    onToggleListView: () -> Unit,
    onPrintCharacterSheet: suspend (String, String) -> Result<Unit>,
    viewModel: CharacterListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val importLauncher = rememberCharacterImportLauncher {
        viewModel.onIntent(CharacterListIntent.ImportDocumentSelected(it))
    }
    val exportLauncher = rememberCharacterExportLauncher {
        viewModel.onIntent(CharacterListIntent.ExportDocumentSelected(it))
    }
    val create by rememberUpdatedState(onCreateCharacter)
    val open by rememberUpdatedState(onOpenCharacter)
    val toggle by rememberUpdatedState(onToggleListView)
    val print by rememberUpdatedState(onPrintCharacterSheet)
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(viewModel, lifecycleOwner, context) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    is CharacterListEffect.OpenCharacter -> open(effect.id)
                    CharacterListEffect.CreateCharacter -> create()
                    CharacterListEffect.ToggleListView -> toggle()
                    CharacterListEffect.LaunchImport -> importLauncher.launch(arrayOf("application/json", "*/*"))
                    CharacterListEffect.LaunchExport -> exportLauncher.launch("characters_backup.json")
                    CharacterListEffect.ImportSucceeded -> Toast.makeText(context, R.string.success_import, Toast.LENGTH_SHORT).show()
                    CharacterListEffect.ExportSucceeded -> Toast.makeText(context, R.string.success_export, Toast.LENGTH_SHORT).show()
                    is CharacterListEffect.Print -> handleCharacterPrintEffect(
                        effect.effect, context, viewModel::claimPrintRequest, print,
                        { id, result -> viewModel.onIntent(CharacterListIntent.PrintFinished(id, result)) },
                        { id -> viewModel.onIntent(CharacterListIntent.PrintCancelled(id)) },
                    )
                }
            }
        }
    }
    with(sharedTransitionScope) {
        CharacterSheetsScreen(state, listView = listView, onIntent = viewModel::onIntent,
            animatedVisibilityScope = animatedVisibilityScope)
    }
}
