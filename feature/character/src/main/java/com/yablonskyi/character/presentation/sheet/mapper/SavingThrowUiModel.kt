package com.yablonskyi.character.presentation.sheet.mapper

import androidx.compose.runtime.Immutable
import com.yablonskyi.model.character.Ability
import com.yablonskyi.model.character.Character

@Immutable
data class SavingThrowUiModel(
    val ability: Ability,
    val modifier: Int,
    val isProficient: Boolean,
)

fun Character.toSavingThrows(): List<SavingThrowUiModel> =
    Ability.playableAbilities.map { ability ->
        SavingThrowUiModel(
            ability = ability,
            modifier = getSavingThrowMod(ability),
            isProficient = ability in savingThrowProficiencies,
        )
    }