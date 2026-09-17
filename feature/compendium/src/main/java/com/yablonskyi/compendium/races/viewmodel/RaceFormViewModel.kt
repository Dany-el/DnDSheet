package com.yablonskyi.compendium.races.viewmodel

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.yablonskyi.compendium.CompendiumRaceUpdateRoute
import com.yablonskyi.domain.repository.RaceRepository
import com.yablonskyi.model.character.Ability
import com.yablonskyi.model.character.Skill
import com.yablonskyi.model.rulebook.Race
import com.yablonskyi.model.rulebook.RaceSize
import com.yablonskyi.ui.validation.FieldState
import com.yablonskyi.ui.validation.IntFieldState
import com.yablonskyi.ui.validation.RaceValidators
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class RaceFormViewModel @Inject constructor(
    private val repository: RaceRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val raceId: String? =
        savedStateHandle.toRoute<CompendiumRaceUpdateRoute>().raceId

    private val _effect = Channel<RaceFormEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private val _uiState = MutableStateFlow(RaceFormUiState())
    val uiState: StateFlow<RaceFormUiState> = _uiState.asStateFlow()

    private var initialSent = false

    init {
        if (raceId != null) {
            viewModelScope.launch {
                repository.getRaceById(raceId).collect { race ->
                    race?.let {
                        if (!initialSent) {
                            initialSent = true
                            _uiState.update { state ->
                                state.copy(
                                    id = race.id,
                                    name = state.name.copy(text = race.name),
                                    size = state.size.copy(text = race.size),
                                    traits = state.traits.copy(text = race.traits.joinToString(", ")),
                                    description = state.description.copy(text = race.description),
                                    speedField = state.speedField.copy(value = race.speed),
                                    abilityBonuses = race.abilityBonuses,
                                    grantedSkills = race.grantedSkills,
                                    isLoading = false
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun onIntent(intent: RaceFormIntent) {
        when (intent) {
            is RaceFormIntent.NameChanged -> onNameChanged(intent.value)
            is RaceFormIntent.NameFocusChanged -> onNameFocusChanged(intent.isFocused)
            is RaceFormIntent.SizeChanged -> onSizeChanged(intent.value)
            is RaceFormIntent.SizeFocusChanged -> onSizeFocusChanged(intent.isFocused)
            is RaceFormIntent.TraitsChanged -> onTraitsChanged(intent.value)
            is RaceFormIntent.TraitsFocusChanged -> onTraitsFocusChanged(intent.isFocused)
            is RaceFormIntent.DescriptionChanged -> onDescriptionChanged(intent.value)
            is RaceFormIntent.DescriptionFocusChanged -> onDescriptionFocusChanged(intent.isFocused)
            is RaceFormIntent.SpeedChanged -> onSpeedChanged(intent.value)
            is RaceFormIntent.SpeedFocusChanged -> onSpeedFocusChanged(intent.isFocused)
            is RaceFormIntent.AddAbilityBonus -> addAbilityBonus(intent.ability)
            is RaceFormIntent.RemoveAbilityBonus -> removeAbilityBonus(intent.ability)
            is RaceFormIntent.AbilityBonusValueChanged ->
                updateAbilityValue(intent.ability, intent.value)

            is RaceFormIntent.AddGrantedSkill -> addGrantedSkill(intent.skill)
            is RaceFormIntent.RemoveGrantedSkill -> removeGrantedSkill(intent.skill)
            RaceFormIntent.Submit -> submit()
            is RaceFormIntent.NavigateBack -> {
                viewModelScope.launch {
                    _effect.send(RaceFormEffect.NavigateBack)
                }
            }
        }
    }

    private fun submit() {
        viewModelScope.launch {
            try {
                val s = _uiState.value
                val race = Race(
                    id = raceId ?: UUID.randomUUID().toString(),
                    name = s.name.text.trim(),
                    size = s.size.text.trim(),
                    speed = s.speedField.value,
                    abilityBonuses = s.abilityBonuses,
                    grantedSkills = s.grantedSkills,
                    traits = s.parsedTraits,
                    description = s.description.text.trim(),
                    isHomebrew = true
                )
                if (raceId == null) {
                    repository.insert(race)
                }
                else {
                    repository.update(race)
                }
            } finally {
                _effect.send(RaceFormEffect.NavigateBack)
            }
        }
    }

    // ── Text Fields ───────────────────────────────────────────────────────────

    private fun onNameChanged(newValue: String) {
        _uiState.update { it.copy(name = it.name.onTextChanged(newValue)) }
    }

    private fun onNameFocusChanged(isFocused: Boolean) {
        _uiState.update {
            it.copy(name = if (isFocused) it.name.onFocusGained() else it.name.onFocusLost())
        }
    }

    private fun onSizeChanged(newValue: String) {
        _uiState.update { it.copy(size = it.size.onTextChanged(newValue)) }
    }

    private fun onSizeFocusChanged(isFocused: Boolean) {
        _uiState.update {
            it.copy(size = if (isFocused) it.size.onFocusGained() else it.size.onFocusLost())
        }
    }

    private fun onTraitsChanged(newValue: String) {
        _uiState.update { it.copy(traits = it.traits.copy(text = newValue).validate()) }
    }

    private fun onTraitsFocusChanged(isFocused: Boolean) {
        _uiState.update {
            it.copy(traits = if (isFocused) it.traits.onFocusGained() else it.traits.onFocusLost())
        }
    }

    private fun onDescriptionChanged(newValue: String) {
        _uiState.update { it.copy(description = it.description.onTextChanged(newValue)) }
    }

    private fun onDescriptionFocusChanged(isFocused: Boolean) {
        _uiState.update {
            it.copy(
                description = if (isFocused) it.description.onFocusGained()
                else it.description.onFocusLost()
            )
        }
    }

    // ── Speed ─────────────────────────────────────────────────────────────────

    private fun onSpeedChanged(newValue: Int) {
        updateState(
            _uiState.value.copy(
                speedField = _uiState.value.speedField.onValueChanged(newValue)
            )
        )
    }

    private fun onSpeedFocusChanged(isFocused: Boolean) {
        _uiState.update { currentState ->
            val currentField = currentState.speedField
            val updatedField = when {
                !isFocused -> currentField.onFocusLost()
                isFocused -> currentField.onFocusGained()
                else -> currentField
            }
            currentState.copy(speedField = updatedField)
        }
    }

    // ── Ability Bonuses ───────────────────────────────────────────────────────

    private fun addAbilityBonus(ability: Ability) {
        val updatedMap = _uiState.value.abilityBonuses + (ability to 1)
        updateState(_uiState.value.copy(abilityBonuses = updatedMap))
    }

    private fun updateAbilityValue(ability: Ability, newValue: Int) {
        val updatedMap = _uiState.value.abilityBonuses.toMutableMap().apply {
            this[ability] = newValue
        }
        updateState(_uiState.value.copy(abilityBonuses = updatedMap))
    }

    private fun removeAbilityBonus(ability: Ability) {
        val updatedMap = _uiState.value.abilityBonuses - ability
        updateState(_uiState.value.copy(abilityBonuses = updatedMap))
    }

    // ── Granted Skills ────────────────────────────────────────────────────────

    private fun addGrantedSkill(skill: Skill) {
        updateState(
            _uiState.value.copy(grantedSkills = _uiState.value.grantedSkills + skill)
        )
    }

    private fun removeGrantedSkill(skill: Skill) {
        updateState(
            _uiState.value.copy(grantedSkills = _uiState.value.grantedSkills - skill)
        )
    }

    // ── Validation ────────────────────────────────────────────────────────────

    private fun updateState(newState: RaceFormUiState) {
        _uiState.update { newState }
    }
}

@Immutable
data class RaceFormUiState(
    val id: String = "",
    val name: FieldState = FieldState(validator = RaceValidators.name),
    val size: FieldState = FieldState(
        text = RaceSize.MEDIUM.name,
        validator = RaceValidators.size),
    val traits: FieldState = FieldState(validator = RaceValidators.traits),
    val description: FieldState = FieldState(validator = RaceValidators.description),
    val speedField: IntFieldState = IntFieldState(
        minValue = 0,
        maxValue = 100,
        validator = RaceValidators.speed
    ),
    val abilityBonuses: Map<Ability, Int> = emptyMap(),
    val grantedSkills: List<Skill> = emptyList(),
    val isLoading: Boolean = false
) {
    val isFormValid: Boolean
        get() = name.isValid && size.isValid && traits.isValid &&
                description.isValid && speedField.isValid

    val parsedTraits: List<String>
        get() = traits.text.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    val availableAbilities: List<Ability>
        get() = Ability.playableAbilities.filterNot { it in abilityBonuses.keys }

    val availableSkills: List<Skill>
        get() = Skill.entries.filterNot { it in grantedSkills }
}

sealed interface RaceFormIntent {
    data class NameChanged(val value: String) : RaceFormIntent
    data class NameFocusChanged(val isFocused: Boolean) : RaceFormIntent
    data class SizeChanged(val value: String) : RaceFormIntent
    data class SizeFocusChanged(val isFocused: Boolean) : RaceFormIntent
    data class TraitsChanged(val value: String) : RaceFormIntent
    data class TraitsFocusChanged(val isFocused: Boolean) : RaceFormIntent
    data class DescriptionChanged(val value: String) : RaceFormIntent
    data class DescriptionFocusChanged(val isFocused: Boolean) : RaceFormIntent
    data class SpeedChanged(val value: Int) : RaceFormIntent
    data class SpeedFocusChanged(val isFocused: Boolean) : RaceFormIntent
    data class AddAbilityBonus(val ability: Ability) : RaceFormIntent
    data class RemoveAbilityBonus(val ability: Ability) : RaceFormIntent
    data class AbilityBonusValueChanged(val ability: Ability, val value: Int) : RaceFormIntent
    data class AddGrantedSkill(val skill: Skill) : RaceFormIntent
    data class RemoveGrantedSkill(val skill: Skill) : RaceFormIntent
    data object Submit : RaceFormIntent
    data object NavigateBack : RaceFormIntent
}

sealed interface RaceFormEffect {
    data object NavigateBack : RaceFormEffect
}