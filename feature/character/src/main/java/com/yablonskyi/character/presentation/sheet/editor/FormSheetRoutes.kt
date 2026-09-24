package com.yablonskyi.character.presentation.sheet.editor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yablonskyi.character.presentation.common.FormWrite
import com.yablonskyi.character.presentation.common.FormWriteResult
import com.yablonskyi.domain.character.CharacterChange
import com.yablonskyi.model.character.Attack
import com.yablonskyi.model.character.Character
import java.util.UUID

/** Lives outside sheet content so closing a sheet cannot cancel an accepted write handoff. */
@Composable
fun ObserveFormSheetEffects(
    attackViewModel: AttackFormViewModel, healthViewModel: HealthFormViewModel,
    onDismiss: () -> Unit, onSave: (Attack) -> Unit, onDelete: (Attack) -> Unit,
    onChange: (CharacterChange.Health, FormWrite) -> Unit
) {
    val save by rememberUpdatedState(onSave)
    val delete by rememberUpdatedState(onDelete)
    val dismiss by rememberUpdatedState(onDismiss)
    val change by rememberUpdatedState(onChange)
    LaunchedEffect(attackViewModel) {
        attackViewModel.effects.collect { effect ->
            val session = when (effect) {
                is AttackFormEffect.SaveRequested -> {
                    save(effect.attack); effect.session
                }

                is AttackFormEffect.DeleteRequested -> {
                    delete(effect.attack); effect.session
                }

                is AttackFormEffect.DismissRequested -> effect.session
            }
            if (attackViewModel.currentSession == session) dismiss()
        }
    }
    LaunchedEffect(healthViewModel) {
        healthViewModel.effects.collect { effect ->
            when (effect) {
                is HealthFormEffect.ChangeRequested -> change(effect.change, effect.write)
                is HealthFormEffect.DismissRequested -> if (healthViewModel.currentSession == effect.session) dismiss()
            }
        }
    }
}

@Composable
fun AttackFormSheet(attack: Attack, viewModel: AttackFormViewModel) {
    val session =
        rememberSaveable(attack.attackId, attack.characterId) { UUID.randomUUID().toString() }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(session) { viewModel.onIntent(AttackFormIntent.BeginSession(session, attack)) }
    if (state.initialized && viewModel.currentSession == session) UpdateAttackSheet(
        state,
        viewModel::onIntent
    )
}

@Composable
fun HealthFormSheet(
    character: Character,
    result: FormWriteResult?,
    viewModel: HealthFormViewModel
) {
    val session = rememberSaveable(character.id) { UUID.randomUUID().toString() }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(session, character) {
        viewModel.onIntent(HealthFormIntent.BeginSession(session, character))
        viewModel.onIntent(HealthFormIntent.Synchronize(character))
    }
    LaunchedEffect(result) { result?.let { viewModel.onIntent(HealthFormIntent.WriteFinished(it)) } }
    if (state.initialized && viewModel.currentSession == session) HealthEditSheetContent(
        state,
        viewModel::onIntent
    )
}