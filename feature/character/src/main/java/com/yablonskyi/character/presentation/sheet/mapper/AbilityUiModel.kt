package com.yablonskyi.character.presentation.sheet.mapper

import androidx.compose.runtime.Immutable
import com.yablonskyi.model.character.Ability
import com.yablonskyi.model.character.Character

@Immutable
data class AbilityUiModel(
    val ability: Ability,
    val score: Int,
    val modifier: Int,
)

fun Character.toAbilityUiModel(): List<AbilityUiModel> =
    Ability.playableAbilities.map { ability ->
        AbilityUiModel(
            ability = ability,
            score = abilityBlock.getScore(ability),
            modifier = getAbilityMod(ability)
        )
    }