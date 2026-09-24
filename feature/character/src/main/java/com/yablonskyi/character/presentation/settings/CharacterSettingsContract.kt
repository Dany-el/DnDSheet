package com.yablonskyi.character.presentation.settings

import androidx.compose.runtime.Immutable
import com.yablonskyi.character.presentation.common.*
import com.yablonskyi.domain.character.*
import com.yablonskyi.model.character.Character

@Immutable
data class CharacterSettingsState(
    val character: Character? = null,
    val status: CharacterLoadStatus = CharacterLoadStatus.LOADING,
    val drafts: Map<CharacterTextField, String> = emptyMap(),
    val pendingWrites: Int = 0,
    val formWriteResults: Map<String, FormWriteResult> = emptyMap(),
    val isPickingImage: Boolean = false,
    val isSavingImage: Boolean = false,
    val errors: Set<CharacterUiError> = emptySet(),
)

sealed interface CharacterSettingsIntent {
    data class Change(val change: CharacterChange, val formWrite: FormWrite? = null) : CharacterSettingsIntent
    data object ImagePickerClicked : CharacterSettingsIntent
    data class ImageSelected(val uri: String?) : CharacterSettingsIntent
    data object BackClicked : CharacterSettingsIntent
    data object Retry : CharacterSettingsIntent
    data object DismissError : CharacterSettingsIntent
}

sealed interface CharacterSettingsEffect {
    data object LaunchImagePicker : CharacterSettingsEffect
    data object Back : CharacterSettingsEffect
}

internal sealed interface CharacterSettingsMutation {
    data class Loaded(val character: Character?) : CharacterSettingsMutation
    data class Draft(val change: CharacterChange.Text) : CharacterSettingsMutation
    data class DiscardDraft(val change: CharacterChange.Text) : CharacterSettingsMutation
    data class WriteCount(val delta: Int) : CharacterSettingsMutation
    data class PickingImage(val picking: Boolean) : CharacterSettingsMutation
    data class SavingImage(val saving: Boolean) : CharacterSettingsMutation
    data class Failed(val error: CharacterUiError) : CharacterSettingsMutation
    data object ClearErrors : CharacterSettingsMutation
}